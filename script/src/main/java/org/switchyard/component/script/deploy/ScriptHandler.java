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
package org.switchyard.component.script.deploy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.switchyard.Exchange;
import org.switchyard.HandlerException;
import org.switchyard.common.type.Classes;
import org.switchyard.component.script.config.model.CodeModel;
import org.switchyard.component.script.config.model.ScriptComponentImplementationModel;
import org.switchyard.deploy.ServiceHandler;
import org.switchyard.exception.SwitchYardException;

/**
 * An ExchangeHandle that can load and invoke a script using Java Scripting API. 
 * 
 * @author Jiri Pechanec
 * @author Daniel Bevenius
 *
 */
public class ScriptHandler implements ServiceHandler {
    
    private final ScriptComponentImplementationModel _implModel;
    private ScriptEngine _engine;
    private CompiledScript _compiledScript;
    private String _scriptCode;

    /**
     * Sole constructor.
     * 
     * @param implModel The configuration model.
     */
    public ScriptHandler(final ScriptComponentImplementationModel implModel) {
        _implModel = implModel;
    }
    
    /**
     * Initializes script engine and loads the script.
     */
    public void start() {
        try {
            final String language = _implModel.getLanguage();
            final String scriptFile = _implModel.getScriptFile();
            final CodeModel scriptModel = _implModel.getCodeModel();
            
            if (scriptFile == null && scriptModel == null) {
                throw new SwitchYardException(
                        "Neither script file nor in-line script were provided");
            }
            if (scriptFile != null && scriptModel != null) {
                throw new SwitchYardException(
                        "Either script file or in-line script may be provided but not both");
            }
            if (scriptModel != null && language == null) {
                throw new SwitchYardException(
                        "Missing language for in-line script");
            }
            final ScriptEngineManager manager = new ScriptEngineManager(
                    Classes.getClassLoader());
            if (language != null) {
                _engine = manager.getEngineByName(language);
                if (_engine == null) {
                    throw new SwitchYardException("Unknown script language '"
                            + language + "'");
                }
            } else {
                // We tested previously that scriptFile is not null when
                // language is null, we fill find an extension of script
                // filename
                final int dotIndex = scriptFile.lastIndexOf('.');
                if (dotIndex == -1) {
                    throw new SwitchYardException("No extension in filename '"
                            + scriptFile + "'");
                }
                final String extension = scriptFile.substring(dotIndex + 1);
                _engine = manager.getEngineByExtension(extension);
                if (_engine == null) {
                    throw new SwitchYardException("Unknown script extension '"
                            + extension + "'");
                }
            }
            
            if (_engine instanceof Compilable) {
                _compiledScript = (scriptModel != null) ? ((Compilable) _engine)
                        .compile(scriptModel.getCode())
                        : ((Compilable) _engine)
                                .compile(loadInputStream(_implModel
                                        .getScriptFile()));
            } else {
                _scriptCode = (scriptModel != null) ? scriptModel.getCode()
                        : streamToString(loadInputStream(_implModel
                                .getScriptFile()));
            }
        } catch (final SwitchYardException e) {
            throw e;
        } catch (final Exception e) {
            throw new SwitchYardException(e);
        }
    }
    
    @Override
    public void stop() {
        // Nothing to do here
    }
    
    private InputStreamReader loadInputStream(final String scriptFile) throws IOException {
        final InputStream in = Classes.getResourceAsStream(scriptFile);
        if (in != null) {
            return new InputStreamReader(in);
        } else {
            return new InputStreamReader(new FileInputStream(scriptFile));
        }
    }

    private String streamToString(final InputStreamReader inputStream) throws IOException {
        final BufferedReader in = new BufferedReader(inputStream);
        char[] buffer = new char[1024];        
        StringBuilder sb = new StringBuilder();
        int count;
        while ((count = in.read(buffer)) != -1) {
            sb.append(buffer, 0, count);
        }
        return sb.toString();
    }
    
    @Override
    public void handleMessage(final Exchange exchange) throws HandlerException {
        try {
            final Bindings bindings = _engine.createBindings();
            if (_implModel.injectExchange()) {
                bindings.put("exchange", exchange);
            } else {
                bindings.put("content", exchange.getMessage().getContent());
            }
            final Object response = (_compiledScript != null) ? _compiledScript
                    .eval(bindings) : _engine.eval(_scriptCode, bindings);
            if (response != null) {
                exchange.getMessage().setContent(response);
                exchange.send(exchange.getMessage());
            }
        } catch (final Exception e) {
            throw new HandlerException(e);
        }
    }

    @Override
    public void handleFault(final Exchange exchange) {
    }

}
