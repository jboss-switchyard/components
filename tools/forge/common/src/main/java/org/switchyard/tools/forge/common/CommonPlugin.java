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

package org.switchyard.tools.forge.common;

import java.util.List;
import java.util.ArrayList;

import javax.inject.Inject;

import org.jboss.forge.project.Project;
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
import org.switchyard.config.model.composer.ContextMapperModel;
import org.switchyard.config.model.composer.MessageComposerModel;
import org.switchyard.config.model.composer.v1.V1ContextMapperModel;
import org.switchyard.config.model.composer.v1.V1MessageComposerModel;
import org.switchyard.config.model.composite.BindingModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;

/**
 * Forge commands related to Component Common Library.
 */
@Alias("common")
@RequiresProject
@RequiresFacet({SwitchYardFacet.class, CommonFacet.class})
@Topic("SOA")
@Help("Provides commands to manage Component Common Facility in SwitchYard.")
public class CommonPlugin implements Plugin {

    @Inject
    private Project _project;
    
    @Inject
    private Shell _shell;
 
    /**
     * Add a context mapper on a binding.
     * @param serviceName composite service name to be added
     * @param className the fully qualified class name of ContextMapper
     * @param includes the comma-separated list of regex property includes
     * @param excludes the comma-separated list of regex property excludes
     * @param includeNamespaces the comma-separated list of regex property include namespaces
     * @param excludeNamespaces the comma-separated list of regex property exclude namespaces.
     * @param out shell output
     */
    @Command(value = "add-context-mapper", help = "Add a context mapper to a service binding.")
    public void addContextMapper(
            @Option(required = true,
                    name = "serviceName",
                    description = "The service name") 
            final String serviceName,
            @Option(required = false,
                    name = "className",
                    description = "The class name if you use your own ContextMapper implementation") 
            final String className,
            @Option(required = false,
                    name = "includes",
                    description = "Regular expression indicates which context property should be included") 
            final String includes,
            @Option(required = false,
                    name = "excludes",
                    description = "Regular expression indicates which context property should be excluded") 
            final String excludes,
            @Option(required = false,
                    name = "includeNamespaces",
                    description = "Regular expression indicates which context property namespaces should be included (if the property name is a qualified name)") 
            final String includeNamespaces,
            @Option(required = false,
                    name = "excludeNamespaces",
                    description = "Regular expression indicates which context property namespaces should be excluded (if the property name is a qualified name)") 
            final String excludeNamespaces,
            final PipeOut out) {
        SwitchYardFacet switchYard = _project.getFacet(SwitchYardFacet.class);
        CompositeServiceModel service = null;
        for (CompositeServiceModel s : switchYard.getSwitchYardConfig().getComposite().getServices()) {
            if (s.getName().equals(serviceName)) {
                service = s;
            }
        }
        if (service == null) {
            out.println(out.renderColor(ShellColor.RED, "Service " + serviceName + " could not be found"));
            return;
        }

        if (service.getBindings().size() == 0) {
            out.println(out.renderColor(ShellColor.YELLOW, "No binding"));
            return;
        }
        List<String> bindingDescList = new ArrayList<String>();
        List<BindingModel> bindingList = new ArrayList<BindingModel>(); 
        for (BindingModel binding : service.getBindings()) {
            bindingList.add(BindingModel.class.cast(binding));
            bindingDescList.add(binding.getModelConfiguration().toString());
        }
        BindingModel binding = bindingList.get(_shell.promptChoice("Which binding to add", bindingDescList));
        
        String namespace = binding.getModelConfiguration().getQName().getNamespaceURI();
        ContextMapperModel model = new V1ContextMapperModel(namespace);
        if (className != null) {
            try {
                model.setClazz(className);
            } catch (Exception e) {
                out.println(out.renderColor(ShellColor.RED, e.getMessage()));
                return;
            }
        }
        model.setIncludes(includes);
        model.setExcludes(excludes);
        model.setIncludeNamespaces(includeNamespaces);
        model.setExcludeNamespaces(excludeNamespaces);
        binding.setContextMapper(model);
        switchYard.saveConfig();
        
        //Notify user of success
        out.println("ContextMapper successfully added to " + serviceName);
    }
    
    /**
     * Add a message composer on a binding.
     * @param serviceName composite service name to be added
     * @param className the fully qualified class name of MessageMapper
     * @param out shell output
     */
    @Command(value = "add-message-composer", help = "Add a message composer to a service binding.")
    public void addMessageComposer(
            @Option(required = true,
                    name = "serviceName",
                    description = "The service name") 
            final String serviceName,
            @Option(required = false,
                    name = "className",
                    description = "The class name if you use your own MessageComposer implementation") 
            final String className,
            final PipeOut out) {
        SwitchYardFacet switchYard = _project.getFacet(SwitchYardFacet.class);
        CompositeServiceModel service = null;
        for (CompositeServiceModel s : switchYard.getSwitchYardConfig().getComposite().getServices()) {
            if (s.getName().equals(serviceName)) {
                service = s;
            }
        }
        if (service == null) {
            out.println(out.renderColor(ShellColor.RED, "Service " + serviceName + " could not be found"));
            return;
        }

        if (service.getBindings().size() == 0) {
            out.println(out.renderColor(ShellColor.YELLOW, "No binding"));
            return;
        }
        List<String> bindingDescList = new ArrayList<String>();
        List<BindingModel> bindingList = new ArrayList<BindingModel>(); 
        for (BindingModel binding : service.getBindings()) {
            bindingList.add(BindingModel.class.cast(binding));
            bindingDescList.add(binding.getModelConfiguration().toString());
        }
        BindingModel binding = bindingList.get(_shell.promptChoice("Which binding to add", bindingDescList));
        
        String namespace = binding.getModelConfiguration().getQName().getNamespaceURI();
        MessageComposerModel model = new V1MessageComposerModel(namespace);
        if (className != null) {
            try {
                model.setClazz(className);
            } catch (Exception e) {
                out.println(out.renderColor(ShellColor.RED, e.getMessage()));
                return;
            }
        }
        binding.setMessageComposer(model);
        switchYard.saveConfig();
        
        //Notify user of success
        out.println("MessageComposer successfully added to " + serviceName);
    }
}
