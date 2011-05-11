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
package org.switchyard.camel.component;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 * SwitchyardComponent enable Switchyard services to be exposed through Apache Camel.
 * <p/>
 * This classes {@link #createEndpoint(String, String, Map)} creates a {@link SwitchyardEndpoint}.
 * 
 * Example usage using Camel's Java DSL:
 * <pre>
 * from("switchyard://someIncomingService")
 * ...
 * .to("switchyard://mySwitchyardService")
 * </pre>
 *
 * @author Daniel Bevenius
 */
public class SwitchyardComponent extends DefaultComponent {
    
    @Override
    protected Endpoint createEndpoint(final String uri, final String path, final Map<String, Object> parameters) throws Exception {
        final String operationName = (String) parameters.remove("operationName");
        return new SwitchyardEndpoint(uri, this, operationName);
    }
    
}
