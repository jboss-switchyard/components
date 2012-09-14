/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
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
package org.switchyard.component.common.rules.config.model.v1;

import java.util.EventListener;

import org.switchyard.common.lang.Strings;
import org.switchyard.common.type.Classes;
import org.switchyard.common.xml.XMLHelper;
import org.switchyard.component.common.rules.config.model.EventListenerModel;
import org.switchyard.config.Configuration;
import org.switchyard.config.model.BaseModel;
import org.switchyard.config.model.Descriptor;

/**
 * The 1st version EventListenerModel.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; &copy; 2012 Red Hat Inc.
 */
public class V1EventListenerModel extends BaseModel implements EventListenerModel {

    /**
     * Creates a new EventListenerModel in the specified namespace.
     * @param namespace the namespace
     */
    public V1EventListenerModel(String namespace) {
        super(XMLHelper.createQName(namespace, EVENT_LISTENER));
    }

    /**
     * Creates a new EventListenerModel with the specified configuration and descriptor.
     * @param config the configuration
     * @param desc the descriptor
     */
    public V1EventListenerModel(Configuration config, Descriptor desc) {
        super(config, desc);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends EventListener> getClazz(ClassLoader loader) {
        String c = Strings.trimToNull(getModelAttribute("class"));
        return c != null ? (Class<? extends EventListener>)Classes.forName(c, loader) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventListenerModel setClazz(Class<? extends EventListener> clazz) {
        String c = clazz != null ? clazz.getName() : null;
        setModelAttribute("class", c);
        return this;
    }

}
