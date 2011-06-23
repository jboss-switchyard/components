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


package org.switchyard.component.camel.config.model.bean.v1;

import java.net.URI;

import org.switchyard.component.camel.config.model.QueryString;
import org.switchyard.component.camel.config.model.bean.CamelBeanBindingModel;
import org.switchyard.component.camel.config.model.v1.NameValueModel;
import org.switchyard.component.camel.config.model.v1.V1BaseCamelBindingModel;
import org.switchyard.config.Configuration;
import org.switchyard.config.model.Descriptor;

/**
 * Implementation of CamelBeanBindingModel.
 */
public class V1CamelBeanBindingModel extends V1BaseCamelBindingModel 
                implements CamelBeanBindingModel {
    
    /**
     * Camel endpoint type.
     */
    public static final String BEAN = "bean";

    /**
     * Camel endpoint configuration values.
     */
    private static final String BEAN_ID                 = "beanID";
    private static final String METHOD                  = "method";
    private static final String CACHE                   = "cache";
    private static final String MULTI_PARAMETER_ARRAY   = "multiParameterArray";
    
    /**
     * Create a new CamelBeanBindingModel.
     */
    public V1CamelBeanBindingModel() {
        super(BEAN);
        setModelChildrenOrder(
                BEAN_ID, 
                METHOD, 
                CACHE, 
                MULTI_PARAMETER_ARRAY);
    }
    
    /**
     * Create a CamelBeanBindingModel from the specified configuration and descriptor.
     * 
     * @param config The switchyard configuration instance.
     * @param desc The switchyard descriptor instance.
     */
    public V1CamelBeanBindingModel(Configuration config, Descriptor desc) {
        super(config, desc);
    }

    @Override
    public String getBeanID() {
        return getConfig(BEAN_ID);
    }

    @Override
    public CamelBeanBindingModel setBeanID(String beanID) {
        setConfig(BEAN_ID, beanID);
        return this;
    }

    @Override
    public String getMethod() {
        return getConfig(METHOD);
    }

    @Override
    public CamelBeanBindingModel setMehod(String method) {
        setConfig(METHOD, method);
        return this;
    }

    @Override
    public Boolean getCache() {
        return getBooleanConfig(CACHE);
    }

    @Override
    public CamelBeanBindingModel setCache(Boolean cache) {
        setConfig(CACHE, String.valueOf(cache));
        return this;
    }

    @Override
    public Boolean getMultiParameterArray() {
        return getBooleanConfig(MULTI_PARAMETER_ARRAY);
    }

    @Override
    public CamelBeanBindingModel setmultiParameterArray(
            Boolean multiParameterArray) {
        setConfig(MULTI_PARAMETER_ARRAY, String.valueOf(multiParameterArray));
        return this;
    }
     
    

    @Override
    public URI getComponentURI() {
        // base URI without params
        String uriStr = BEAN + ":" + getConfig(BEAN_ID);
        // create query string from config values
        QueryString queryStr = new QueryString()
        .add(METHOD, getConfig(METHOD))
        .add(CACHE, getConfig(CACHE))
        .add(MULTI_PARAMETER_ARRAY, getConfig(MULTI_PARAMETER_ARRAY));

        return URI.create(uriStr.toString() + queryStr);
    }
    
    private String getConfig(String configName) {
        Configuration config = getModelConfiguration().getFirstChild(configName);
        if (config != null) {
            return config.getValue();
        } else {
            return null;
        }
    }
    
    private Boolean getBooleanConfig(String configName) {
        String value = getConfig(configName);
        return value != null ? Boolean.valueOf(value) : null;
    }
    
    private void setConfig(String name, String value) {
        Configuration config = getModelConfiguration().getFirstChild(name);
        if (config != null) {
            // set an existing config value
            config.setValue(value);
        } else {
            // create the config model and set the value
            NameValueModel model = new NameValueModel(name);
            model.setValue(value);
            setChildModel(model);
        }
    }
}
