/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use, 
 * modify, copy, or redistribute it subject to the terms and conditions 
 * of the GNU Lesser General Public License, v. 2.1. 
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details. 
 * You should have received a copy of the GNU Lesser General Public License, 
 * v.2.1 along with this distribution; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.switchyard.component.rules.exchange;

import static org.switchyard.component.common.knowledge.util.Operations.getInputList;
import static org.switchyard.component.common.knowledge.util.Operations.getListMap;
import static org.switchyard.component.common.knowledge.util.Operations.setFaults;
import static org.switchyard.component.common.knowledge.util.Operations.setGlobals;
import static org.switchyard.component.common.knowledge.util.Operations.setOutputs;
import static org.switchyard.component.common.knowledge.util.Operations.toVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.namespace.QName;

import org.kie.api.runtime.rule.SessionEntryPoint;
import org.switchyard.Context;
import org.switchyard.Exchange;
import org.switchyard.ExchangePattern;
import org.switchyard.HandlerException;
import org.switchyard.Message;
import org.switchyard.ServiceDomain;
import org.switchyard.common.lang.Strings;
import org.switchyard.common.type.Classes;
import org.switchyard.component.common.knowledge.exchange.KnowledgeOperation;
import org.switchyard.component.common.knowledge.exchange.KnowledgeExchangeHandler;
import org.switchyard.component.common.knowledge.session.KnowledgeDisposal;
import org.switchyard.component.common.knowledge.session.KnowledgeSession;
import org.switchyard.component.rules.RulesOperationType;
import org.switchyard.component.rules.RulesConstants;
import org.switchyard.component.rules.config.model.RulesComponentImplementationModel;

/**
 * A "rules" implementation of a KnowledgeExchangeHandler.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; &copy; 2012 Red Hat Inc.
 */
public class RulesExchangeHandler extends KnowledgeExchangeHandler<RulesComponentImplementationModel> {

    private static final AtomicInteger FIRE_UNTIL_HALT_COUNT = new AtomicInteger();
    private static final KnowledgeOperation DEFAULT_OPERATION = new KnowledgeOperation(RulesOperationType.EXECUTE);

    private Thread _fireUntilHaltThread = null;

