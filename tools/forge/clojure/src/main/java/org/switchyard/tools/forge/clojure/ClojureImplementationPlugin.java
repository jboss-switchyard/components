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

package org.switchyard.tools.forge.clojure;

import java.io.File;

import javax.inject.Inject;

import org.jboss.seam.forge.shell.ShellColor;
import org.jboss.seam.forge.shell.ShellPrintWriter;
import org.jboss.seam.forge.shell.plugins.Alias;
import org.jboss.seam.forge.shell.plugins.Command;
import org.jboss.seam.forge.shell.plugins.Help;
import org.jboss.seam.forge.shell.plugins.Option;
import org.jboss.seam.forge.shell.plugins.PipeOut;
import org.jboss.seam.forge.shell.plugins.RequiresFacet;
import org.jboss.seam.forge.shell.plugins.RequiresProject;
import org.jboss.seam.forge.shell.plugins.Topic;
import org.switchyard.component.clojure.config.model.ClojureComponentImplementationModel;
import org.switchyard.config.model.composite.v1.V1ComponentModel;
import org.switchyard.config.model.composite.v1.V1ComponentServiceModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.tools.forge.AbstractPlugin;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Commands related to Clojure services.
 * 
 * @author Daniel Bevenius
 */
@Alias("clojure-service")
@Topic("SOA")
@RequiresProject
@RequiresFacet({SwitchYardFacet.class, ClojureFacet.class})
@Help("Provides commands to create Clojure services in SwitchYard.")
public class ClojureImplementationPlugin extends AbstractPlugin {
    
    @Inject
    private ShellPrintWriter _writer;
    
    /**
     * Create a new Clojure implementation service.
     * 
     * @param serviceName The SwitchYard service name.
     * @param inlineScript Path to the Clojure script to inline
     * @param externalScript Path to the external Clojure script.
     * @param injectExchange Inject the SwitchYard Exchange object into the Clojure script
     * @param out shell output.
     */
    @Command(value = "create", help = "Create a new implemenation.clojure")
    public void newImplementation(@Option(required = true, 
                    name = "serviceName", shortName = "s", 
                    description = "The service name",
                    help="The SwitchYard service name to use for this implementation") 
            final String serviceName, 
            @Option(name = "inlineScript", shortName = "i", 
                    description = "Use inline Clojure script",
                    help="Path to the Clojure script to inline, the content will be placed into the script element") 
            final File inlineScript,        
            @Option(name = "externalScript", shortName = "e",
                    description = "Path to the external Clojure Script",
                    help="Path to the external Clojure script to be referenced from the 'scriptFile' attribute") 
            final String externalScript,        
            @Option(name = "injectExchange", shortName = "x",
                    flagOnly = true,
                    description = "Inject the SwitchYard Exchange object into the Clojure script",
                    help="The SwitchYard Exchange will be injected into the Clojure script if this value is set. If not, only the Message content will be injected.") 
            final boolean injectExchange,        
            final PipeOut out) {
        final ClojureComponentImplementationModel impl = createImplModel(inlineScript, externalScript, injectExchange);
        final V1ComponentModel component = createComponentModel(serviceName);
        component.setImplementation(impl);
        saveSwitchYardModel(component);
        out.println("Created Clojure service " + serviceName);
    }
    
    private ClojureComponentImplementationModel createImplModel(final File inlineScript, 
            final String externalScript, 
            final boolean injectExchange) {
        try {
            return new ClojureModelBuilder()
                .inlineScript(inlineScript)
                .externalScript(externalScript)
                .injectExchange(injectExchange)
                .build();
        } catch (final ClojureBuilderException e) {
            _writer.println(ShellColor.RED, e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    private void saveSwitchYardModel(final V1ComponentModel component) {
        final SwitchYardFacet switchYard = getProject().getFacet(SwitchYardFacet.class);
        final SwitchYardModel syConfig = switchYard.getSwitchYardConfig();
        syConfig.getComposite().addComponent(component);
        switchYard.saveConfig();
    }
    
    private V1ComponentModel createComponentModel(final String serviceName) {
        final V1ComponentModel component = new V1ComponentModel();
        component.setName(serviceName + "Component");
        final V1ComponentServiceModel service = new V1ComponentServiceModel();
        service.setName(serviceName);
        component.addService(service);
        return component;
    }
    
}
