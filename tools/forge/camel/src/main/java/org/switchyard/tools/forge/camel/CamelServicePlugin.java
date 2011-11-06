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

package org.switchyard.tools.forge.camel;

import javax.inject.Inject;

import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.MetadataFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.Topic;
import org.switchyard.component.camel.config.model.v1.V1CamelImplementationModel;
import org.switchyard.config.model.composite.v1.V1ComponentModel;
import org.switchyard.config.model.composite.v1.V1ComponentServiceModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.plugin.TemplateResource;

/**
 * Commands related to Camel services.
 */
@Alias("camel-service")
@RequiresProject
@RequiresFacet({SwitchYardFacet.class, CamelFacet.class})
@Topic("SOA")
@Help("Provides commands to create and edit Camel routes in SwitchYard.")
public class CamelServicePlugin implements Plugin {
    

    // Template files used for camel route services
    private static final String ROUTE_INTERFACE_TEMPLATE = "/org/switchyard/tools/forge/camel/RouteInterfaceTemplate.java";
    private static final String ROUTE_IMPLEMENTATION_TEMPLATE = "/org/switchyard/tools/forge/camel/RouteImplementationTemplate.java";
    
    private enum RouteType {
        JAVA, XML;
        
        static RouteType fromString(String typeStr) {
            if (JAVA.toString().equalsIgnoreCase(typeStr)) {
                return JAVA;
            } else if (XML.toString().equalsIgnoreCase(typeStr)) {
                return XML;
            } else {
                return null;
            }
        }
    }
    
    @Inject
    private Project _project;
    
    @Inject
    private Shell _shell;
    
    
    /**
     * Create a new Camel component service.
     * @param serviceName service name
     * @param out shell output
     * @param routeType type of the route (Java, XML)
     * @throws java.io.IOException error locating class 
     */
    @Command(value = "create", help = "Created a new Camel service.")
    public void newRoute(
            @Option(required = true,
                    name = "serviceName",
                    description = "The service name") 
            final String serviceName,
            @Option(required = false,
                    name = "type",
                    description = "Route type") 
            final String routeType,
            final PipeOut out) throws java.io.IOException {

        RouteType type = RouteType.fromString(routeType);
        if (type == null || type == RouteType.JAVA) {
            createJavaRoute(serviceName, out);
        } else if (RouteType.XML == type) {
            createXMLRoute(serviceName);
        }
        
        
        out.println("Created Camel service " + serviceName);
    }
    
    private void createXMLRoute(String routeName) {
        SwitchYardFacet switchYard = _project.getFacet(SwitchYardFacet.class);
        // Create the component service model
        V1ComponentModel component = new V1ComponentModel();
        component.setName(routeName + "Component");
        V1ComponentServiceModel service = new V1ComponentServiceModel();
        service.setName(routeName);
        component.addService(service);
        
        // Create the Camel implementation model and add it to the component model
        V1CamelImplementationModel impl = new V1CamelImplementationModel();
        component.setImplementation(impl);
        
        // Add the new component service to the application config
        SwitchYardModel syConfig = switchYard.getSwitchYardConfig();
        syConfig.getComposite().addComponent(component);
        switchYard.saveConfig();
    }
    
    /**
     * Creates a Java DSL bean containing a Camel route.  You'll notice that this
     * code is very similar to the Bean generation logic in the bean component
     * plugin.  We need to look at ways to synchronize these two pieces (e.g.
     * create a bean component, then add a route definition to it).
     */
    private void createJavaRoute(String routeName, PipeOut out) 
    throws java.io.IOException {
        
        String pkgName = _project.getFacet(MetadataFacet.class).getTopLevelPackage();
        
        if (pkgName == null) {
            pkgName = _shell.promptCommon(
                "Java package for route interface and implementation:",
                PromptType.JAVA_PACKAGE);
        }
        
        // Create the camel interface and implementation
        TemplateResource camelIntf = new TemplateResource(ROUTE_INTERFACE_TEMPLATE);
        camelIntf.serviceName(routeName);
        String interfaceFile = camelIntf.writeJavaSource(
                _project.getFacet(ResourceFacet.class), pkgName, routeName, false);
        out.println("Created route service interface [" + interfaceFile + "]");
        
        TemplateResource camelImpl = new TemplateResource(ROUTE_IMPLEMENTATION_TEMPLATE);
        camelImpl.serviceName(routeName);
        String implementationFile = camelImpl.writeJavaSource(
                _project.getFacet(ResourceFacet.class), pkgName, routeName + "Builder", false);
        out.println("Created route service implementation [" + implementationFile + "]");
        
        out.println(out.renderColor(ShellColor.BLUE, 
                "NOTE: Run 'mvn package' to make " + routeName + " visible to SwitchYard shell."));
    }
}
