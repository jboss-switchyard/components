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
package org.switchyard.component.jca.composer;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.switchyard.Exchange;
import org.switchyard.component.common.composer.BaseMessageComposer;
import org.switchyard.exception.SwitchYardException;

/**
 * MessageComposer implementation for JMS Message that is used by JCA component.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; (C) 2011 Red Hat Inc.
 * @author <a href="mailto:tm.igarashi@gmail.com">Tomohisa Igarashi</a>
 */
public class JMSMessageComposer extends BaseMessageComposer<JMSBindingData> {

    /**
     * {@inheritDoc}
     */
    @Override
    public org.switchyard.Message compose(JMSBindingData source, Exchange exchange, boolean create) throws Exception {
        final org.switchyard.Message syMessage = create ? exchange.createMessage() : exchange.getMessage();
        getContextMapper().mapFrom(source, exchange.getContext());

        Message jmsMessage = source.getMessage();
        if (jmsMessage instanceof BytesMessage) {
            BytesMessage sourceBytes = BytesMessage.class.cast(jmsMessage);
            if (sourceBytes.getBodyLength() > Integer.MAX_VALUE) {
                throw new SwitchYardException("The size of message content exceeds "
                        + Integer.MAX_VALUE + " bytes, that is not supported by this MessageComposer");
            }
            byte[] bytearr = new byte[(int)sourceBytes.getBodyLength()];
            sourceBytes.readBytes(bytearr);
            syMessage.setContent(bytearr);

        } else if (jmsMessage instanceof MapMessage) {
            MapMessage sourceMap = MapMessage.class.cast(jmsMessage);
            Map<String,Object> body = new HashMap<String,Object>();
            Enumeration<?> e = sourceMap.getMapNames();
            while (e.hasMoreElements()) {
                String key = String.class.cast(e.nextElement());
                body.put(key, sourceMap.getObject(key));
            }
            syMessage.setContent(body);
            
         } else if (jmsMessage instanceof ObjectMessage) {
             ObjectMessage sourceObj = ObjectMessage.class.cast(jmsMessage);
             syMessage.setContent(sourceObj.getObject());
             
        } else if (jmsMessage instanceof StreamMessage) {
            StreamMessage sourceStream = StreamMessage.class.cast(jmsMessage);
            syMessage.setContent(sourceStream);
            
        } else if (jmsMessage instanceof TextMessage) {
            TextMessage sourceText = TextMessage.class.cast(jmsMessage);
            syMessage.setContent(sourceText.getText());
            
        } else {
            // plain javax.jms.Message doesn't have body content
            syMessage.setContent(null);
        }

        return syMessage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JMSBindingData decompose(Exchange exchange, JMSBindingData target) throws Exception {
        getContextMapper().mapTo(exchange.getContext(), target);
        Message jmsMessage = target.getMessage();
        ObjectMessage targetObj = ObjectMessage.class.cast(jmsMessage);
        // expect transformer to transform the content into Serializable ...
        targetObj.setObject(exchange.getMessage().getContent(Serializable.class));
        return target;
    }

}
