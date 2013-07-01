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

import org.kie.api.KieBaseConfiguration;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilderConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.switchyard.component.common.knowledge.config.model.KnowledgeComponentImplementationModel;

/**
 * Configuration functions.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; &copy; 2012 Red Hat Inc.
 */
public final class Configurations {

    /**
     * Gets a base configuration.
     * @param model the model
     * @param overrides any overrides
     * @param loader the class loader
     * @return the base configuration
     */
    public static KieBaseConfiguration getBaseConfiguration(KnowledgeComponentImplementationModel model, Properties overrides, ClassLoader loader) {
        return KnowledgeBaseFactory.newKnowledgeBaseConfiguration(Propertys.getProperties(model, overrides), loader);
    }

    /**
     * Gets a builder configuration.
     * @param model the model
     * @param overrides any overrides
     * @param loader the class loader
     * @return the builder configuration
     */
    public static KnowledgeBuilderConfiguration getBuilderConfiguration(KnowledgeComponentImplementationModel model, Properties overrides, ClassLoader loader) {
        return KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration(Propertys.getProperties(model, overrides), loader);
    }

    /**
     * Gets a session configuration.
     * @param model the model
     * @param overrides any overrides
     * @return the session configuration
     */
    public static KieSessionConfiguration getSessionConfiguration(KnowledgeComponentImplementationModel model, Properties overrides) {
        return KnowledgeBaseFactory.newKnowledgeSessionConfiguration(Propertys.getProperties(model, overrides));
    }

    private Configurations() {}

}
