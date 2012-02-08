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
package org.switchyard.component.camel.deploy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.xml.namespace.QName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.ServiceReference;
import org.switchyard.deploy.internal.Deployment;
import org.switchyard.test.Invoker;
import org.switchyard.test.ServiceOperation;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;
import org.switchyard.test.SwitchYardTestKit;
import org.switchyard.test.mixins.CDIMixIn;

/**
 * Test for {@link CamelActivator}.
 * 
 * @author Daniel Bevenius
 * 
 */
@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(config = "switchyard-activator-impl.xml", mixins = CDIMixIn.class)
public class CamelImplementationTest {

    @ServiceOperation("OrderService.getTitleForItem")
    private Invoker _getTitleForItem;
    
    private SwitchYardTestKit _testKit;
    private Deployment _deployment;

    @Test
    public void sendOneWayMessageThroughCamelToSwitchYardService() throws Exception {
        final String title = _getTitleForItem.sendInOut("10").getContent(String.class);
        assertThat(title, is(equalTo("Fletch")));
    }
    
    @Test
    public void verifyThatOutboundHandlersAreStopped() {
        final QName serviceName = new QName("urn:camel-core:test:1.0", "OutputService");
        final CamelActivator camelActivator = getCamelActivator();
        assertThat(camelActivator.getOutboundHandlersForService(serviceName).size(), is(1));
        camelActivator.stop(getServiceRef(serviceName));
        camelActivator.destroy(getServiceRef(serviceName));
        assertThat(camelActivator.getOutboundHandlersForService(serviceName).size(), is(0));
    }
    
    private CamelActivator getCamelActivator() {
        return (CamelActivator) _deployment.findActivator("camel");
    }
    
    private ServiceReference getServiceRef(final QName serviceName) {
        return _testKit.getServiceDomain().getService(serviceName);
    }
}
