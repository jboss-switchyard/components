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

import java.util.HashMap;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.switchyard.ServiceDomain;
import org.switchyard.internal.ServiceDomains;

/**
 * SOAP Gateway acts as an adapter to expose SwitchYard services as a Webservice
 * and also to invoke other Webservices.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 */
public class SOAPGateway {
    private static final Logger LOGGER = Logger.getLogger(SOAPGateway.class);

    private InboundHandler _wsProvider;
    private OutboundHandler _wsConsumer;
    private ServiceDomain _domain;

    /**
     * Constructor.
     */
    public SOAPGateway() {
    }

    /**
     * Initialization code.
     * @param config the configuration settings
     */
    public void init(final HashMap<String, String> config) {
        _domain = ServiceDomains.getDomain();
        String publishAsWS = config.get("publishAsWS");
        if (publishAsWS != null && publishAsWS.equals("true")) {
            // Consume the SwitchYard service
            _wsProvider = new InboundHandler(config);
        } else {
            // Provide the SwitchYard service
            String localService = config.get("serviceName");
            QName serviceName = new QName(localService);
            // Create a WS Client for our service
            _wsConsumer = new OutboundHandler(config);
            _domain.registerService(serviceName, _wsConsumer);
        }
    }

    /**
     * Start lifecycle.
     */
    public void start() {
        if (_wsProvider != null) {
            try {
                _wsProvider.start();
            } catch (Exception e) {
                LOGGER.error(e);
                throw new RuntimeException("WebService could not be published!");
            }
        }
        if (_wsConsumer != null) {
            _wsConsumer.start();
        }
    }

    /**
     * Stop lifecycle.
     */
    public void stop() {
        if (_wsProvider != null) {
            _wsProvider.stop();
        }
        if (_wsConsumer != null) {
            _wsConsumer.stop();
        }
    }

    /**
     * Destroy lifecycle.
     */
    public void destroy() {
    }
}
