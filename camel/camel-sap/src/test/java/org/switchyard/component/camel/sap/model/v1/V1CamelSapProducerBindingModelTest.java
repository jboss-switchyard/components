/*
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors.
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
package org.switchyard.component.camel.sap.model.v1;

import static junit.framework.Assert.assertEquals;

import org.fusesource.camel.component.sap.SAPEndpoint;
import org.switchyard.component.camel.config.test.v1.V1BaseCamelReferenceBindingModelTest;
import org.switchyard.component.camel.sap.model.CamelSapNamespace;

/**
 * Test for {@link V1CamelSapBindingModel}.
 */
public class V1CamelSapProducerBindingModelTest extends V1BaseCamelReferenceBindingModelTest<V1CamelSapBindingModel, SAPEndpoint> {

    private static final String CAMEL_XML = "/v1/switchyard-sap-producer-binding-beans.xml";
    private static final String CAMEL_URI = "sap:destination:nplHost:BAPI_FLTRIP_GETLIST?transacted=true";
    private static final String DESTINATION = "nplHost";
    private static final String RFC_NAME = "BAPI_FLTRIP_GETLIST";
    private static final Boolean TRANSACTED = Boolean.TRUE;

    public V1CamelSapProducerBindingModelTest() {
        super(SAPEndpoint.class, CAMEL_XML);

        setSkipCamelEndpointTesting(true);
    }

    @Override
    protected V1CamelSapBindingModel createTestModel() {
        return new V1CamelSapBindingModel(CamelSapNamespace.V_1_1.uri()) {
            @Override
            public boolean isReferenceBinding() {
                return true;
            }
        }
        .setDestination(DESTINATION)
        .setRfcName(RFC_NAME)
        .setTransacted(TRANSACTED);
    }

    @Override
    protected void createModelAssertions(V1CamelSapBindingModel model) {
        assertEquals(DESTINATION, model.getDestination());
        assertEquals(RFC_NAME, model.getRfcName());
        assertEquals(TRANSACTED, model.isTransacted());
    }

    @Override
    protected String createEndpointUri() {
        return CAMEL_URI;
    }

}