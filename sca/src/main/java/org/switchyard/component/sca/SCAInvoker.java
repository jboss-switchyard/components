/*
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.switchyard.component.sca;

import javax.xml.namespace.QName;

import org.switchyard.Context;
import org.switchyard.Exchange;
import org.switchyard.ExchangePattern;
import org.switchyard.ExchangeState;
import org.switchyard.HandlerException;
import org.switchyard.Message;
import org.switchyard.Scope;
import org.switchyard.ServiceReference;
import org.switchyard.SwitchYardException;
import org.switchyard.component.common.SynchronousInOutHandler;
import org.switchyard.config.model.composite.SCABindingModel;
import org.switchyard.deploy.BaseServiceHandler;
import org.switchyard.label.BehaviorLabel;
import org.switchyard.remote.RemoteMessage;
import org.switchyard.remote.RemoteRegistry;
import org.switchyard.remote.cluster.ClusteredInvoker;
import org.switchyard.remote.cluster.LoadBalanceStrategy;
import org.switchyard.remote.cluster.RandomStrategy;
import org.switchyard.remote.cluster.RoundRobinStrategy;
import org.switchyard.runtime.event.ExchangeCompletionEvent;

/**
 * Handles outbound communication to an SCA service endpoint.
 */
public class SCAInvoker extends BaseServiceHandler {
    
    private final SCABindingModel _config;
    private final String _bindingName;
    private final String _referenceName;
    private ClusteredInvoker _invoker;
    
    /**
     * Create a new SCAInvoker for invoking local endpoints.
     * @param config binding configuration model
     */
    public SCAInvoker(SCABindingModel config) {
        _config = config;
        _bindingName = config.getName();
        _referenceName = config.getReference().getName();
    }
    
    /**
     * Create a new SCAInvoker capable of invoking remote service endpoints.
     * @param config binding configuration model
     * @param registry registry of remote services
     */
    public SCAInvoker(SCABindingModel config, RemoteRegistry registry) {
        this(config);
        if (config.isLoadBalanced()) {
            LoadBalanceStrategy loadBalancer = createLoadBalancer(config.getLoadBalance());
            _invoker = new ClusteredInvoker(registry, loadBalancer);
        } else {
            _invoker = new ClusteredInvoker(registry);
        }
    }
    
    @Override
    public void handleMessage(Exchange exchange) throws HandlerException {
        // identify ourselves
        exchange.getContext().setProperty(ExchangeCompletionEvent.GATEWAY_NAME, _bindingName, Scope.EXCHANGE)
                .addLabels(BehaviorLabel.TRANSIENT.label());

        if (getState() != State.STARTED) {
            throw SCAMessages.MESSAGES.referenceBindingNotStarted(_referenceName, _bindingName);
        }
        try {
            if (_config.isClustered()) {
                invokeRemote(exchange);
            } else {
                invokeLocal(exchange);
            }
        } catch (SwitchYardException syEx) {
            throw new HandlerException(syEx.getMessage());
        }
    }
    
    private void invokeLocal(Exchange exchange) throws HandlerException {
        // Figure out the QName for the service were invoking
        QName serviceName = getTargetServiceName(exchange);
        // Get a handle for the reference and use a copy of the exchange to invoke it
        ServiceReference ref = exchange.getProvider().getDomain().getServiceReference(serviceName);
        if (ref == null) {
            throw SCAMessages.MESSAGES.serviceReferenceNotFoundInDomain(serviceName.toString(), exchange.getProvider().getDomain().getName().toString());
        }
        SynchronousInOutHandler replyHandler = new SynchronousInOutHandler();
        Exchange ex = ref.createExchange(exchange.getContract().getProviderOperation().getName(), replyHandler);
        
        // Can't send same message twice, so make a copy
        Message invokeMsg = exchange.getMessage().copy();
        exchange.getContext().mergeInto(invokeMsg.getContext());
        
        ex.send(invokeMsg);
        if (ExchangePattern.IN_OUT.equals(ex.getPattern())) {
            replyHandler.waitForOut();
            if (ex.getMessage() != null) {
                Message replyMsg = ex.getMessage().copy();
                ex.getContext().mergeInto(replyMsg.getContext());
                if (ExchangeState.FAULT.equals(ex.getState())) {
                    exchange.sendFault(replyMsg);
                } else {
                    exchange.send(replyMsg);
                }
            }
        } else if (ExchangeState.FAULT.equals(ex.getState())) {
            // Even though this is in-only, we need to report a runtime fault on send
            throw createHandlerException(ex.getMessage());
        }
    }
    
    private void invokeRemote(Exchange exchange) throws HandlerException {
        // Figure out the QName for the service were invoking
        QName serviceName = getTargetServiceName(exchange);

        RemoteMessage request = new RemoteMessage()
            .setDomain(exchange.getProvider().getDomain().getName())
            .setService(serviceName)
            .setContent(exchange.getMessage().getContent());
        exchange.getContext().mergeInto(request.getContext());

        try {
            RemoteMessage reply = _invoker.invoke(request);
            if (reply == null) {
                return;
            }
            
            if (ExchangePattern.IN_OUT.equals(exchange.getPattern())) {
                Message msg = exchange.createMessage();
                msg.setContent(reply.getContent());
                Context replyCtx = reply.getContext();
                if (replyCtx != null) {
                    replyCtx.mergeInto(exchange.getContext());
                }
                if (reply.isFault()) {
                    exchange.sendFault(msg);
                } else {
                    exchange.send(msg);
                }
            } else {
                // still need to account for runtime exceptions on in-only
                if (reply.isFault()) {
                    throw createHandlerException(reply.getContent());
                }
            }
        } catch (java.io.IOException ioEx) {
            ioEx.printStackTrace();
            exchange.sendFault(exchange.createMessage().setContent(ioEx));
        }
    }
    
    private QName getTargetServiceName(Exchange exchange) {
        // Figure out the QName for the service were invoking
        QName service = exchange.getProvider().getName();
        String targetName = _config.hasTarget() ? _config.getTarget() : service.getLocalPart();
        String targetNS = _config.hasTargetNamespace() ? _config.getTargetNamespace() : service.getNamespaceURI();
        return new QName(targetNS, targetName);
    }
    
    private HandlerException createHandlerException(Message message) {
        return createHandlerException(message == null ? null : message.getContent());
    }
    
    private HandlerException createHandlerException(Object content) {
        HandlerException ex;
        if (content == null) {
            ex = SCAMessages.MESSAGES.runtimeFaultOccurredWithoutExceptionDetails();
        } else if (content instanceof HandlerException) {
            ex = (HandlerException)content;
        } else if (content instanceof Throwable) {
            ex = new HandlerException((Throwable)content);
        } else {
            ex = new HandlerException(content.toString());
        }
        return ex;
    }
    
    
    LoadBalanceStrategy createLoadBalancer(String strategy) {
        if (RoundRobinStrategy.class.getSimpleName().equals(strategy)) {
            return new RoundRobinStrategy();
        } else if (RandomStrategy.class.getSimpleName().equals(strategy)) {
            return new RandomStrategy();
        } else {
            try {
                Class<?> strategyClass = Class.forName(strategy);
                if (!LoadBalanceStrategy.class.isAssignableFrom(strategyClass)) {
                    throw SCAMessages.MESSAGES.loadBalanceClassDoesNotImplementLoadBalanceStrategy(strategy);
                }
                return (LoadBalanceStrategy)strategyClass.newInstance();
            } catch (Exception ex) {
                throw SCAMessages.MESSAGES.unableToInstantiateStrategyClass(strategy, ex);
            }
        }
    }
}
