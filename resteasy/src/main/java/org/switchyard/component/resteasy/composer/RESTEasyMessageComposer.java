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

import java.io.StringWriter;
import java.io.Writer;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.switchyard.Exchange;
import org.switchyard.Message;
import org.switchyard.component.common.composer.BaseMessageComposer;
import org.switchyard.component.common.rest.RsMethod;

/**
 * Composes/decomposes RESTEasy messages.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; &copy; 2012 Red Hat Inc.
 */
public class RESTEasyMessageComposer extends BaseMessageComposer<RESTEasyMessage> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Message compose(RESTEasyMessage source, Exchange exchange, boolean create) throws Exception {
        final Message message = create ? exchange.createMessage() : exchange.getMessage();
        getContextMapper().mapFrom(source, exchange.getContext());
        if (source instanceof ContentRESTEasyMessage) {
            ContentRESTEasyMessage contentMessage = (ContentRESTEasyMessage)source;
            message.setContent(contentMessage.getContent());
        } else if (source instanceof ClientRESTEasyMessage) {
            ClientRESTEasyMessage clientMessage = (ClientRESTEasyMessage)source;
            ClientResponse<?> clientResponse = clientMessage.getClientResponse();
            if (clientResponse != null) {
                message.setContent(clientResponse.getEntity());
            }
        }
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RESTEasyMessage decompose(Exchange exchange, RESTEasyMessage target) throws Exception {
        getContextMapper().mapTo(exchange.getContext(), target);
        if (target instanceof ContentRESTEasyMessage) {
            ContentRESTEasyMessage contentMessage = (ContentRESTEasyMessage)target;
            contentMessage.setContent(exchange.getMessage().getContent());
        } else if (target instanceof ClientRESTEasyMessage) {
            ClientRESTEasyMessage clientMessage = (ClientRESTEasyMessage)target;
            ClientRequest clientRequest = clientMessage.getClientRequest();
            if (clientRequest != null) {
                RsMethod rsMethod = clientMessage.getRsMethod();
                Object content = exchange.getMessage().getContent();
                if ((rsMethod.getRequestType() != null) && (content != null) && !rsMethod.hasParam()) {
                    // Factor based on media type
                    if (rsMethod.getConsumes().contains(MediaType.TEXT_PLAIN_TYPE)) {
                        clientRequest.body(MediaType.TEXT_PLAIN, content);
                    } else if (rsMethod.getConsumes().contains(MediaType.APPLICATION_XML_TYPE) || rsMethod.getConsumes().contains(MediaType.WILDCARD_TYPE)) {
                        JAXBContext jaxbContext = JAXBContext.newInstance(rsMethod.getRequestType());
                        Marshaller marshaller = jaxbContext.createMarshaller();
                        Writer sw = new StringWriter();
                        marshaller.marshal(content, sw);
                        clientRequest.body(MediaType.APPLICATION_XML, sw.toString());
                    } else if (rsMethod.getConsumes().contains(MediaType.TEXT_XML_TYPE)) {
                        JAXBContext jaxbContext = JAXBContext.newInstance(rsMethod.getRequestType());
                        Marshaller marshaller = jaxbContext.createMarshaller();
                        Writer sw = new StringWriter();
                        marshaller.marshal(content, sw);
                        clientRequest.body(MediaType.TEXT_XML, sw.toString());
                    } else if (rsMethod.getConsumes().contains(MediaType.APPLICATION_JSON_TYPE)) {
                        ObjectMapper mapper = new ObjectMapper();
                        Writer sw = new StringWriter();
                        mapper.writeValue(sw, content);
                        clientRequest.body(MediaType.APPLICATION_JSON, sw.toString());
                    }
                    // Other types coming soon
                } else if (rsMethod.hasQueryParam()) {
                    clientRequest.queryParameter(rsMethod.getParamName(), content);
                } else if (rsMethod.hasPathParam()) {
                    clientRequest.pathParameters(content);
                } else if (rsMethod.hasMatrixParam()) {
                    clientRequest.matrixParameter(rsMethod.getParamName(), content);
                }
            }
        }
        return target;
    }

}
