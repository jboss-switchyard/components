/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.switchyard.component.clojure.config.model.v1;

import org.switchyard.component.clojure.config.model.ClojureComponentImplementationModel;
import org.switchyard.component.clojure.config.model.ClojureScriptModel;
import org.switchyard.config.Configuration;
import org.switchyard.config.model.Descriptor;
import org.switchyard.config.model.composite.v1.V1ComponentImplementationModel;

/**
 * Version 1 implementation of a {@link ClojureComponentImplementationModel}. 
 * 
 * @author Daniel Bevenius
 *
 */
public class V1ClojureComponentImplementationModel extends V1ComponentImplementationModel implements ClojureComponentImplementationModel {
    
    /**
     * No args constructor that uses the default namespace when constructing
     * this model.
     */
    public V1ClojureComponentImplementationModel() {
        super(CLOJURE, DEFAULT_NAMESPACE);
    }

    /**
     * Constructor.
     * 
     * @param config The configuration model.
     * @param desc The descriptor for the model.
     */
    public V1ClojureComponentImplementationModel(final Configuration config, final Descriptor desc) {
        super(config, desc);
    }

    @Override
    public String getScript() {
        final ClojureScriptModel scriptModel = (ClojureScriptModel) getFirstChildModel(SCRIPT);
        if (scriptModel != null) {
            return scriptModel.getScript();
        }
        return null;
    }
    
    @Override
    public V1ClojureComponentImplementationModel setScript(final ClojureScriptModel scriptModel) {
        setChildModel(scriptModel);
        return this;
    }

    @Override
    public String getScriptFile() {
        return getModelAttribute(SCRIPT_FILE);
    }
    
    @Override
    public V1ClojureComponentImplementationModel setScriptFile(final String scriptFile) {
        setModelAttribute(SCRIPT_FILE, scriptFile);
        return this;
    }
    
    @Override
    public boolean injectExchange() {
        return Boolean.parseBoolean(getModelAttribute(INJECT_EXCHANGE));
    }

    @Override
    public ClojureComponentImplementationModel setInjectExchange(final boolean enable)
    {
        setModelAttribute(INJECT_EXCHANGE, Boolean.toString(enable));
        return this;
    }
}
