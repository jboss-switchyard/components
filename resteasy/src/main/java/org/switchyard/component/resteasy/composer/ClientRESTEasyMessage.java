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
package org.switchyard.component.resteasy.composer;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.switchyard.component.common.rest.RsMethod;

/**
 * A client (request or response) RESTEasy message.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; &copy; 2012 Red Hat Inc.
 */
public class ClientRESTEasyMessage implements RESTEasyMessage {

    private final ClientRequest _clientRequest;
    private final ClientResponse<?> _clientResponse;
    private final RsMethod _rsMethod;

    /**
     * Creates a new request-based client RESTEasy message.
     * @param clientRequest the client request
     * @param rsMethod the REST method
     */
    public ClientRESTEasyMessage(ClientRequest clientRequest, RsMethod rsMethod) {
        _clientRequest = clientRequest;
        _clientResponse = null;
        _rsMethod = rsMethod;
    }

    /**
     * Creates a new response-based client RESTEasy message.
     * @param clientResponse the client response
     * @param rsMethod the REST method
     */
    public ClientRESTEasyMessage(ClientResponse<?> clientResponse, RsMethod rsMethod) {
        _clientRequest = null;
        _clientResponse = clientResponse;
        _rsMethod = rsMethod;
    }

    /**
     * Gets the client request.
     * @return the client request
     */
    public ClientRequest getClientRequest() {
        return _clientRequest;
    }

    /**
     * Gets the client response.
     * @return the client response
     */
    public ClientResponse<?> getClientResponse() {
        return _clientResponse;
    }

    /**
     * Gets the REST method.
     * @return the REST method
     */
    public RsMethod getRsMethod() {
        return _rsMethod;
    }

}
