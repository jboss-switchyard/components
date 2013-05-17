/*
/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.switchyard.component.jca.deploy;

import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import javax.transaction.UserTransaction;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.component.test.mixins.cdi.CDIMixIn;
import org.switchyard.component.test.mixins.hornetq.HornetQMixIn;
import org.switchyard.component.test.mixins.jca.JCAMixIn;
import org.switchyard.component.test.mixins.jca.ResourceAdapterConfig;
import org.switchyard.test.BeforeDeploy;
import org.switchyard.test.Invoker;
import org.switchyard.test.ServiceOperation;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;

/**
 * Functional test for {@link JCAActivator}.
 * 
 * @author <a href="mailto:tm.igarashi@gmail.com">Tomohisa Igarashi</a>
 *
 */
@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(config = "switchyard-outbound-jms-test.xml", mixins = {HornetQMixIn.class, JCAMixIn.class, CDIMixIn.class})
public class JCAJMSReferenceBindingTest  {
    
    private static final String OUTPUT_QUEUE = "TestQueue";
    private HornetQMixIn _hqMixIn;
    private JCAMixIn _jcaMixIn;
    
    @ServiceOperation("JCAJMSReferenceService.onMessage")
    private Invoker _service;

    @ServiceOperation("JCAJMSReferenceService.onMessageText")
    private Invoker _serviceText;
    
    @BeforeDeploy
    public void before() {
        ResourceAdapterConfig ra = new ResourceAdapterConfig(ResourceAdapterConfig.ResourceAdapterType.HORNETQ);
        _jcaMixIn.deployResourceAdapters(ra);
    }
    
    @Test
    public void testUnmanagedOutboundJMS() throws Exception {
        String payload = "Karakoromo";
        _service.sendInOnly(payload);
        
        final MessageConsumer consumer = _hqMixIn.getJMSSession().createConsumer(HornetQMixIn.getJMSQueue(OUTPUT_QUEUE));
        javax.jms.Message msg = consumer.receive(1000);
        _hqMixIn.readJMSMessageAndTestString(msg, payload+"test");
        Assert.assertEquals(msg.getStringProperty("testProp"), "testVal");
        
    }
    
    @Test
    public void testUnmanagedOutboundJMSSpecifyingMessageType() throws Exception {
        String payload = "Shikishima";
        _serviceText.sendInOnly(payload);
        
        final MessageConsumer consumer = _hqMixIn.getJMSSession().createConsumer(HornetQMixIn.getJMSQueue(OUTPUT_QUEUE));
        javax.jms.Message msg = consumer.receive(1000);
        Assert.assertTrue(msg instanceof TextMessage);
        javax.jms.TextMessage txtmsg = TextMessage.class.cast(msg);
        Assert.assertEquals(payload+"test", txtmsg.getText());
        Assert.assertEquals(msg.getStringProperty("testProp"), "testVal");
        
    }
    
    @Test
    public void testManagedOutboundJMS() throws Exception {
        String payload = "Ashihiki";
        UserTransaction tx = _jcaMixIn.getUserTransaction();
        tx.begin();
        _service.sendInOnly(payload);
        
        final MessageConsumer consumer = _hqMixIn.getJMSSession().createConsumer(HornetQMixIn.getJMSQueue(OUTPUT_QUEUE));
        Assert.assertNull(consumer.receive(1000));
        
        tx.commit();
        
        javax.jms.Message msg = consumer.receive(1000);
        _hqMixIn.readJMSMessageAndTestString(msg, payload+"test");
        Assert.assertEquals(msg.getStringProperty("testProp"), "testVal");
    }
}

