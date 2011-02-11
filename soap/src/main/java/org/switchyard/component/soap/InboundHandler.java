/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 
package org.switchyard.component.soap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Endpoint;

import org.apache.log4j.Logger;
import org.switchyard.BaseHandler;
import org.switchyard.Context;
import org.switchyard.Exchange;
import org.switchyard.HandlerException;
import org.switchyard.Message;
import org.switchyard.Service;
import org.switchyard.ServiceDomain;
import org.switchyard.component.soap.config.model.SOAPBindingModel;
import org.switchyard.component.soap.util.SOAPUtil;
import org.switchyard.component.soap.util.WSDLUtil;
import org.switchyard.internal.ServiceDomains;
import org.switchyard.internal.transform.TransformSequence;
import org.switchyard.metadata.BaseExchangeContract;
import org.switchyard.metadata.ServiceOperation;

/**
 * Hanldes SOAP requests to invoke a SwitchYard service.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 */
public class InboundHandler extends BaseHandler {

    private static final Logger LOGGER = Logger.getLogger(InboundHandler.class);
    private static final long DEFAULT_TIMEOUT = 15000;
    private static final int DEFAULT_SLEEP = 100;
    private static final String MESSAGE_NAME = "MESSAGE_NAME";

    private final ConcurrentHashMap<String, BaseExchangeContract> _contracts = new ConcurrentHashMap<String, BaseExchangeContract>();
    private static ThreadLocal<SOAPMessage> _response = new ThreadLocal<SOAPMessage>();

    private MessageComposer _composer;
    private MessageDecomposer _decomposer;
    private ServiceDomain _domain;
    private Service _service;
    private long _waitTimeout = DEFAULT_TIMEOUT; // default of 15 seconds
    private Endpoint _endpoint;
    private Port _wsdlPort;
    private String _scheme = "http";
    private SOAPBindingModel _config;

    /**
     * Constructor.
     * @param config the configuration settings
     */
    public InboundHandler(SOAPBindingModel config) {
        _config = config;
        String composer = config.getComposer();
        String decomposer = config.getDecomposer();

        if (composer != null && composer.length() > 0) {
            try {
                Class<? extends MessageComposer> composerClass = Class.forName(composer).asSubclass(MessageComposer.class);
                _composer = composerClass.newInstance();
            } catch (Exception cnfe) {
                LOGGER.error("Could not instantiate composer", cnfe);
            }
        }
        if (_composer == null) {
            _composer = new DefaultMessageComposer();
        }
        if (decomposer != null && decomposer.length() > 0) {
            try {
                Class<? extends MessageDecomposer> decomposerClass = Class.forName(decomposer).asSubclass(MessageDecomposer.class);
                _decomposer = decomposerClass.newInstance();
            } catch (Exception cnfe) {
                LOGGER.error("Could not instantiate decomposer", cnfe);
            }
        }
        if (_decomposer == null) {
            _decomposer = new DefaultMessageDecomposer();
        }

        _domain = ServiceDomains.getDomain();
    }

    /**
     * Start lifecycle.
     * @throws WebServicePublishException If unable to publish the endpoint
     */
    public void start() throws WebServicePublishException {
        try {
            PortName portName = _config.getPort();
            javax.wsdl.Service wsdlService = WSDLUtil.getService(_config.getWsdl(), portName);
            _wsdlPort = WSDLUtil.getPort(wsdlService, portName);
            // Update the portName
            portName.setServiceQName(wsdlService.getQName());
            portName.setName(_wsdlPort.getName());
            
            BaseWebService wsProvider = new BaseWebService();
            // Hook the handler
            wsProvider.setConsumer(this);
            
            // lookup the SwitchYard service
            _service = _domain.getService(_config.getServiceName());
            if (_service == null) {
                throw new WebServicePublishException(
                        "Target service not registered: " + _config.getServiceName());
            }
            _contracts.putAll(WSDLUtil.getContracts(_wsdlPort,_service));

            _endpoint = Endpoint.create(wsProvider);
            List<Source> metadata = new ArrayList<Source>();
            StreamSource source = WSDLUtil.getStream(_config.getWsdl());
            metadata.add(source);
            _endpoint.setMetadata(metadata);
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(Endpoint.WSDL_SERVICE, portName.getServiceQName());
            properties.put(Endpoint.WSDL_PORT, portName.getPortQName());
            _endpoint.setProperties(properties);

            String path = "/" + portName.getServiceName();
            if (_config.getContextPath() != null) {
                path = "/" + _config.getContextPath() + "/" + portName.getServiceName();
            }
            String publishUrl = _scheme + "://" + _config.getServerHost() + ":" + _config.getServerPort() + path;

            _endpoint.publish(publishUrl);
            LOGGER.info("WebService published at " + publishUrl);
        } catch (WSDLException e) {
            throw new WebServicePublishException(e);
        }
    }

    /**
     * Stop lifecycle.
     */
    public void stop() {
        _endpoint.stop();
        LOGGER.info("WebService " + _config.getPort() + " stopped.");
    }

    /**
     * The handler method that handles responses from a WebService.
     * @param exchange the Exchange
     * @throws HandlerException handler exception
     */
    @Override
    public void handleMessage(final Exchange exchange) throws HandlerException {
        assertTransformsApplied(exchange);
        try {
            _response.set(_decomposer.decompose(exchange.getMessage()));
        } catch (SOAPException se) {
            throw new HandlerException("Unexpected exception generating SOAP Message", se);
        }
    }

