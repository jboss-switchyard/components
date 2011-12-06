/*
 * JBoss, by Red Hat.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.switchyard.tools.forge.rules;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.switchyard.tools.forge.GenericTestForge;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Test for {@link RulesFacet}.
 *
 * @author Mario Antollini
 */
public class ForgeRulesTest extends GenericTestForge {

    private static final String RULES_SERVICE = "ForgeRulesService";
    
    private static final String _rulesServiceSuccessMsg = "Rule service " + RULES_SERVICE + " has been created";
    

    @Deployment
    public static JavaArchive getDeployment() {
        
    	// The deployment method is where references to classes, packages, 
        // and configuration files are added via Arquillian.
        JavaArchive archive = AbstractShellTest.getDeployment();
        archive.addPackages(true, SwitchYardFacet.class.getPackage());
        archive.addPackages(true, RulesFacet.class.getPackage());
        return archive;
    }

    @Test
    public void test() throws Exception{
        //rules-service create
    	testCreateRuleService();
    }
    
    private void testCreateRuleService() throws Exception {
        resetOutputStream();
        getShell().execute("project install-facet switchyard.rules");
        getShell().execute("rules-service create --serviceName " + RULES_SERVICE);
        System.out.println(outputStream);
        Assert.assertTrue(outputStream.toString().contains(_rulesServiceSuccessMsg));
    }

}
