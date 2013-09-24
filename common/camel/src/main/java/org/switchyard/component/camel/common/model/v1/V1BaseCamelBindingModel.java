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
package org.switchyard.component.camel.common.model.v1;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.camel.util.UnsafeUriCharactersEncoder;
import org.switchyard.component.camel.CommonCamelMessages;
import org.switchyard.component.camel.common.QueryString;
import org.switchyard.component.camel.common.model.CamelBindingModel;
import org.switchyard.config.Configuration;
import org.switchyard.config.Configurations;
import org.switchyard.config.model.Descriptor;
import org.switchyard.config.model.Model;
import org.switchyard.config.model.composite.v1.V1BindingModel;

/**
 * Version 1.0 implementation of a {@link CamelBindingModel}.
 * 
 * @author Daniel Bevenius
 */
public abstract class V1BaseCamelBindingModel extends V1BindingModel
    implements CamelBindingModel {

    /**
     * Camel endpoint type.
     */
    public static final String CAMEL = "camel";

    private Configuration _environment = Configurations.newConfiguration();

    /**
     * 
     * Create a new CamelBindingModel.
     * 
     * @param type Binding type
     * @param namespace Namespace name
     */
    public V1BaseCamelBindingModel(String type, String namespace) {
        super(type, namespace);
    }

    /**
     * Create a CamelBindingModel from the specified configuration and
     * descriptor.
     * 
     * @param config The switchyard configuration instance.
     * @param desc The switchyard descriptor instance.
     */
    public V1BaseCamelBindingModel(Configuration config, Descriptor desc) {
        super(config, desc);
    }

    /**
     * Returns the global configuration.
     * 
     * @return the environment/global config
     */
    public Configuration getEnvironment() {
        return _environment;
    }

    /**
     * Sets the global configuration.
     * 
     * @param config the environment/global config
     */
    public void setEnvironment(Configuration config) {
        _environment = config;
    }

    protected final void traverseConfiguration(List<Configuration> parent, QueryString queryString,
        String ... excludes) {

        if (parent.size() != 0) {
            List<String> excludeParameters = new ArrayList<String>(Arrays.asList(excludes));

            Iterator<Configuration> parentIterator = parent.iterator();
            while (parentIterator.hasNext()) {
                Configuration child = parentIterator.next();

                if (child != null && child.getName() != null && excludeParameters.contains(child.getName())) {
                    continue;
                }

                if (child != null && child.getChildren().size() == 0) {
                    queryString.add(child.getName(), UnsafeUriCharactersEncoder.encode(child.getValue()));
                } else {
                    traverseConfiguration(child.getChildren(), queryString, excludes);
                }
            }
        }
    }

    protected <T extends Model> T replaceChildren(String children, T model) {
        Configuration config = getModelConfiguration().getFirstChild(children);
        if (config != null) {
            // set an existing config value
            getModelConfiguration().removeChildren(children);
            getModelConfiguration().addChild(model.getModelConfiguration());
        } else {
            setChildModel(model);
        }
        return model;
    }

    protected Configuration getFirstChild(String name) {
        return getModelConfiguration().getFirstChild(name);
    }

    protected final Integer getIntegerConfig(String configName) {
        String value = getConfig(configName);
        return value != null ? Integer.parseInt(value) : null;
    }

    protected final Boolean getBooleanConfig(String configName) {
        String value = getConfig(configName);
        return value != null ? Boolean.valueOf(value) : null;
    }

    protected final Long getLongConfig(String configName) {
        String value = getConfig(configName);
        return value != null ? Long.parseLong(value) : null;
    }

    protected final Date getDateConfig(String configName, DateFormat format) {
        String value = getConfig(configName);
        if (value == null) {
            return null;
        } else {
            try {
                return format.parse(value);
            } catch (java.text.ParseException parseEx) {
                throw CommonCamelMessages.MESSAGES.failedToParse(configName, parseEx);
            }
        }
    }

    protected final String getConfig(String configName) {
        Configuration config = getModelConfiguration().getFirstChild(configName);
        if (config != null) {
            return config.getValue();
        } else {
            return null;
        }
    }

    protected final <T extends Enum<T>> T getEnumerationConfig(String configName, Class<T> type) {
        String constantName = getConfig(configName);
        if (constantName != null) {
            return Enum.valueOf(type, constantName);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected <X extends V1BaseCamelBindingModel> X setConfig(String name, Object value) {
        String modelValue = String.valueOf(value);
        Configuration config = getModelConfiguration().getFirstChild(name);
        if (config != null) {
            // set an existing config value
            config.setValue(modelValue);
        } else {
            // create the config model and set the value
            NameValueModel model = new NameValueModel(getNamespaceURI(), name);
            model.setValue(modelValue);
            setChildModel(model);
        }
        return (X) this;
    }

}
