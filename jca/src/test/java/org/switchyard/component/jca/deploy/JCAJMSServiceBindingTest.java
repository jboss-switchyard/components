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

import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.Exchange;
import org.switchyard.HandlerException;
import org.switchyard.test.BeforeDeploy;
import org.switchyard.test.MockHandler;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;
import org.switchyard.test.SwitchYardTestKit;
import org.switchyard.component.test.mixins.cdi.CDIMixIn;
import org.switchyard.component.test.mixins.hornetq.HornetQMixIn;
import org.switchyard.component.test.mixins.jca.JCAMixIn;
import org.switchyard.component.test.mixins.jca.ResourceAdapterConfig;

/**
 * Functional test for {@link JCAActivator}.
 * 
 * @author <a href="mailto:tm.igarashi@gmail.com">Tomohisa Igarashi</a>
 *
 */
@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(config = "switchyard-inbound-jms-test.xml", mixins = {CDIMixIn.class, JCAMixIn.class, HornetQMixIn.class})
public class JCAJMSServiceBindingTest  {
    
    private static final String INPUT_QUEUE = "TestQueue";
    private SwitchYardTestKit _testKit;
    private HornetQMixIn _hqMixIn;
    private JCAMixIn _jcaMixIn;
    
    @BeforeDeploy
    public void before() {
        ResourceAdapterConfig ra = new ResourceAdapterConfig(ResourceAdapterConfig.ResourceAdapterType.HORNETQ);
        _jcaMixIn.deployResourceAdapters(ra);
    }
    
    @Test
    public void testInflowJMS() throws Exception {
        _testKit.removeService("JCAJMSService");
        final MockHandler mockHandler = _testKit.registerInOnlyService("JCAJMSService");
        
        final MessageProducer producer = _hqMixIn.getJMSSession().createProducer(HornetQMixIn.getJMSQueue(INPUT_QUEUE));
        TextMessage msg = _hqMixIn.getJMSSession().createTextMessage();
        msg.setText("payload");
        producer.send(msg);
        
        // wait for message to be picked up the HornetQ queue.
        mockHandler.waitForOKMessage();
        
        Assert.assertEquals(1, mockHandler.getMessages().size());
        Exchange exchange = mockHandler.getMessages().poll();
        final Object content = exchange.getMessage().getContent();
        Assert.assertTrue(content instanceof String);
        final String string = (String) content;
        Assert.assertEquals(string, "payloadtest");
        final String val = exchange.getContext().getProperty("testProp").getValue().toString();
        Assert.assertEquals(val, "testVal");
    }
    
    @Test
    public void testInflowJMS_fault_rollback() throws Exception {
        _testKit.removeService("JCAJMSService");
        final MockHandler mockHandler = new MockHandler() {
            private int count = 0;
            public void handleMessage(final Exchange exchange) throws HandlerException {
                if (count < 4) {
                    count++;
                    handleFault(exchange);
                    throw new HandlerException("faultmessage");
                } else {
                    super.handleMessage(exchange);
                }
            }
        };
        _testKit.registerInOnlyService("JCAJMSService", mockHandler);
        
        final MessageProducer producer = _hqMixIn.getJMSSession().createProducer(HornetQMixIn.getJMSQueue(INPUT_QUEUE));
        TextMessage msg = _hqMixIn.getJMSSession().createTextMessage();
        msg.setText("payload");
        producer.send(msg);
        
        // wait for message to be picked up the HornetQ queue.
        mockHandler.waitForOKMessage();
        
        Assert.assertEquals(4, mockHandler.getFaults().size());
        Exchange faultExchange = mockHandler.getFaults().poll();
        final Object faultContent = faultExchange.getMessage().getContent();
        Assert.assertTrue(faultContent instanceof HandlerException);
        Assert.assertEquals("faultmessage", HandlerException.class.cast(faultContent).getMessage());
        Assert.assertEquals(1, mockHandler.getMessages().size());
        Exchange exchange = mockHandler.getMessages().poll();
        final Object content = exchange.getMessage().getContent();
        Assert.assertTrue(content instanceof String);
        Assert.assertEquals("payloadtest", content);
    }

    @Test
    public void testInflowJMS_fault_commit() throws Exception {
        _testKit.removeService("JCAJMSService");
        final MockHandler mockHandler = new MockHandler() {
            private int count = 0;
            public void handleMessage(final Exchange exchange) throws HandlerException {
                if (count < 4) {
                    count++;
                    handleFault(exchange);
                    exchange.sendFault(exchange.createMessage().setContent(new JCAJMSFault("faultmessage")));
                } else {
                    super.handleMessage(exchange);
                }
            }
        };
        _testKit.registerInOnlyService("JCAJMSService", mockHandler);
        
        final MessageProducer producer = _hqMixIn.getJMSSession().createProducer(HornetQMixIn.getJMSQueue(INPUT_QUEUE));
        TextMessage msg = _hqMixIn.getJMSSession().createTextMessage();
        msg.setText("payload");
        producer.send(msg);
        
        // wait for message to be picked up the HornetQ queue.
        mockHandler.waitForFaultMessage();
        
        Assert.assertEquals(1, mockHandler.getFaults().size());
        Exchange exchange = mockHandler.getFaults().poll();
        final Object content = exchange.getMessage().getContent();
        Assert.assertTrue(content instanceof JCAJMSFault);
        Assert.assertEquals("faultmessage", JCAJMSFault.class.cast(content).getMessage());
    }
}

