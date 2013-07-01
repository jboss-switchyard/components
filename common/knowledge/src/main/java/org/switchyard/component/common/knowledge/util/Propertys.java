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
package org.switchyard.component.common.knowledge.util;

import java.util.Properties;

import org.switchyard.component.common.knowledge.config.model.KnowledgeComponentImplementationModel;
import org.switchyard.config.model.property.PropertiesModel;

/**
 * Property functions.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; &copy; 2012 Red Hat Inc.
 */
public final class Propertys {

    /**
     * Gets properties.
     * @param model the model
     * @param overrides any overrides
     * @return the properties
     */
    public static Properties getProperties(KnowledgeComponentImplementationModel model, Properties overrides) {
        Properties properties = new Properties();
        // If this isn't false, then all rules' LHS object conditions will not match on redeploys!
        // (since objects are only equal if their classloaders are also equal - and they're not on redeploys)
        // NOTE: not necessary any more, per mproctor
        //properties.setProperty(ClassLoaderCacheOption.PROPERTY_NAME, Boolean.FALSE.toString());
        if (overrides != null) {
            overrideProperties(properties, overrides);
        }
        PropertiesModel propertiesModel  = model.getProperties();
        if (propertiesModel != null) {
            overrideProperties(properties, propertiesModel.toProperties());
        }
        return properties;
    }

    private static void overrideProperties(Properties target, Properties overrides) {
        for (Object key : overrides.keySet()) {
            String name = (String)key;
            String value = overrides.getProperty(name);
            target.setProperty(name, value);
        }
    }

    private Propertys() {}

}
