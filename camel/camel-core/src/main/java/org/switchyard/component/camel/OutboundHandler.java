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
package org.switchyard.component.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;
import org.switchyard.BaseHandler;
import org.switchyard.Exchange;
import org.switchyard.ExchangePattern;
import org.switchyard.HandlerException;
import org.switchyard.composer.MessageComposer;

/**
 * A handler that is capable of calling Apache Camel components and returning responses 
 * from Camel to SwitchYard.
 * <p>
 * The typical usage would be when a POJO has a field with a reference annotation. SwitchYard 
 * can inject a proxy instance which will invoke a Camel endpoint. It is an instance of this 
 * class that will handle the invocation of the Camel endpoint.
 * 
 * @author Daniel Bevenius
 *
 */
public class OutboundHandler extends BaseHandler {
    
    private static Logger _logger = Logger.getLogger(OutboundHandler.class);
    
    private final MessageComposer<org.apache.camel.Message> _messageComposer;
    private final ProducerTemplate _producerTemplate;
    private final CamelContext _camelContext;
    private final String _uri;

    /**
     * Sole constructor.
     * 
     * @param uri The Camel endpoint uri.
     * @param messageComposer the MessageComposer this handler should use
     * @param context The {@link CamelContext}.
     */
    public OutboundHandler(final String uri, final CamelContext context, MessageComposer<org.apache.camel.Message> messageComposer) {
        if (uri == null) {
            throw new IllegalArgumentException("uri argument must not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("camelContext argument must not be null");
        }
        _uri = uri;
        _camelContext = context;
        _producerTemplate = _camelContext.createProducerTemplate();
        _messageComposer = messageComposer;
    }
    
    /**
     * Starts the {@link ProducerTemplate}.
     * 
     * @throws Exception If an error occurs while trying to start the {@link ProducerTemplate}.
     */
    public void start() throws Exception {
        _logger.debug("Starting " + this);
        _producerTemplate.start();
    }
    
    /**
     * Stops the {@link ProducerTemplate}.
     * 
     * @throws Exception If an error occurs while trying to stop the {@link ProducerTemplate}.
     */
    public void stop() throws Exception {
        _logger.debug("Stopping " + this);
        _producerTemplate.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleMessage(final Exchange exchange) throws HandlerException {
        if (isInOnly(exchange)) {
            handleInOnly(exchange);
        } else {
            handleInOut(exchange);
        }
    }
    
    private boolean isInOnly(final Exchange exchange) {
        return exchange.getContract().getServiceOperation().getExchangePattern() == ExchangePattern.IN_ONLY;
    }

    private void handleInOnly(final Exchange exchange) throws HandlerException {
        try {
            _producerTemplate.send(_uri, createProcessor(exchange));
        } catch (final CamelExecutionException e) {
            throw new HandlerException(e);
        }
    }
    
    private void handleInOut(final Exchange switchyardExchange) throws HandlerException {
        try {
            final Object payload = sendToCamel(switchyardExchange);
            sendResponseToSwitchyard(switchyardExchange, payload);
        } catch (final CamelExecutionException e) {
            throw new HandlerException(e);
        }
    }
    
    private Object sendToCamel(final Exchange switchyardExchange) {
        final org.apache.camel.Exchange camelExchange = _producerTemplate.request(_uri, createProcessor(switchyardExchange));
        return camelExchange.getOut().getBody();
    }
    
    private void sendResponseToSwitchyard(final Exchange switchyardExchange, final Object payload)
    {
        switchyardExchange.getMessage().setContent(payload);
        switchyardExchange.send(switchyardExchange.getMessage());
    }
    
    private Processor createProcessor(final Exchange switchyardExchange) {
        return new Processor() {
            @Override
            public void process(org.apache.camel.Exchange camelExchange) throws Exception {
                _messageComposer.decompose(switchyardExchange, camelExchange.getIn());
            }
        };
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void handleFault(Exchange exchange) {
        super.handleFault(exchange);
    }

    /**
     * Return the CamelContext used by this handler.
     * @return CamelContext
     */
    public CamelContext getCamelContext() {
        return _camelContext;
    }
    
    /**
     * Return the Camel endpoint URI used by this handler.
     * @return Camel endpoint URI
     */
    public String getUri() {
        return _uri;
    }
    
    /**
     * Returns a String representation of this object instance.
     * 
     * @return {@code String} string representation.
     */
    @Override
    public String toString() {
        return "OutboundHandler [uri=" + _uri + ", producerTemplate=" + _producerTemplate.getClass().getName() + "]";
    }
    
}
