/*
 * 2012 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.switchyard.tools.forge.rules;

import static org.switchyard.component.rules.config.model.RulesComponentImplementationModel.DEFAULT_NAMESPACE;

import java.io.File;

import javax.inject.Inject;

import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.java.JavaInterface;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.project.facets.MetadataFacet;
import org.jboss.forge.project.facets.ResourceFacet;
import org.jboss.forge.shell.PromptType;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.Command;
import org.jboss.forge.shell.plugins.Help;
import org.jboss.forge.shell.plugins.Option;
import org.jboss.forge.shell.plugins.PipeOut;
import org.jboss.forge.shell.plugins.Plugin;
import org.jboss.forge.shell.plugins.RequiresFacet;
import org.jboss.forge.shell.plugins.RequiresProject;
import org.jboss.forge.shell.plugins.Topic;
import org.switchyard.common.io.resource.ResourceType;
import org.switchyard.component.common.knowledge.config.model.OperationModel;
import org.switchyard.component.common.knowledge.config.model.v1.V1ManifestModel;
import org.switchyard.component.common.knowledge.config.model.v1.V1OperationsModel;
import org.switchyard.component.rules.RulesOperationType;
import org.switchyard.component.rules.config.model.v1.V1RulesComponentImplementationModel;
import org.switchyard.component.rules.config.model.v1.V1RulesOperationModel;
import org.switchyard.config.model.composite.InterfaceModel;
import org.switchyard.config.model.composite.v1.V1ComponentModel;
import org.switchyard.config.model.composite.v1.V1ComponentServiceModel;
import org.switchyard.config.model.composite.v1.V1InterfaceModel;
import org.switchyard.config.model.resource.v1.V1ResourceModel;
import org.switchyard.config.model.resource.v1.V1ResourcesModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;
import org.switchyard.tools.forge.plugin.SwitchYardFacet;
import org.switchyard.tools.forge.plugin.TemplateResource;

/**
 * Forge plugin for Rules component commands.
 */
@Alias("rules-service")
@RequiresProject
@RequiresFacet({RulesFacet.class, ResourceFacet.class})
@Topic("SOA")
@Help("Provides commands related to rules services in SwitchYard.")
public class RulesServicePlugin implements Plugin {

    // rule definition template
    private static final String RULES_TEMPLATE = "RulesTemplate.drl";
    // rule definition file extension
    private static final String RULES_EXTENSION = ".drl";
    // rule definition directory
    private static final String RULES_DIR = "META-INF";
    
    @Inject
    private Project _project;
    
    @Inject
    private Shell _shell;
    
    /**
     * Create a new rules service interface and implementation.
     * @param argServiceName service name
     * @param out shell output
     * @param argInterfaceClass class name of Java service interface
     * @param argRuleFilePath path to the rule definition
     * @throws java.io.IOException error with file resources
     */
    @Command(value = "create", help = "Created a new service backed by business rules.")
    public void newRules(
            @Option(required = true,
                     name = "serviceName",
                     description = "The service name") 
             final String argServiceName,
             @Option(required = false,
                     name = "interfaceClass",
                     description = "The Java service interface") 
             final String argInterfaceClass,
             @Option(required = false,
                     name = "ruleDefinition",
                     description = "The business rule definition") 
             final String argRuleFilePath,
             final PipeOut out)
    throws java.io.IOException {
      
        JavaSourceFacet java = _shell.getCurrentProject().getFacet(JavaSourceFacet.class);
        String pkgName = _project.getFacet(MetadataFacet.class).getTopLevelPackage();
        String interfaceClass = argInterfaceClass;
        
        if (interfaceClass == null) {
            // Figure out the Java package name for the interface
            if (pkgName == null) {
                pkgName = _shell.promptCommon(
                    "Java package for service interface:",
                    PromptType.JAVA_PACKAGE);
            }
        
            // Create the service interface
            JavaInterface ruleInterface = JavaParser.create(JavaInterface.class)
                .setPackage(pkgName)
                .setName(argServiceName)
                .setPublic();
            java.saveJavaSource(ruleInterface);
            interfaceClass = ruleInterface.getQualifiedName();

            out.println("Created service interface [" + interfaceClass + "]");
        }
        
        String ruleDefinitionPath = argRuleFilePath;
        if (ruleDefinitionPath == null) {
            // Create an empty rule definition
            ruleDefinitionPath = RULES_DIR + File.separator + argServiceName + RULES_EXTENSION;
            TemplateResource template = new TemplateResource(RULES_TEMPLATE)
                .serviceName(argServiceName)
                .packageName(pkgName);
            template.writeResource(_project.getFacet(ResourceFacet.class).getResource(ruleDefinitionPath));
            
            out.println("Created rule definition [" + ruleDefinitionPath + "]");
        }
        
        // Add the SwitchYard config
        createImplementationConfig(argServiceName, interfaceClass, ruleDefinitionPath);
          
        // Notify user of success
        out.println("Rule service " + argServiceName + " has been created.");
    }
    
    private void createImplementationConfig(String serviceName,
            String interfaceName,
            String rulesDefinition) {
        
        SwitchYardFacet switchYard = _project.getFacet(SwitchYardFacet.class);
        // Create the component service model
        V1ComponentModel component = new V1ComponentModel();
        component.setName(serviceName);
        V1ComponentServiceModel service = new V1ComponentServiceModel();
        service.setName(serviceName);
        InterfaceModel csi = new V1InterfaceModel(InterfaceModel.JAVA);
        csi.setInterface(interfaceName);
        service.setInterface(csi);
        component.addService(service);
        
        // Create the Rules implementation model and add it to the component model
        V1RulesComponentImplementationModel rules = new V1RulesComponentImplementationModel();
        V1OperationsModel operations = new V1OperationsModel(DEFAULT_NAMESPACE);
        OperationModel operation = (OperationModel)new V1RulesOperationModel().setType(RulesOperationType.EXECUTE).setName("operation");
        operations.addOperation(operation);
        rules.setOperations(operations);
        V1ManifestModel manifest = new V1ManifestModel(DEFAULT_NAMESPACE);
        V1ResourcesModel resources = new V1ResourcesModel(DEFAULT_NAMESPACE);
        resources.addResource(new V1ResourceModel(DEFAULT_NAMESPACE).setLocation(rulesDefinition).setType(ResourceType.valueOf("DRL")));
        manifest.setResources(resources);
        rules.setManifest(manifest);
        component.setImplementation(rules);
        
        // Add the new component service to the application config
        SwitchYardModel syConfig = switchYard.getSwitchYardConfig();
        syConfig.getComposite().addComponent(component);
        switchYard.saveConfig();
    }

}
