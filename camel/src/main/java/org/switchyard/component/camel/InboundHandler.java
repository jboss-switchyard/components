/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.switchyard.component.camel;

import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.switchyard.Exchange;
import org.switchyard.ExchangeHandler;
import org.switchyard.ServiceReference;
import org.switchyard.component.camel.config.model.CamelBindingModel;
import org.switchyard.component.camel.config.model.OperationSelector;
import org.switchyard.metadata.ServiceOperation;

/**
 * An {@link ExchangeHandler} that acts as a gateway/entrypoint for Camel Components.
 * 
 * This gives access to all component of Apache Camel and works by creating a
 * Camel route that looks something like this 
 * <pre>
 * from("CamelComponentURI").to("switchyard://serviceName?operationName=operationName"); 
 * </pre>
 * 
 * @author Daniel Bevenius
 *
 */
public class InboundHandler implements ExchangeHandler {
    
    private final CamelBindingModel _camelBindingModel;
    private final CamelContext _camelContext;

    /**
     * Sole constructor.
     * 
     * @param camelBindingModel The {@link CamelBindingModel}.
     * @param camelContext The {@link CamelContext}.
     */
    public InboundHandler(final CamelBindingModel camelBindingModel, final CamelContext camelContext) {
        _camelBindingModel = camelBindingModel;
        _camelContext = camelContext;
    }
    
    /**
     * Will create the Camel route and add it to the {@link CamelContext}.
     * 
     * @param serviceReference The target service reference.
     * @throws Exception If an error occurs while creating the route definition.
     */
    public void start(final ServiceReference serviceReference) throws Exception {
        final RouteDefinition rd = new RouteDefinition();
        rd.routeId(composeRouteId(serviceReference));
        rd.from(uriFromBindingModel());
        rd.to(composeSwitchYardComponentName(serviceReference));
        
        _camelContext.addRouteDefinition(rd);
    }
    
    private String composeSwitchYardComponentName(final ServiceReference serviceReference) {
        final StringBuilder sb = new StringBuilder();
        sb.append("switchyard://").append(serviceReference.getName().getLocalPart());
        sb.append("?operationName=").append(operationName(serviceReference));
        return sb.toString();
    }
    
    private String uriFromBindingModel() {
        return _camelBindingModel.getComponentURI().toString();
    }
    
    private String operationName(final ServiceReference serviceRef) {
        final OperationSelector os = _camelBindingModel.getOperationSelector();
        if (os != null) {
            return _camelBindingModel.getOperationSelector().getOperationName();
        }
        
        return lookupOperationNameFor(serviceRef);
    }
    
    private String lookupOperationNameFor(final ServiceReference serviceRef) {
        final Set<ServiceOperation> operations = serviceRef.getInterface().getOperations();
        if (operations.size() != 1) {
            final StringBuilder msg = new StringBuilder();
            msg.append("No operationSelector was configured for the Camel Component and the Service Interface ");
            msg.append("contains more than one operation: ").append(operations);
            msg.append("Please add an operationSelector element with the target 'operationName' as an attribute.");
            throw new RuntimeException(msg.toString());
        }
        final ServiceOperation serviceOperation = operations.iterator().next();
        return serviceOperation.getName();
    }
    
    private String composeRouteId(final ServiceReference serviceRef) {
        return serviceRef.getName().toString() + "-[" +_camelBindingModel.getComponentURI() + "]";
    }
    
    /**
     * Removes the route associated with the {@link ServiceReference}.
     * 
     * @param serviceRef The {@link ServiceReference} of the target service.
     * @throws Exception If an error occurs while trying to removed the route from the {@link CamelContext}.
     */
    public void stop(final ServiceReference serviceRef) throws Exception {
        final RouteDefinition rd = _camelContext.getRouteDefinition(composeRouteId(serviceRef));
        _camelContext.removeRouteDefinition(rd);
    }

    /**
     * This is a noop for this handler. This handler is only responsible for setting up
     * a route in camel and the {@link SwitchyardComponent} will take care of calling
     * the configured SwitchYard service.
     * 
     * @param switchYardExchange Message exchange.
     */
    @Override
    public void handleMessage(final Exchange switchYardExchange) {
    }

    @Override
    public void handleFault(final Exchange exchange) {
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_camelBindingModel == null) ? 0 : _camelBindingModel.getComponentURI().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final InboundHandler other = (InboundHandler) obj;
        if (_camelBindingModel == null) {
            if (other._camelBindingModel != null) {
                return false;
            }
        } else if (!_camelBindingModel.getComponentURI().equals(other._camelBindingModel.getComponentURI())) {
            return false;
        }
        return true;
    }
    
}
