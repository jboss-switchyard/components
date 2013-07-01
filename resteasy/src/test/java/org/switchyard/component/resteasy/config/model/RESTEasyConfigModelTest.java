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

package org.switchyard.component.resteasy.config.model;

import org.junit.Assert;
import org.junit.Test;
import org.switchyard.component.resteasy.config.model.RESTEasyBindingModel;
import org.switchyard.config.model.ModelPuller;

/**
 * Test of rest binding model.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
public class RESTEasyConfigModelTest {

    private static final String REST_BINDING = "rest-binding.xml";
    private static final String REST_BINDING_INVALID = "rest-binding-invalid.xml";

    @Test
    public void testReadConfigBinding() throws Exception {
        ModelPuller<RESTEasyBindingModel> puller = new ModelPuller<RESTEasyBindingModel>();
        RESTEasyBindingModel model = puller.pull(REST_BINDING, getClass());
        Assert.assertTrue(model.isModelValid());
        model = puller.pull(REST_BINDING_INVALID, getClass());
        Assert.assertFalse(model.isModelValid());
    }
}