    /**
     * The handler method that handles faults from a WebService.
     * @param exchange the Exchange
     */
    @Override
    public void handleFault(final Exchange exchange) {
        try {
            _response.set(_decomposer.decompose(exchange.getMessage()));
        } catch (SOAPException se) {
            try {
                _response.set(SOAPUtil.generateFault(se));
            } catch (SOAPException e) {
                LOGGER.error(e);
            }
        }
    }

    /**
     * The delegate method called by the Webservice implementation.
     * @param soapMessage the SOAP request
     * @return the SOAP response
     */
    public SOAPMessage invoke(final SOAPMessage soapMessage) {
        String operationName;
        ServiceOperation targetServiceOperation;
        BaseExchangeContract exchangeContract;
        Operation operation;
        Boolean oneWay = false;

        // Clear the response...
        _response.remove();

        try {
            operationName = SOAPUtil.getOperationName(soapMessage);
            operation = WSDLUtil.getOperation(_wsdlPort, operationName);
            exchangeContract = _contracts.get(operationName);
        } catch (SOAPException e) {
            LOGGER.error(e);
            return null;
        }

        if (exchangeContract == null) {
            handleException(oneWay, new SOAPException("Operation '" + operationName + "' not available on target Service '" + _service.getName() + "'."));
            return _response.get();
        }

        try {
            Exchange exchange;

            exchange = _domain.createExchange(_service, exchangeContract, this);
            Message message = _composer.compose(soapMessage, exchange);

            if (!assertComposedMessageOK(message, operation, oneWay)) {
                return _response.get();
            }

            Context msgCtx = message.getContext();
            msgCtx.setProperty(MESSAGE_NAME, operation.getInput().getMessage().getQName().getLocalPart());


            if (oneWay) {
                exchange.send(message);
            } else {
                exchange.send(message);
                waitForResponse();
            }

            return _response.get();

        } catch (SOAPException se) {
            handleException(oneWay, se);
        } finally {
            _response.remove();
        }

        return null;
    }

    private boolean assertComposedMessageOK(Message soapMessage, Operation operation, Boolean oneWay) {
        Object content = soapMessage.getContent();

        if (content == null) {
            handleException(oneWay, new SOAPException("Composer created a null ESB Message payload for service '" + _service.getName() + "'.  Must be of type '" + SOAPMessage.class.getName() + "'."));
            return false;
        } else if (!(content instanceof Node)) {
            handleException(oneWay, new SOAPException("Composer created invalid ESB Message payload type '" + content.getClass().getName() + "' for service '" + _service.getName() + "'.  Must be of type '" + Node.class.getName() + "'."));
            return false;
        }

        Node inputMessage = (Node) content;
        QName expectedPayloadType = operation.getInput().getMessage().getQName();
        String expectedNS = expectedPayloadType.getNamespaceURI();
        String expectedLN = expectedPayloadType.getLocalPart();
        String actualNS = inputMessage.getNamespaceURI();
        String actualLN = inputMessage.getLocalName();

        if (expectedNS != null && !expectedNS.equals(actualNS)) {
            handleException(oneWay, new SOAPException("Invalid input SOAP payload namespace for service operation '" + operation.getName() + "' (service '" + _service.getName()
                                                                              + "').  Port defines operation namespace as '" + expectedNS + "'.  Actual namespace on input SOAP message '" + actualNS + "'."));
            return false;
        } else if (expectedLN != null && !expectedLN.equals(actualLN)) {
            handleException(oneWay, new SOAPException("Invalid input SOAP payload localNamePart for service operation '" + operation.getName() + "' (service '" + _service.getName()
                                                                              + "').  Port defines operation localNamePart as '" + expectedLN + "'.  Actual localNamePart on input SOAP message '" + actualLN + "'."));
            return false;
        }

        return true;
    }

    private void handleException(Boolean oneWay, SOAPException se) {
        if (oneWay) {
            LOGGER.error(se);
        } else {
            try {
                _response.set(SOAPUtil.generateFault(se));
            } catch (SOAPException e) {
                LOGGER.error(e);
            }
        }
    }

    /**
     * Sleep until we get a response or timeout has reached.
     */
    private void waitForResponse() {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() < start + _waitTimeout) {
            if (_response.get() != null) {
                return;
            }
            try {
                Thread.sleep(DEFAULT_SLEEP);
            } catch (InterruptedException e) {
                //ignore
                continue;
            }
        }
    }

    private void assertTransformsApplied(Exchange exchange) throws HandlerException {
        if (!TransformSequence.assertTransformsApplied(exchange)) {
            String actualPayloadType = TransformSequence.getCurrentMessageType(exchange);
            String expectedPayloadType = TransformSequence.getTargetMessageType(exchange);
            String operationName = exchange.getContract().getServiceOperation().getName();

            throw new HandlerException("Error invoking '" + operationName + "'.  Response requires a payload type of '" + expectedPayloadType + "'.  Actual payload type is '" + actualPayloadType + "'.  You must define and register a Transformer to transform between these types.");
        }
    }
}
