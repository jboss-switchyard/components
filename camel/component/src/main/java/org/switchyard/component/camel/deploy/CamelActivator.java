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
package org.switchyard.component.camel.deploy;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.ToDefinition;
import org.switchyard.ServiceReference;
import org.switchyard.common.camel.SwitchYardCamelContext;
import org.switchyard.common.property.PropertyResolver;
import org.switchyard.component.camel.ComponentNameComposer;
import org.switchyard.component.camel.RouteFactory;
import org.switchyard.component.camel.SwitchYardConsumer;
import org.switchyard.component.camel.SwitchYardEndpoint;
import org.switchyard.component.camel.SwitchYardPropertiesParser;
import org.switchyard.component.camel.common.CamelConstants;
import org.switchyard.component.camel.common.SwitchYardRouteDefinition;
import org.switchyard.component.camel.common.composer.CamelComposition;
import org.switchyard.component.camel.common.deploy.BaseCamelActivator;
import org.switchyard.component.camel.model.CamelComponentImplementationModel;
import org.switchyard.config.model.composite.ComponentModel;
import org.switchyard.config.model.composite.ComponentReferenceModel;
import org.switchyard.config.model.composite.ComponentServiceModel;
import org.switchyard.deploy.ServiceHandler;
import org.switchyard.exception.SwitchYardException;

/**
 * Activates Camel bindings, references and implementations in SwitchYard. 
 * 
 * @author Daniel Bevenius
 */
public class CamelActivator extends BaseCamelActivator {

    /**
     * Creates a new activator for Camel endpoint types.
     * 
     * @param context Camel context to use.
     * @param types Activation types.
     */
    public CamelActivator(SwitchYardCamelContext context, String ... types) {
        super(context, types);
    }

    @Override
    public ServiceHandler activateService(QName serviceName, ComponentModel config) {
        ServiceHandler handler = null;

        // add switchyard property parser to camel PropertiesComponent
        PropertiesComponent propertiesComponent = getCamelContext().getComponent("properties", PropertiesComponent.class);
        PropertyResolver pr = config.getModelConfiguration().getPropertyResolver();
        propertiesComponent.setPropertiesParser(new SwitchYardPropertiesParser(pr));
        
        // process service
        for (ComponentServiceModel service : config.getServices()) {
            if (service.getQName().equals(serviceName)) {
                handler = handleImplementation(service, serviceName);
                break;
            }
        }

        return handler;
    }

    @Override
    public void deactivateService(QName name, ServiceHandler handler) {
        // nothing to do here ...
    }

    private ServiceHandler handleImplementation(final ComponentServiceModel config, final QName serviceName) {
        final CamelComponentImplementationModel ccim = 
                (CamelComponentImplementationModel)config.getComponent().getImplementation();
        try {
            final String endpointUri = ComponentNameComposer.composeComponentUri(serviceName);
            final List<RouteDefinition> routeDefinitions = getRouteDefinition(ccim);
            checkSwitchYardReferencedServiceExist(routeDefinitions, ccim);
            verifyRouteDefinitions(routeDefinitions, ccim);
            getCamelContext().addRouteDefinitions(routeDefinitions);
            final SwitchYardEndpoint endpoint = getCamelContext().getEndpoint(endpointUri, SwitchYardEndpoint.class);
            endpoint.setMessageComposer(CamelComposition.getMessageComposer());
            final SwitchYardConsumer consumer = endpoint.getConsumer();
            return consumer;
        } catch (final Exception e) {
            throw new SwitchYardException(e.getMessage(), e);
        }
    }