    /**
     * Constructs a new RulesExchangeHandler with the specified model, service domain, and service name.
     * @param model the specified model
     * @param serviceDomain the specified service domain
     * @param serviceName the specified service name
     */
    public RulesExchangeHandler(RulesComponentImplementationModel model, ServiceDomain serviceDomain, QName serviceName) {
        super(model, serviceDomain, serviceName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public KnowledgeOperation getDefaultOperation() {
        return DEFAULT_OPERATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleOperation(Exchange exchange, KnowledgeOperation operation) throws HandlerException {
        Integer sessionId = null;
        Message inputMessage = exchange.getMessage();
        ExchangePattern exchangePattern = exchange.getContract().getProviderOperation().getExchangePattern();
        Map<String, Object> expressionContext = new HashMap<String, Object>();
        RulesOperationType operationType = (RulesOperationType)operation.getType();
        switch (operationType) {
            case EXECUTE: {
                KnowledgeSession session = newStatelessSession();
                sessionId = session.getId();
                setGlobals(inputMessage, operation, session);
                List<Object> facts = getInputList(inputMessage, operation);
                session.getStateless().execute(facts);
                if (ExchangePattern.IN_OUT.equals(exchangePattern)) {
                    expressionContext.putAll(getGlobalVariables(session));
                }
                break;
            }
            case INSERT:
            case FIRE_ALL_RULES: {
                /*
                if (!isContinue(exchange)) {
                    disposeStatefulSession();
                }
                */
                KnowledgeSession session = getStatefulSession();
                sessionId = session.getId();
                setGlobals(inputMessage, operation, session);
                List<Object> facts = getInputList(inputMessage, operation);
                for (Object fact : facts) {
                    session.getStateful().insert(fact);
                }
                if (RulesOperationType.FIRE_ALL_RULES.equals(operationType)) {
                    session.getStateful().fireAllRules();
                }
                if (ExchangePattern.IN_OUT.equals(exchangePattern)) {
                    expressionContext.putAll(getGlobalVariables(session));
                }
                if (isDispose(exchange)) {
                    disposeStatefulSession();
                }
                break;
            }
            case FIRE_UNTIL_HALT: {
                /*
                if (!isContinue(exchange)) {
                    disposeStatefulSession();
                }
                */
                KnowledgeSession session = getStatefulSession();
                sessionId = session.getId();
                setGlobals(inputMessage, operation, session);
                if (_fireUntilHaltThread == null) {
                    FireUntilHalt fireUntilHalt = new FireUntilHalt(this, session, getLoader());
                    session.addDisposals(fireUntilHalt);
                    _fireUntilHaltThread = fireUntilHalt.startThread();
                }
                final String undefinedVariable = toVariable(exchange);
                Map<String, List<Object>> inputMap = getListMap(inputMessage, operation.getInputExpressionMappings(), true, undefinedVariable);
                if (inputMap.size() > 0) {
                    for (Entry<String, List<Object>> inputEntry : inputMap.entrySet()) {
                        String key = inputEntry.getKey();
                        if (undefinedVariable.equals(key)) {
                            String eventId = Strings.trimToNull(operation.getEventId());
                            if (eventId != null) {
                                key = eventId;
                            }
                        }
                        List<Object> facts = inputEntry.getValue();
                        if (undefinedVariable.equals(key)) {
                            for (Object fact : facts) {
                                session.getStateful().insert(fact);
                            }
                        } else {
                            SessionEntryPoint sep = session.getStateful().getEntryPoint(key);
                            if (sep != null) {
                                for (Object fact : facts) {
                                    sep.insert(fact);
                                }
                            } else {
                                throw new HandlerException("Unknown entry point: " + sep + "; please check your rules source.");
                            }
                        }
                    }
                } else {
                    List<Object> facts = getInputList(inputMessage, operation);
                    for (Object fact : facts) {
                        session.getStateful().insert(fact);
                    }
                }
                if (ExchangePattern.IN_OUT.equals(exchangePattern)) {
                    expressionContext.putAll(getGlobalVariables(session));
                }
                if (isDispose(exchange)) {
                    disposeStatefulSession();
                }
                break;
            }
            default: {
                throw new HandlerException("Unsupported operation type: " + operationType);
            }
        }
        if (ExchangePattern.IN_OUT.equals(exchangePattern)) {
            Message outputMessage = exchange.createMessage();
            Context outputContext = exchange.getContext(outputMessage);
            if (sessionId != null && sessionId.intValue() > 0) {
                outputContext.setProperty(RulesConstants.SESSION_ID_PROPERTY, sessionId);
            }
            setFaults(outputMessage, operation, expressionContext);
            if (outputMessage.getContent() != null) {
                exchange.sendFault(outputMessage);
            } else {
                setOutputs(outputMessage, operation, expressionContext);
                exchange.send(outputMessage);
            }
        }
    }

    /*
    private boolean isContinue(Exchange exchange) {
        return isBoolean(exchange, RulesConstants.CONTINUE_PROPERTY);
    }
    */

    private boolean isDispose(Exchange exchange) {
        return isBoolean(exchange, RulesConstants.DISPOSE_PROPERTY);
    }

    private final class FireUntilHalt implements Runnable, KnowledgeDisposal {

        private final RulesExchangeHandler _handler;
        private final KnowledgeSession _session;
        private final ClassLoader _loader;

        private FireUntilHalt(RulesExchangeHandler handler, KnowledgeSession session, ClassLoader loader) {
            _handler = handler;
            _session = session;
            _loader = loader;
        }

        @Override
        public void run() {
            ClassLoader originalLoader = Classes.setTCCL(_loader);
            try {
                _session.getStateful().fireUntilHalt();
            } finally {
                try {
                    _handler.disposeStatefulSession();
                } finally {
                    Classes.setTCCL(originalLoader);
                }
            }
        }

        @Override
        public void dispose() {
            _handler._fireUntilHaltThread = null;
        }

        private Thread startThread() {
            Thread thread = new Thread(this);
            String name = new StringBuilder()
                .append(_handler.getClass().getSimpleName())
                .append(':')
                .append(getClass().getSimpleName())
                .append(':')
                .append(FIRE_UNTIL_HALT_COUNT.incrementAndGet())
                .toString();
            thread.setName(name);
            thread.setDaemon(true);
            thread.start();
            return thread;
        }

    }

}
