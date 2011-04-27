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
package org.switchyard.tools.forge.clojure;

import java.io.File;
import java.io.IOException;

import org.switchyard.common.io.resource.StringResource;
import org.switchyard.component.clojure.config.model.ClojureComponentImplementationModel;
import org.switchyard.component.clojure.config.model.ClojureScriptModel;
import org.switchyard.component.clojure.config.model.v1.V1ClojureComponentImplementationModel;
import org.switchyard.component.clojure.config.model.v1.V1ClojureScriptModel;

/**
 * Builder that is able to popluate and build a {@link ClojureComponentImplementationModel}. 
 * 
 * @author Daniel Bevenius
 *
 */
public class ClojureModelBuilder {
    
    private ClojureComponentImplementationModel _implModel;
    
    /**
     * No-args constructor.
     * 
     * Defaults to creating a version 1.0 {@link ClojureComponentImplementationModel}.
     */
    public ClojureModelBuilder() {
        _implModel = new V1ClojureComponentImplementationModel();
    }

    /**
     * Will set the injectExchange on the underlying model.
     * 
     * @param inject The value to set.
     * @return {@link ClojureModelBuilder} to support method chaining.
     */
    public ClojureModelBuilder injectExchange(final boolean inject) {
        _implModel.setInjectExchange(inject);
        return this;
    }
    
    /**
     * Will pull the content from the passed in file and set it as the inline 
     * Clojure script.
     * 
     * @param script The script file to inline
     * @return {@link ClojureModelBuilder} to support method chaining.
     * @throws ClojureBuilderException If the file could not be located.
     */
    public ClojureModelBuilder inlineScript(final File script) throws ClojureBuilderException {
        try {
           return inlineScript(new StringResource().pull(script));
        } catch (IOException e) {
            throw new ClojureBuilderException("Could not located the Clojure script [" + script + "]", e);
        }
    }
    
    /**
     * Add the passed-in script to the model.
     * 
     * @param script The Clojure script to add
     * @return {@link ClojureModelBuilder} to support method chaining.
     */
    public ClojureModelBuilder inlineScript(final String script) {
        if (script != null) {
            final V1ClojureScriptModel scriptModel = new V1ClojureScriptModel();
            scriptModel.setScript(script);
            _implModel.setScriptModel(scriptModel);
        }
        return this;
    }

    /**
     * Set the script file path.
     * 
     * @param scriptFile Path to the external (classpath or filesystem) Clojure script.
     * @return {@link ClojureModelBuilder} to support method chaining.
     */
    public ClojureModelBuilder externalScript(String scriptFile) {
        if (scriptFile != null) {
            _implModel.setScriptFile(scriptFile);
        }
        return this;
    }

    /**
     * Builds the {@link ClojureComponentImplementationModel}.
     * 
     * @return {@link ClojureComponentImplementationModel} the populated {@link ClojureComponentImplementationModel}.
     * @throws ClojureBuilderException If a correct {@link ClojureComponentImplementationModel} could not be built.
     */
    public ClojureComponentImplementationModel build() throws ClojureBuilderException {
        if (hasInlineScript() && hasExternalScript()) {
            throw new ClojureBuilderException("Only one of 'inlineScript' and 'externalScript' can be set, not both!");
        }
        return _implModel;
    }
    
    private boolean hasExternalScript() {
        String scriptFile = _implModel.getScriptFile();
        return notNullAndNotEmpty(scriptFile);
    }
    
    private boolean notNullAndNotEmpty(final String s) {
        return s != null && !s.equals("");
    }

    private boolean hasInlineScript() {
        final ClojureScriptModel scriptModel = _implModel.getScriptModel();
        if (scriptModel == null) {
            return false;
        }
        return notNullAndNotEmpty(scriptModel.getScript());
    }
    
}
