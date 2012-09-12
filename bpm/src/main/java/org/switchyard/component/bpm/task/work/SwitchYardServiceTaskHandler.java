/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
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
package org.switchyard.component.bpm.task.work;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.switchyard.Context;
import org.switchyard.Exchange;
import org.switchyard.ExchangePattern;
import org.switchyard.Message;
import org.switchyard.Property;
import org.switchyard.Scope;
import org.switchyard.ServiceReference;
import org.switchyard.SynchronousInOutHandler;
import org.switchyard.common.lang.Strings;
import org.switchyard.common.type.Classes;
import org.switchyard.common.xml.XMLHelper;
import org.switchyard.exception.DeliveryException;

/**
 * Executes SwitchYard Services.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; (C) 2011 Red Hat Inc.
 */
public class SwitchYardServiceTaskHandler extends BaseTaskHandler {

    private static final Logger LOGGER = Logger.getLogger(SwitchYardServiceTaskHandler.class);

    /** SwitchYard Service . */
    public static final String SWITCHYARD_SERVICE = "SwitchYard Service";
    /** ServiceName . */
    public static final String SERVICE_NAME = "ServiceName";
    /** ServiceOperationName . */
    public static final String SERVICE_OPERATION_NAME = "ServiceOperationName";

    /**
     * Constructs a new SwitchYardServiceTaskHandler with the default name ("SwitchYard Service").
     */
    public SwitchYardServiceTaskHandler() {
        super(SWITCHYARD_SERVICE);
    }

    /**
     * Constructs a new SwitchYardServiceTaskHandler with the specified name.
     * @param name the specified name
     */
    public SwitchYardServiceTaskHandler(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeTask(Task task, TaskManager manager) {
        String problem = null;
        Map<String,Object> parameters = task.getParameters();
        Map<String,Object> results = null;
        QName serviceName = getServiceName(parameters);
        if (serviceName != null) {
            ServiceReference serviceRef = getServiceDomain().getServiceReference(serviceName);
            if (serviceRef != null) {
                String operation = (String)parameters.get(SERVICE_OPERATION_NAME);
                final Exchange exchangeIn;
                SynchronousInOutHandler inOutHandler = new SynchronousInOutHandler();
                if (operation != null) {
                    exchangeIn = serviceRef.createExchange(operation, inOutHandler);
                } else {
                    exchangeIn = serviceRef.createExchange(inOutHandler);
                }
                Context contextIn = exchangeIn.getContext();
                for (Map.Entry<String,Object> entry : parameters.entrySet()) {
                    contextIn.setProperty(entry.getKey(), entry.getValue(), Scope.IN);
                }
                Message messageIn = exchangeIn.createMessage();
                String messageContentInName = getMessageContentInName();
                Object messageContentIn = parameters.get(messageContentInName);
                if (messageContentIn != null) {
                    messageIn.setContent(messageContentIn);
                }
                if (inOutHandler != null && ExchangePattern.IN_OUT.equals(
                        exchangeIn.getContract().getConsumerOperation().getExchangePattern())) {
                    exchangeIn.send(messageIn);
                    try {
                        Exchange exchangeOut = inOutHandler.waitForOut();
                        Message messageOut = exchangeOut.getMessage();
                        Object messageContentOut = messageOut.getContent();
                        results = task.getResults();
                        if (results == null) {
                            results = new HashMap<String,Object>();
                        }
                        String messageContentOutName = getMessageContentOutName();
                        results.put(messageContentOutName, messageContentOut);
                        Context contextOut = exchangeOut.getContext();
                        for (Property property : contextOut.getProperties(Scope.OUT)) {
                            results.put(property.getName(), property.getValue());
                        }
                    } catch (DeliveryException e) {
                        problem = e.getMessage();
                    }
                } else {
                    exchangeIn.send(messageIn);
                }
            } else {
                problem = "serviceRef (" + serviceName + ") == null";
            }
        } else {
            problem = SERVICE_NAME + " == null";
        }
        if (problem == null) {
            manager.completeTask(task.getId(), results);
        } else {
            LOGGER.error(problem);
            manager.abortTask(task.getId());
        }
    }

    private QName getServiceName(Map<String, Object> parameters) {
        QName serviceName = null;
        Object p = parameters.get(SERVICE_NAME);
        if (p instanceof QName) {
            serviceName = (QName)p;
        } else if (p instanceof String) {
            serviceName = XMLHelper.createQName((String)p);
        }
        if (serviceName != null && Strings.trimToNull(serviceName.getNamespaceURI()) == null) {
            String tns = getTargetNamespace();
            if (tns != null) {
                serviceName = XMLHelper.createQName(tns, serviceName.getLocalPart());
            }
        }
        return serviceName;
    }
}
