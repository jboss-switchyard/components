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
package org.switchyard.component.bean.internal.exchange;

import javax.enterprise.context.ApplicationScoped;

import org.switchyard.Context;
import org.switchyard.Exchange;
import org.switchyard.ExchangeHandler;
import org.switchyard.ExchangePattern;
import org.switchyard.ExchangePhase;
import org.switchyard.ExchangeState;
import org.switchyard.Message;
import org.switchyard.Service;
import org.switchyard.ServiceReference;
import org.switchyard.metadata.ExchangeContract;
import org.switchyard.metadata.ServiceOperation;

/**
 * SwitchYard Exchange proxy.
 */
@ApplicationScoped
public class ExchangeProxy implements Exchange {

    private static final ThreadLocal<Exchange> EXCHANGE = new ThreadLocal<Exchange>();

    /**
     * Gets the {@link Exchange} for the current thread.
     * @return the message
     */
    private static Exchange getExchange() {
        Exchange exchange = EXCHANGE.get();
        if (exchange == null) {
            throw new IllegalStateException("Illegal call to get the SwitchYard Exchange; must be called within the execution of an ExchangeHandler chain.");
        }
        return exchange;
    }

    /**
     * Sets the {@link Exchange} for the current thread.
     * @param exchange the exchange
     */
    public static void setExchange(Exchange exchange) {
        if (exchange != null) {
            EXCHANGE.set(exchange);
        } else {
            EXCHANGE.remove();
        }
    }

    @Override
    public Context getContext() {
        return getExchange().getContext();
    }

    @Override
    public Context getContext(Message message) {
        return getExchange().getContext(message);
    }

    @Override
    public ServiceReference getConsumer() {
        return getExchange().getConsumer();
    }

    @Override
    public Service getProvider() {
        return getExchange().getProvider();
    }

    @Override
    public ExchangeContract getContract() {
        return getExchange().getContract();
    }

    @Override
    public Exchange consumer(ServiceReference consumer, ServiceOperation operation) {
        throw new ProxyNotSupportedException("consumer");
    }

    @Override
    public Exchange provider(Service provider, ServiceOperation operation) {
        throw new ProxyNotSupportedException("provider");
    }

    @Override
    public Message getMessage() {
        return getExchange().getMessage();
    }

    @Override
    public Message createMessage() {
        return getExchange().createMessage();
    }

    @Override
    public void send(Message message) {
        getExchange().send(message);
    }

    @Override
    public void sendFault(Message message) {
        getExchange().sendFault(message);
    }

    @Override
    public ExchangeState getState() {
        return getExchange().getState();
    }

    @Override
    public ExchangePhase getPhase() {
        return getExchange().getPhase();
    }

    @Override
    public ExchangeHandler getReplyHandler() {
        throw new ProxyNotSupportedException("getReplyHandler");
    }

    @Override
    public ExchangePattern getPattern() {
        return getExchange().getPattern();
    }
    
}

final class ProxyNotSupportedException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    ProxyNotSupportedException(String operation) {
        super("Operation " + operation + " not supported on injected Exchange instance");
    }
}
