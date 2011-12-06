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

package org.switchyard.tools.forge.bpm;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.switchyard.tools.forge.GenericTestForge;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Test for {@link BPMFacet}.
 *
 * @author Mario Antollini
 */
public class ForgeBPMTest extends GenericTestForge {

    private static final String BPM_SERVICE = "ForgeBPMService";
    
    private static final String _bpmServiceSuccessMsg = "Process service " + BPM_SERVICE + " has been created";
    

    @Deployment
    public static JavaArchive getDeployment() {
        
    	// The deployment method is where references to classes, packages, 
        // and configuration files are added via Arquillian.
        JavaArchive archive = AbstractShellTest.getDeployment();
        archive.addPackages(true, SwitchYardFacet.class.getPackage());
        archive.addPackages(true, BPMFacet.class.getPackage());
        return archive;
    }

    @Test
    public void test() throws Exception{
        //bpm-service create
    	testCreateBPMService();
    }
    
    private void testCreateBPMService() throws Exception {
        resetOutputStream();
        getShell().execute("project install-facet switchyard.bpm");
        getShell().execute("bpm-service create --serviceName " + BPM_SERVICE);
        System.out.println(outputStream);
        Assert.assertTrue(outputStream.toString().contains(_bpmServiceSuccessMsg));
    }
    
  

}
