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

package org.switchyard.component.camel.config.model.bean.v1;

import java.io.InputStream;
import java.util.List;

import junit.framework.Assert;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.switchyard.component.camel.config.model.bean.CamelBeanBindingModel;
import org.switchyard.component.camel.config.model.v1.V1CamelBindingModel;
import org.switchyard.config.model.ModelResource;
import org.switchyard.config.model.Validation;
import org.switchyard.config.model.composite.BindingModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;


/**
 * Test for {@link V1CamelBindingModel}.
 *
 * @author Mario Antollini
 */
public class V1CamelBeanBindingModelTest {


    private static final String CAMEL_XML = "switchyard-bean-binding-beans.xml";

    private static final String BEAN_ID                 = "fooBeanID";
    private static final String METHOD                  = "fooMethodName";
    private static final Boolean CACHE                   = Boolean.FALSE;
    private static final Boolean MULTI_PARAMETER_ARRAY   = Boolean.TRUE;
    
    private static final String CAMEL_URI = "bean:fooBeanID?method=fooMethodName&" +
    		"cache=false&multiParameterArray=true";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testConfigOverride() {
        // Set a value on an existing config element
        CamelBeanBindingModel bindingModel = createBeanModel();
        Assert.assertEquals(BEAN_ID, bindingModel.getBeanID());
        bindingModel.setBeanID("newFooBeanName");
        Assert.assertEquals("newFooBeanName", bindingModel.getBeanID());
    }

    @Test
    public void testReadConfig() throws Exception {
        final CamelBeanBindingModel bindingModel = getCamelBinding(CAMEL_XML);
        final Validation validateModel = bindingModel.validateModel();
        //Valid Model?
        Assert.assertEquals(validateModel.isValid(), true);
        //Camel Bean
        Assert.assertEquals(bindingModel.getBeanID(), BEAN_ID);
        Assert.assertEquals(bindingModel.getMethod(), METHOD);
        Assert.assertEquals(bindingModel.getCache(), CACHE);
        Assert.assertEquals(bindingModel.getMultiParameterArray(), MULTI_PARAMETER_ARRAY);
        //URI
        Assert.assertEquals(bindingModel.getComponentURI().toString(), CAMEL_URI);
    }

    @Test
    public void testWriteConfig() throws Exception {
        CamelBeanBindingModel bindingModel = createBeanModel();
        final Validation validateModel = bindingModel.validateModel();
        //Valid Model?
        Assert.assertEquals(validateModel.isValid(), true);
        //Camel Bean
        Assert.assertEquals(bindingModel.getBeanID(), BEAN_ID);
        Assert.assertEquals(bindingModel.getMethod(), METHOD);
        Assert.assertEquals(bindingModel.getCache(), CACHE);
        Assert.assertEquals(bindingModel.getMultiParameterArray(), MULTI_PARAMETER_ARRAY);
        //URI
        Assert.assertEquals(bindingModel.getComponentURI().toString(), CAMEL_URI);
    }

    @Test
    public void compareWriteConfig() throws Exception {
        String refXml = getCamelBinding(CAMEL_XML).toString();
        String newXml = createBeanModel().toString();
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = XMLUnit.compareXML(refXml, newXml);
        Assert.assertTrue(diff.toString(), diff.similar());
    }

    private CamelBeanBindingModel createBeanModel() {
        return new V1CamelBeanBindingModel().setBeanID(BEAN_ID)
        .setMehod(METHOD)
        .setCache(CACHE)
        .setmultiParameterArray(MULTI_PARAMETER_ARRAY);
    }


    private CamelBeanBindingModel getCamelBinding(final String config) throws Exception {
        final InputStream in = getClass().getResourceAsStream(config);
        final SwitchYardModel model = (SwitchYardModel) new ModelResource<SwitchYardModel>().pull(in);
        final List<CompositeServiceModel> services = model.getComposite().getServices();
        final CompositeServiceModel compositeServiceModel = services.get(0);
        final List<BindingModel> bindings = compositeServiceModel.getBindings();
        return (CamelBeanBindingModel) bindings.get(0);
    }

}