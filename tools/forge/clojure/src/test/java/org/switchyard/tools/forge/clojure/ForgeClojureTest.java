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

package org.switchyard.tools.forge.clojure;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.api.Deployment;
import org.jboss.forge.project.dependencies.DependencyResolver;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Test for {@link ClojureFacet}.
 *
 * @author Mario Antollini
 */
public class ForgeClojureTest extends AbstractShellTest {

	/**
     * Property name used in POMs to identify SwitchYard version.
     */
    public static final String VERSION = "switchyard.version";
	
    @Inject
    private DependencyResolver resolver;
    
    private static OutputStream outputStream;
    
    private static final String switchyardVersion = SwitchYardFacet.class.getPackage().getSpecificationVersion();
    
    private static final String switchyardFacetVersion = " - [org.switchyard:switchyard-api:::" + switchyardVersion;
    
    private static final String FORGE_APP = "ForgeTestApp";
    
    private static final String CLOJURE_SERVICE = "ForgeClojureService";
    
    private static final String _switchyardSuccessMsg = "SwitchYard version " + switchyardVersion;
    
    private static final String _clojureServiceSuccessMsg = "Created Clojure implementation " + CLOJURE_SERVICE;
    

    @Deployment
    public static JavaArchive getDeployment() {
        
    	// The deployment method is where references to classes, packages, 
        // and configuration files are added via Arquillian.
        JavaArchive archive = AbstractShellTest.getDeployment();
        archive.addPackage(SwitchYardFacet.class.getPackage());
        archive.addPackage(ClojureFacet.class.getPackage());
        return archive;
    }

    @Test
    public void test() throws Exception{
    	prepareSwitchyardForge();
        //clojure-service create
    	testCreateClojureService();
    }
    
    private void testCreateClojureService() throws Exception {
        getShell().execute("project install-facet switchyard.clojure");
        getShell().execute("clojure-service create --serviceName " + CLOJURE_SERVICE + " --emptyInlineScript");
        System.out.println(outputStream);
        Assert.assertTrue(outputStream.toString().contains(_clojureServiceSuccessMsg));
        Assert.assertNotNull(resolver);
    }
    
    
    private void prepareSwitchyardForge() throws IOException {
    	initializeJavaProject();
    	initializeOutputStream();
        
    	getShell().execute("project install-facet switchyard");
    	
        int index = outputStream.toString().indexOf(switchyardFacetVersion);
        if(index > -1){
            Integer version = Integer.decode(outputStream.toString().substring(index - 1, index));
            queueInputLines(version.toString());
        }
    	
        queueInputLines(FORGE_APP);
        getShell().execute("project install-facet switchyard");
        getShell().execute("switchyard get-version");
        Assert.assertTrue(outputStream.toString().contains(_switchyardSuccessMsg));
    }
    
    
    private void initializeOutputStream()  throws IOException {
        outputStream = new OutputStream()
        {
            private StringBuilder string = new StringBuilder();
            @Override
            public void write(int b) throws IOException {
                this.string.append((char) b );
            }

            public String toString(){
                return this.string.toString();
            }
        };
        getShell().setOutputStream(outputStream);
    }

}
