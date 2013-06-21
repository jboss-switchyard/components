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
package org.switchyard.component.common.composer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;
import org.switchyard.ProviderRegistry;
import org.switchyard.common.type.Classes;
import org.switchyard.config.model.composer.ContextMapperModel;

/**
 * Utility AND base class making it easy for Component developers to specify their own ContextMapper implementations.
 *
 * @param <D> the type of binding data
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; (C) 2011 Red Hat Inc.
 */
public abstract class ContextMapperFactory<D extends BindingData> {

    private static final Logger LOGGER = Logger.getLogger(ContextMapperFactory.class);

    /**
     * Component developer should implement this message to specify the type of source/target object.
     * @return the type of source/target object
     */
    public abstract Class<D> getBindingDataClass();

    /**
     * Component developer should implement this message to provide their default/fallback implementation
     * if the ContextMapperModel passed into {@link ContextMapperFactory#newContextMapper(ContextMapperModel)}
     * doesn't specify (or specifies a bad) context mapper class to use.
     * @return the default/fallback context mapper implementation
     */
    public abstract ContextMapper<D> newContextMapperDefault();

    /**
     * Will create a new ContextMapper based on the specifications of the passed in ContextMapperModel, or if
     * a class it not specified, will apply the rest of the model properties on the default/fallback implementation.
     * @param model contains the config details
     * @return the new ContextMapper instance
     */
    @SuppressWarnings("unchecked")
    public final ContextMapper<D> newContextMapper(ContextMapperModel model) {
        ContextMapper<D> contextMapper = null;
        ContextMapperFactory<D> contextMapperFactory = ContextMapperFactory.getContextMapperFactory(getBindingDataClass());
        if (model != null) {
            contextMapper = contextMapperFactory.newContextMapper((Class<ContextMapper<D>>)Classes.forName(model.getClazz()));
            contextMapper.setModel(model);
            if (contextMapper instanceof RegexContextMapper) {
                RegexContextMapper<D> regexContextMapper = (RegexContextMapper<D>)contextMapper;
                regexContextMapper.setIncludes(model.getIncludes());
                regexContextMapper.setExcludes(model.getExcludes());
                regexContextMapper.setIncludeNamespaces(model.getIncludeNamespaces());
                regexContextMapper.setExcludeNamespaces(model.getExcludeNamespaces());
            }
        } else {
            contextMapper = contextMapperFactory.newContextMapperDefault();
        }
        return contextMapper;
    }

    /**
     * Will create a new custom ContextMapper based on the specified class, or if it can't, will use the
     * default/fallback implementation.
     * @param custom the custom ContextMapper class
     * @return the new ContextMapper instance
     */
    public final ContextMapper<D> newContextMapper(Class<? extends ContextMapper<D>> custom) {
        ContextMapper<D> contextMapper = null;
        if (custom != null) {
            try {
                contextMapper = custom.newInstance();
            } catch (Exception e) {
                LOGGER.error("Could not instantiate ContextMapper: " + custom.getClass().getName() + " - " + e.getMessage());
            }
        }
        if (contextMapper == null) {
            contextMapper = newContextMapperDefault();
        }
        return contextMapper;
    }

    /**
     * Constructs a new ContextMapperFactory that is known to be able to construct ContextMappers
     * of the specified type.
     * @param <F> the type of binding data
     * @param targetClass the target ContextMapper class
     * @return the new ContextMapperFactory instance
     */
    @SuppressWarnings("unchecked")
    public static final <F extends BindingData> ContextMapperFactory<F> getContextMapperFactory(Class<F> targetClass) {
        return (ContextMapperFactory<F>)getContextMapperFactories().get(targetClass);
    }

    /**
     * Gets a map of all known ContextMapperFactories, keyed by their supported source/target object type.
     * @return the ContextMapperFactories map
     */
    @SuppressWarnings("rawtypes")
    public static final Map<Class, ContextMapperFactory> getContextMapperFactories() {
        Map<Class, ContextMapperFactory> factories = new HashMap<Class, ContextMapperFactory>();
        List<ContextMapperFactory> services = ProviderRegistry.getProviders(ContextMapperFactory.class);
        for (ContextMapperFactory factory : services) {
            factories.put(factory.getBindingDataClass(), factory);
        }
        return factories;
    }

}