    private void checkSwitchYardReferencedServiceExist(List<RouteDefinition> routeDefinitions, CamelComponentImplementationModel ccim) {
        for (RouteDefinition routeDefinition : routeDefinitions) {
            final List<ProcessorDefinition<?>> outputs = routeDefinition.getOutputs();
            for (ProcessorDefinition<?> processorDef : outputs) {
                if (processorDef instanceof ToDefinition) {
                    final ToDefinition to = (ToDefinition) processorDef;
                    final URI componentUri = URI.create(to.getUri());
                    if (componentUri.getScheme().equals(CamelConstants.SWITCHYARD_COMPONENT_NAME)) {
                        final String serviceName = componentUri.getHost();
                        final String namespace = ComponentNameComposer.getNamespaceFromURI(componentUri);
                        final QName refServiceName = new QName(namespace, serviceName);
                        if (!containsServiceRef(ccim.getComponent().getReferences(), serviceName)) {
                            throw new SwitchYardException("Could not find the service reference for '" + serviceName + "'" 
                            + " which is referenced in " + to);
                        }
                        
                        final ServiceReference service = getServiceDomain().getServiceReference(refServiceName);
                        if (service == null) {
                            throw new SwitchYardException("Could not find the service name '" + serviceName + "'" 
                            + " which is referenced in " + to);
                        }
                    }
                }
            }
        }
    }

    private boolean containsServiceRef(final List<ComponentReferenceModel> refs, final String serviceName) {
        for (ComponentReferenceModel refModel : refs) {
            if (refModel.getName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    private void verifyRouteDefinitions(List<RouteDefinition> routeDefinitions, CamelComponentImplementationModel ccim) throws Exception {
        // service name & namespace
        // TODO what happens when we have multiple services?
        String serviceName = ccim.getComponent().getServices().get(0).getName();
        String compositeNs = ccim.getComponent().getComposite().getTargetNamespace();

        // number of switchyard:// consumers/from statements
        int serviceConsumer = 0;
        for (RouteDefinition routeDefinition : routeDefinitions) {
            if (routeDefinition.getInputs().isEmpty()) {
                throw new SwitchYardException("Every route must have at least one input");
            }
            for (FromDefinition fromDefinition : routeDefinition.getInputs()) {
                URI from = URI.create(fromDefinition.getUri());
                if (from.getScheme().equals(CamelConstants.SWITCHYARD_COMPONENT_NAME)) {
                    if (serviceConsumer > 0) {
                        throw new SwitchYardException("Only one switchyard input per implementation is allowed");
                    }
                    String host = from.getHost();
                    String namespace = ComponentNameComposer.getNamespaceFromURI(from);

                    if (!serviceName.equals(host) || !compositeNs.equals(namespace)) {
                        throw new SwitchYardException("The implementation consumer doesn't match expected service " + serviceName + " and namespace " + namespace);
                    }
                    serviceConsumer++;
                }
            }

            List<ProcessorDefinition<?>> outputs = routeDefinition.getOutputs();
            for (ProcessorDefinition<?> processorDefinition : outputs) {
                if (processorDefinition instanceof ToDefinition) {
                    ToDefinition to = (ToDefinition) processorDefinition;
                    final URI componentUri = URI.create(to.getUri());
                    if (componentUri.getScheme().equals(CamelConstants.SWITCHYARD_COMPONENT_NAME)) {
                        final String referenceName = componentUri.getHost();
                        final String namespace = ComponentNameComposer.getNamespaceFromURI(componentUri);
                        final QName refServiceName = new QName(namespace, referenceName);
                        if (!containsServiceRef(ccim.getComponent().getReferences(), referenceName)) {
                            throw new SwitchYardException("Could not find the service reference for '" + referenceName + "'" 
                            + " which is referenced in " + to);
                        }
                        
                        final ServiceReference service = getServiceDomain().getServiceReference(refServiceName);
                        if (service == null) {
                            throw new SwitchYardException("Could not find the service name '" + referenceName + "'" 
                            + " which is referenced in " + to);
                        }
                    }
                }
            }
        }
        if (serviceConsumer != 1) {
            throw new SwitchYardException("Can not create camel based component implementation without consuming from switchyard service");
        }
    }


    /**
     * There are two options for Camel implementation : Spring XML or Java DSL.
     * This method figures out which one were dealing with and returns the
     * corresponding RouteDefinition.
     */
    private List<RouteDefinition> getRouteDefinition(CamelComponentImplementationModel model) {
        List<RouteDefinition> routes = RouteFactory.getRoutes(model);
        for (RouteDefinition route : routes) {
            SwitchYardRouteDefinition.addNamespaceParameter(route, model.getComponent().getTargetNamespace());
        }
        return routes;
    }

}
