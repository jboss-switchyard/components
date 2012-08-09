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
package org.switchyard.component.script.config.model.v1;

import org.switchyard.component.script.config.model.ScriptComponentImplementationModel;
import org.switchyard.component.script.config.model.CodeModel;
import org.switchyard.config.Configuration;
import org.switchyard.config.model.Descriptor;
import org.switchyard.config.model.composite.v1.V1ComponentImplementationModel;

/**
 * Version 1 implementation of a {@link ScriptComponentImplementationModel}. 
 * 
 * @author Jiri Pechanec
 * @author Daniel Bevenius
 *
 */
public class V1ScriptComponentImplementationModel extends V1ComponentImplementationModel implements ScriptComponentImplementationModel {
    
    private Boolean _injectExchange;
    private String _scriptFile;
    private String _language;
    private CodeModel _scriptModel;
    
    /**
     * No args constructor that uses the default namespace when constructing
     * this model.
     */
    public V1ScriptComponentImplementationModel() {
        super(SCRIPT, DEFAULT_NAMESPACE);
    }

    /**
     * Constructor.
     * 
     * @param config The configuration model.
     * @param desc The descriptor for the model.
     */
    public V1ScriptComponentImplementationModel(final Configuration config, final Descriptor desc) {
        super(config, desc);
    }

    @Override
    public CodeModel getCodeModel() {
        if (_scriptModel != null) {
            return _scriptModel;
        }
        
        _scriptModel = (CodeModel) getFirstChildModel(CODE);
        return _scriptModel;
    }
    
    @Override
    public V1ScriptComponentImplementationModel setScriptModel(final CodeModel scriptModel) {
        setChildModel(scriptModel);
        _scriptModel = scriptModel;
        return this;
    }

    @Override
    public String getScriptFile() {
        if (_scriptFile != null) {
            return _scriptFile;
        }
        
        _scriptFile = getModelAttribute(SCRIPT_FILE);
        return _scriptFile;
    }
    
    @Override
    public V1ScriptComponentImplementationModel setScriptFile(final String scriptFile) {
        setModelAttribute(SCRIPT_FILE, scriptFile);
        _scriptFile = scriptFile;
        return this;
    }
    
    @Override
    public Boolean injectExchange() {
        if (_injectExchange != null) {
            return _injectExchange;
        }
        
        _injectExchange = Boolean.valueOf(getModelAttribute(INJECT_EXCHANGE));
        return _injectExchange;
    }

    @Override
    public ScriptComponentImplementationModel setInjectExchange(final Boolean enable)
    {
        setModelAttribute(INJECT_EXCHANGE, enable.toString());
        _injectExchange = enable;
        return this;
    }

    @Override
    public String getLanguage() {
        if (_language != null) {
            return _language;
        }
        
        _language = getModelAttribute(LANGUAGE);
        return _language;
    }
    
    @Override
    public V1ScriptComponentImplementationModel setLanguage(final String language) {
        setModelAttribute(LANGUAGE, language);
        _language = language;
        return this;
    }
    
}
