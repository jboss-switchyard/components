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

package org.switchyard.component.camel.config.model.bean;

import org.switchyard.component.camel.config.model.CamelBindingModel;

/**
 * Represents the configuration settings for a Bean endpoint in Camel. The bean 
 * component binds beans to Camel message exchanges.
 */
public interface CamelBeanBindingModel extends CamelBindingModel {
    
    /**
     * The string which is used to look up the bean in the Registry.
     * @return the string
     */
    String getBeanID();
    
    /**
     * Specify the string to be used to look up the bean in the Registry.
     * @param beanID the string id
     * @return a reference to this Bean binding model
     */
    CamelBeanBindingModel setBeanID(String beanID);
    
    /**
     * The method name from the bean that will be invoked.
     * @return the method name from the bean that will be invoked
     */
    String getMethod();
    
    /**
     * Specify the method name from the bean that will be invoked.
     * @param method the method name
     * @return a reference to this Bean binding model
     */
    CamelBeanBindingModel setMehod(String method);

    /**
     * If enabled, Camel will cache the result of the first Registry look-up. 
     * @return true if enabled, false otherwise
     */
    Boolean getCache();
    
    /**
     * Specify whether Camel is to cache the result of the first Registry look-up. 
     * Cache can be enabled if the bean in the Registry is defined as a 
     * singleton scope.
     * @param cache true if cache is to be enabled, false otherwise
     * @return a reference to this Bean binding model
     */
    CamelBeanBindingModel setCache(Boolean cache);


    /**
     * How to treat the parameters which are passed from the message body; 
     * if it is true, the In message body should be an array of parameters. 
     * @return if true, the In message body should be an array of parameters
     */
    Boolean getMultiParameterArray();
    
    /**
     * How to treat the parameters which are passed from the message body; 
     * if it is true, the In message body should be an array of parameters. 
     * @param multiParameterArray if true, the In message body should be an 
     * array of parameters
     * @return a reference to this Bean binding model
     */
    CamelBeanBindingModel setmultiParameterArray(Boolean multiParameterArray);
    
}
