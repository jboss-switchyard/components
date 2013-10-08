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
package org.switchyard.component.camel.quartz.model.v1;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import org.apache.camel.component.quartz.QuartzEndpoint;
import org.switchyard.component.camel.config.test.v1.V1BaseCamelServiceBindingModelTest;
import org.switchyard.component.camel.quartz.Constants;

/**
 * Test for {@link V1CamelQuartzBindingModel}.
 * Tests for the conditions in SWITCHYARD-1760, due to host parsing in the Quartz
 * framework, the targetName cannot contain underscores.   
 *
 * @author Tom Cunningham
 */
public class V1CamelQuartzNameSubstitutionTest extends V1BaseCamelServiceBindingModelTest<V1CamelQuartzBindingModel, QuartzEndpoint> {

    private static final String CAMEL_XML = "/v1/switchyard-quartz-name-substitution.xml";

    // Used for dateTime fields
    private static DateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private static final String NAME = "My_Job";
    private static final String CRON = "0 0 12 * * ?";
    private static final Boolean STATEFUL = true;
    private static Date START_TIME;
    private static Date END_TIME;
    private static String TIMEZONE;

    private static final String CAMEL_URI = "quartz://My-Job?cron=0 0 12 * * ?&stateful=true" +
        "&trigger.startTime=2011-01-01T12:00:00&trigger.endTime=2011-01-01T12:00:00&trigger.timeZone=America/New_York";

    static {
        try {
            START_TIME = _dateFormat.parse("2011-01-01T12:00:00");
            END_TIME = _dateFormat.parse("2011-01-01T12:00:00");
            TIMEZONE = "America/New_York";
        } catch (Exception e) { /* ignore */ }
    }

    public V1CamelQuartzNameSubstitutionTest() {
        super(QuartzEndpoint.class, CAMEL_XML);

        setSkipCamelEndpointTesting(true);
    }

    @Override
    protected V1CamelQuartzBindingModel createTestModel() {
        final V1CamelQuartzBindingModel model = new V1CamelQuartzBindingModel();
        model.setAdditionalUriParameters(createAdditionalUriParametersModel(Constants.QUARTZ_NAMESPACE_V1, Collections.singletonMap("trigger.timeZone", "GMT")));
        model
        .setTimerName(NAME)
        .setCron(CRON)
        .setStateful(STATEFUL)
        .setStartTime(START_TIME)
        .setEndTime(END_TIME)
        .setTimeZone(TIMEZONE);
        return (V1CamelQuartzBindingModel) model;
    }

    @Override
    protected void createModelAssertions(V1CamelQuartzBindingModel model) {
        assertTrue(model.getTimerName().contains("_"));
        assertTrue(model.getTimerName().equals(NAME));
        
        assertEquals(CRON, model.getCron());
        assertEquals(STATEFUL, model.isStateful());
        assertEquals(START_TIME, model.getStartTime());
        assertEquals(END_TIME, model.getEndTime());
        assertEquals(TIMEZONE, model.getTimeZone());
    }

    @Override
    protected String createEndpointUri() {
        return CAMEL_URI;
    }

}
