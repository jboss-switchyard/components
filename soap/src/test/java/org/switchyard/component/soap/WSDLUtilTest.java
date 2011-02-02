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

import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;

import org.junit.Assert;
import org.junit.Test;

import org.switchyard.component.soap.util.WSDLUtil;

/**
 * Tests for SOAPGateway's wsPort parameter.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 */
public class WSDLUtilTest {

    @Test
    public void fullyQualifiedPortName() throws Exception {
        Service service = WSDLUtil.getService("target/test-classes/HelloWebService.wsdl", "{http://test.ws/}HelloWebService:HelloWebServicePort");
        Assert.assertNotNull(service);
        Port port = WSDLUtil.getPort(service, "{http://test.ws/}HelloWebService:HelloWebServicePort");
        Assert.assertNotNull(port);
    }
    
    @Test
    public void halfQualifiedPortName() throws Exception {
        Service service = WSDLUtil.getService("target/test-classes/HelloWebService.wsdl", "HelloWebService:HelloWebServicePort");
        Assert.assertNotNull(service);
        Port port = WSDLUtil.getPort(service, "HelloWebService:HelloWebServicePort");
        Assert.assertNotNull(port);
    }
    
    @Test
    public void nonQualifiedPortName() throws Exception {
        Service service = WSDLUtil.getService("target/test-classes/HelloWebService.wsdl", "HelloWebServicePort");
        Assert.assertNotNull(service);
        Port port = WSDLUtil.getPort(service, "HelloWebServicePort");
        Assert.assertNotNull(port);
    }
    
    @Test
    public void nullPortName() throws Exception {
        Service service = WSDLUtil.getService("target/test-classes/HelloWebService.wsdl", null);
        Assert.assertNotNull(service);
        Port port = WSDLUtil.getPort(service, null);
        Assert.assertNotNull(port);
    }
    
    @Test(expected=WSDLException.class)
    public void nonExistentService() throws Exception {
        Service service = WSDLUtil.getService("target/test-classes/HelloWebService.wsdl", "GoodbyeWebService:HelloWebServiceSOAPPort");
    }
    
    @Test(expected=WSDLException.class)
    public void nonExistentPortName() throws Exception {
        Service service = WSDLUtil.getService("target/test-classes/HelloWebService.wsdl", "HelloWebService:");
        Assert.assertNotNull(service);
        Port port = WSDLUtil.getPort(service, "HelloWebServicePort1.2");
    }
}
