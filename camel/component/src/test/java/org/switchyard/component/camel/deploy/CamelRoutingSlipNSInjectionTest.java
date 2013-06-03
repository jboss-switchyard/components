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
package org.switchyard.component.camel.deploy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RecipientListDefinition;
import org.apache.camel.model.RouteDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.common.camel.SwitchYardCamelContext;
import org.switchyard.component.test.mixins.cdi.CDIMixIn;
import org.switchyard.test.Invoker;
import org.switchyard.test.ServiceOperation;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;

/**
 * @author Ashwin Karpe &lt;<a href="mailto:akarpe@jboss.org">akarpe@jboss.org</a>&gt; (C) 2011 Red Hat Inc.
 */
@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(config = "switchyard-routingslip-ns-injection-test.xml", mixins = CDIMixIn.class)
public class CamelRoutingSlipNSInjectionTest {
    private final static String MATH_SERVICE = "CamelMathService";

    private SwitchYardCamelContext _camelContext;

    @ServiceOperation(MATH_SERVICE)
    private Invoker invoker;

    @Before
    public void verifyContext() {
        assertNotNull(_camelContext.getServiceDomain());
    }

    @Test
    public void autoInjectedNamespaceinReceipientList() throws Exception {
        String uri = null;
        
        // Route is already instantiated at this point and the camel context 
        // should contain the namespace updated route definition
        RouteDefinition routeDefinition = _camelContext.getRouteDefinition("CamelMathRoute");
        for (ProcessorDefinition<?> processorDefinition : routeDefinition.getOutputs()) {
            if (processorDefinition instanceof RecipientListDefinition) {
                uri = ((RecipientListDefinition)processorDefinition).getExpression().getExpression();
            }
        }
        
        assertEquals("switchyard://MathAll?namespace=urn:camel-core:test:1.0", uri);
        
    }
}
