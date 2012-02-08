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
package org.switchyard.component.rules.exchange;

import javax.xml.namespace.QName;

import org.switchyard.ServiceDomain;
import org.switchyard.component.rules.config.model.RulesComponentImplementationModel;
import org.switchyard.deploy.ServiceHandler;

/**
 * The ExchangeHandler for the Rules component.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; (C) 2011 Red Hat Inc.
 */
public interface RulesExchangeHandler extends ServiceHandler {

    /**
     * Initializes the RulesExchangeHandler.
     * @param qname the qualified name
     * @param model the configuration
     * @param domain the service domain
     */
    public void init(QName qname, RulesComponentImplementationModel model, ServiceDomain domain);

    /**
     * Destroys the RulesExchangeHandler.
     */
    public void destroy();

}
