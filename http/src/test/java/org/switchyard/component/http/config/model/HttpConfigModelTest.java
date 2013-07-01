/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
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

package org.switchyard.component.http.config.model;

import org.junit.Assert;
import org.junit.Test;
import org.switchyard.component.http.config.model.BasicAuthModel;
import org.switchyard.component.http.config.model.HttpBindingModel;
import org.switchyard.component.http.config.model.ProxyModel;
import org.switchyard.config.model.ModelPuller;

/**
 * Test of HTTP binding model.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
public class HttpConfigModelTest {

    private static final String HTTP_BINDING = "http-binding.xml";
    private static final String HTTP_BINDING2 = "http-binding2.xml";
    private static final String HTTP_BINDING_AUTH = "http-binding-auth.xml";
    private static final String HTTP_BINDING_PROXY = "http-binding-proxy.xml";

    @Test
    public void testReadConfigBinding() throws Exception {
        ModelPuller<HttpBindingModel> puller = new ModelPuller<HttpBindingModel>();
        HttpBindingModel model = puller.pull(HTTP_BINDING, getClass());
        Assert.assertTrue(model.isModelValid());
        model = puller.pull(HTTP_BINDING2, getClass());
        Assert.assertTrue(model.isModelValid());
    }

    @Test
    public void authConfigBinding() throws Exception {
        ModelPuller<HttpBindingModel> puller = new ModelPuller<HttpBindingModel>();
        HttpBindingModel model = puller.pull(HTTP_BINDING_AUTH, getClass());
        Assert.assertTrue(model.isModelValid());
        Assert.assertTrue(model.isBasicAuth());
        BasicAuthModel authConfig = model.getBasicAuthConfig();
        Assert.assertNotNull(authConfig);
        Assert.assertEquals("Beal", authConfig.getUser());
        Assert.assertEquals("conjecture", authConfig.getPassword());
        Assert.assertEquals("Any", authConfig.getRealm());
    }

    @Test
    public void proxyConfigBinding() throws Exception {
        ModelPuller<HttpBindingModel> puller = new ModelPuller<HttpBindingModel>();
        HttpBindingModel model = puller.pull(HTTP_BINDING_PROXY, getClass());
        Assert.assertTrue(model.isModelValid());
        ProxyModel proxyConfig = model.getProxyConfig();
        Assert.assertNotNull(proxyConfig);
        Assert.assertEquals("host", proxyConfig.getHost());
        Assert.assertEquals("8090", proxyConfig.getPort());
        Assert.assertEquals("Beal", proxyConfig.getUser());
        Assert.assertEquals("conjecture", proxyConfig.getPassword());
    }
}
