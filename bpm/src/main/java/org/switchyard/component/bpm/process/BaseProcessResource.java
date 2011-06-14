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
package org.switchyard.component.bpm.process;

/**
 * Base class for ProcessResources.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; (C) 2011 Red Hat Inc.
 */
public abstract class BaseProcessResource implements ProcessResource {

    private String _location;
    private ProcessResourceType _type;

    /**
     * Constructs a new BaseProcessResource.
     */
    public BaseProcessResource() {}

    /**
     * Constructs a new BaseProcessResource with the specified resource location.
     * @param location the resource location
     */
    public BaseProcessResource(String location) {
        setLocation(location);
    }

    /**
     * Constructs a new BaseProcessResource with the specified resource location and type.
     * @param location the resource location
     * @param type the resource type
     */
    public BaseProcessResource(String location, ProcessResourceType type) {
        setLocation(location);
        setType(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLocation() {
        return _location;
    }

    /**
     * Sets the resource location.
     * @param location the resource location
     * @return this BaseProcessResource (useful for chaining)
     */
    public BaseProcessResource setLocation(String location) {
        _location = location;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessResourceType getType() {
        return _type;
    }

    /**
     * Sets the resource type.
     * @param type the resource type
     * @return this BaseProcessResource (useful for chaining)
     */
    public BaseProcessResource setType(ProcessResourceType type) {
        _type = type;
        return this;
    }

}
