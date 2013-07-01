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
package org.switchyard.component.soap.config.model;

import junit.framework.Assert;

import org.junit.Test;
import org.switchyard.config.model.ModelPuller;

/**
 * Test of SOAP binding model.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
public class SOAPBindingModelTest {

    private static final String SOAP_BINDING_SERVICE = "soap-binding-service.xml";
    private static final String SOAP_BINDING_REFERENCE = "soap-binding-reference.xml";
    private static final String SOAP_BINDING_INVALID = "soap-binding-invalid.xml";

    @Test
    public void serviceBinding() throws Exception {
        ModelPuller<SOAPBindingModel> puller = new ModelPuller<SOAPBindingModel>();
        SOAPBindingModel binding = puller.pull(SOAP_BINDING_SERVICE, getClass());
        binding.assertModelValid();
        Assert.assertEquals("myendpoint", binding.getContextPath());
        EndpointConfigModel endpointConfig = binding.getEndpointConfig();
        Assert.assertEquals("myFile", endpointConfig.getConfigFile());
        Assert.assertEquals("myName", endpointConfig.getConfigName());
        MtomModel mtomConfig = binding.getMtomConfig();
        Assert.assertEquals(new Boolean(true), mtomConfig.isEnabled());
        Assert.assertEquals(new Integer(1300), mtomConfig.getThreshold());
        Assert.assertEquals(new Boolean(true), mtomConfig.isXopExpand());
    }

    @Test
    public void referenceBinding() throws Exception {
        ModelPuller<SOAPBindingModel> puller = new ModelPuller<SOAPBindingModel>();
        SOAPBindingModel binding = puller.pull(SOAP_BINDING_REFERENCE, getClass());
        binding.assertModelValid();
        Assert.assertEquals("http://modified.com/phantom", binding.getEndpointAddress());
        MtomModel mtomConfig = binding.getMtomConfig();
        Assert.assertEquals(new Boolean(true), mtomConfig.isEnabled());
        Assert.assertEquals(new Integer(1300), mtomConfig.getThreshold());
        Assert.assertEquals(new Boolean(true), mtomConfig.isXopExpand());
    }

    @Test
    public void invalidBinding() throws Exception {
        ModelPuller<SOAPBindingModel> puller = new ModelPuller<SOAPBindingModel>();
        SOAPBindingModel binding = puller.pull(SOAP_BINDING_INVALID, getClass());
        Assert.assertTrue(!binding.isModelValid());
    }
}
