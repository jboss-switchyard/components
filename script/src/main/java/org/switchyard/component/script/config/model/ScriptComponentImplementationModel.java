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
package org.switchyard.component.script.config.model;

import org.switchyard.config.model.composite.ComponentImplementationModel;

/**
 * A definition of an 'implementation.script' element.
 * 
 * @author Jiri Pechanec 
 * @author Daniel Bevenius
 *
 */
public interface ScriptComponentImplementationModel extends ComponentImplementationModel {
    
    /**
     * The 'script' namespace.
     */
    public static final String DEFAULT_NAMESPACE = "urn:switchyard-component-script:config:1.0";
    
    /**
     * The 'script' implementation type.
     */
    String SCRIPT = "script";
    
    /**
     * The 'code' element name.
     */
    String CODE = "code";
    
    /**
     * The 'scriptFile' attribute name.
     */
    String SCRIPT_FILE = "scriptFile";
    
    /**
     * The 'injectExchange' attribute name.
     */
    String INJECT_EXCHANGE = "injectExchange";

    /**
     * The 'language' attribute name.
     */
    String LANGUAGE = "language";

    /**
     * Gets the JSR-223 script that is the actual script code.
     * @return {@link CodeModel} the code configuration model.
     */
    CodeModel getCodeModel();
    
    /**
     * Sets the in-line script model.
     * @param codeModel The code configuration model.
     * @return {@link ScriptComponentImplementationModel} to enable method chaining.
     */
    ScriptComponentImplementationModel setScriptModel(final CodeModel codeModel);
    
    /**
     * Gets the file that is the actual script code.
     * @return String the filename of a script to execute.
     */
    String getScriptFile();
    
    /**
     * Sets the scriptFile.
     * 
     * @param scriptFile The script file.
     * @return {@link ScriptComponentImplementationModel} to enable method chaining.
     */
    ScriptComponentImplementationModel setScriptFile(final String scriptFile);
    
    /**
     * Determines whether the complete Exchange should be injected into the script.
     * 
     * @return true If the Exchange should be injected.
     */
    Boolean injectExchange();
    
    /**
     * Sets the 'injectExchange' property whether the complete Exchange should be injected into the script.
     * 
     * @param enable The value to 'injectExchange to.
     * @return {@link ScriptComponentImplementationModel} to enable method chaining.
     */
    ScriptComponentImplementationModel setInjectExchange(final Boolean enable);
    
    /**
     * Gets the name of script language.
     * @return String the language name.
     */
    String getLanguage();
    
    /**
     * Sets the script language.
     * 
     * @param language The script language.
     * @return {@link ScriptComponentImplementationModel} to enable method chaining.
     */
    ScriptComponentImplementationModel setLanguage(final String language);
}
