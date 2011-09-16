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

package org.switchyard.tools.forge.soap;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.api.Deployment;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.project.dependencies.DependencyResolver;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.switchyard.tools.forge.bean.BeanFacet;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Test for {@link BeanFacet}.
 *
 * @author Mario Antollini
 */
public class ForgeSOAPTest extends AbstractShellTest {

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
    
    private static final String BEAN_SERVICE = "ForgeBeanService";
    
    private static final String BEAN_SERVICE_REFERENCEABLE = "ForgeBeanServiceReferenceable"; 
    
    private static final String _switchyardSuccessMsg = "SwitchYard version " + switchyardVersion;
    
    private static final String _soapFacetSuccessMsg = "***SUCCESS*** Installed [switchyard.soap] successfully";
    
    private static final String _soapBindingSuccessMsg = "Added binding.soap to service " + BEAN_SERVICE;
    
    private static final String _soapReferenceSuccessMsg = "Added binding.soap to reference " + BEAN_SERVICE_REFERENCEABLE;
    

    @Deployment
    public static JavaArchive getDeployment() {
        
    	// The deployment method is where references to classes, packages, 
        // and configuration files are added via Arquillian.
        JavaArchive archive = AbstractShellTest.getDeployment();
        archive.addPackage(SwitchYardFacet.class.getPackage());
        archive.addPackage(SOAPFacet.class.getPackage());
        archive.addPackage(BeanFacet.class.getPackage());
        return archive;
    }

    @Test
    public void test() throws Exception{
    	prepareSwitchyardForge();
        //soap-binding bind-service
    	testBindService();
    	//camel-binding bind-reference
    	testBindReference();
    }
    
    private void testBindService() throws Exception {
    	
        getShell().execute("project install-facet switchyard.soap");
        Assert.assertTrue(outputStream.toString().contains(_soapFacetSuccessMsg));
    
    	getShell().execute("project install-facet switchyard.bean");
        queueInputLines(BEAN_SERVICE);
        getShell().execute("bean-service create");
        String[] mvnCommand = new String[]{"package", "-Dmaven.test.skip=true"};
        getProject().getFacet(MavenCoreFacet.class).executeMaven(mvnCommand);
//        getShell().execute("build");
        //waiting(60);
        getShell().execute("switchyard promote-service --serviceName " + BEAN_SERVICE);
        queueInputLines("dummyWSDLEndpoint");
        getShell().execute("soap-binding bind-service --serviceName " + BEAN_SERVICE);
        System.out.println(outputStream);
        Assert.assertTrue(outputStream.toString().contains(_soapBindingSuccessMsg));
        Assert.assertNotNull(resolver);
    }
    
    private void testBindReference() throws Exception {
    	initializeOutputStream();
    	getShell().execute("project install-facet switchyard.bean");
        queueInputLines(BEAN_SERVICE_REFERENCEABLE);
        getShell().execute("bean-service create");
        getShell().execute("project list-facets");
        getShell().execute("switchyard show-config");

        //Let's overwrite the ForgeBeanServiceBean.java file with the one in test/resources, which
        //is referencing ForgeBeanServiceReferenceable
        File from = new File("." + File.separator + "src" + File.separator + "test" + File.separator + 
        		"resources" + File.separator + "com" + File.separator + "test" + File.separator + "ForgeBeanServiceBean.java");
        File to = new File(getShell().getCurrentDirectory().getFullyQualifiedName() + File.separator + "src" + 
        		File.separator + "main" + File.separator + "java" + File.separator + 
        		"com" + File.separator + "test");
        FileUtils.copyFileToDirectory(from, to);
        
        
        getShell().execute("build");
        queueInputLines(BEAN_SERVICE_REFERENCEABLE);
        getShell().execute("switchyard promote-reference");
        queueInputLines(BEAN_SERVICE_REFERENCEABLE);
        queueInputLines("file://target/input?fileName=test.txt");
        getShell().execute("soap-binding bind-reference");
        getShell().execute("switchyard show-config");
        System.out.println(outputStream);
        Assert.assertTrue(outputStream.toString().contains(_soapReferenceSuccessMsg));
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
    
    public static void waiting (int n){
        long t0, t1;
        t0 =  System.currentTimeMillis();
        do{
            t1 = System.currentTimeMillis();
        }
        while ((t1 - t0) < (n * 1000));
    }

}
