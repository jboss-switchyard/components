/*
 * 2012 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
        Assert.assertEquals("testVal", msg.getStringProperty("testProp"));
        
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
        Assert.assertEquals("testVal", msg.getStringProperty("testProp"));
        
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
        Assert.assertEquals("testVal", msg.getStringProperty("testProp"));
    }
}

