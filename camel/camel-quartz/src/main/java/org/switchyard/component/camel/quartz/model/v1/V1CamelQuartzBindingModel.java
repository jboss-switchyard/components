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

import static org.switchyard.component.camel.quartz.Constants.QUARTZ_NAMESPACE_V1;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.camel.util.UnsafeUriCharactersEncoder;
import org.switchyard.component.camel.common.QueryString;
import org.switchyard.component.camel.common.model.v1.V1BaseCamelBindingModel;
import org.switchyard.component.camel.quartz.model.CamelQuartzBindingModel;
import org.switchyard.config.Configuration;
import org.switchyard.config.model.Descriptor;

/**
 * Configuration binding for quartz.
 * 
 * @author Lukasz Dywicki
 */
public class V1CamelQuartzBindingModel extends V1BaseCamelBindingModel
    implements CamelQuartzBindingModel {

    private static final String NAME = "name";
    private static final String CRON = "cron";
    private static final String STATEFUL = "stateful";
    private static final String START_TIME = "trigger.startTime";
    private static final String END_TIME = "trigger.endTime";
    private static final String TIMEZONE = "trigger.timeZone";

    // Used for dateTime fields
    private static DateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Create a new CamelDirectBindingModel.
     */
    public V1CamelQuartzBindingModel() {
        super(QUARTZ, QUARTZ_NAMESPACE_V1);

        setModelChildrenOrder(NAME, CRON, STATEFUL, START_TIME, END_TIME, TIMEZONE);
    }

    /**
     * Create a V1CamelQuartzBindingModel from the specified configuration and descriptor.
     * 
     * @param config The switchyard configuration instance.
     * @param desc The switchyard descriptor instance.
     */
    public V1CamelQuartzBindingModel(Configuration config, Descriptor desc) {
        super(config, desc);
    }

    @Override
    public String getTimerName() {
        // The underbar character is not a valid character 
        return getConfig(NAME);
    }

    @Override
    public V1CamelQuartzBindingModel setTimerName(String name) {
        return setConfig(NAME, name);
    }

    @Override
    public String getCron() {
        return getConfig(CRON);
    }

    @Override
    public V1CamelQuartzBindingModel setCron(String cron) {
        return setConfig(CRON, cron);
    }

    @Override
    public Boolean isStateful() {
        return getBooleanConfig(STATEFUL);
    }

    @Override
    public V1CamelQuartzBindingModel setStateful(Boolean stateful) {
        return setConfig(STATEFUL, stateful);
    }

    @Override
    public Date getStartTime() {
        return getDateConfig(START_TIME, _dateFormat);
    }

    @Override
    public V1CamelQuartzBindingModel setStartTime(Date startTime) {
        return setConfig(START_TIME, _dateFormat.format(startTime));
    }

    @Override
    public Date getEndTime() {
        return getDateConfig(END_TIME, _dateFormat);
    }

    @Override
    public V1CamelQuartzBindingModel setEndTime(Date endTime) {
        return setConfig(END_TIME, _dateFormat.format(endTime));
    }

    @Override
    public String getTimeZone() {
        return getConfig(TIMEZONE);
    }

    @Override
    public V1CamelQuartzBindingModel setTimeZone(String timeZone) {
        return setConfig(TIMEZONE, timeZone);
    }
    
    @Override
    public URI getComponentURI() {
        Configuration modelConfiguration = getModelConfiguration();
        List<Configuration> children = modelConfiguration.getChildren();

        // SWITCHYARD-1760 : eliminate underscores in the TimerName part of
        // baseUri to prevent parsing mixups in QuartzComponent
        String baseUri = QUARTZ + "://" + getTimerName().replaceAll("_", "-");

        QueryString queryStr = new QueryString();
        traverseConfiguration(children, queryStr, NAME);

        return URI.create(UnsafeUriCharactersEncoder.encode(baseUri + queryStr.toString()));
    }

}
