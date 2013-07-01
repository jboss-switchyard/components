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
package org.switchyard.component.bpel.riftsaw;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFault;

import org.apache.log4j.Logger;
import org.riftsaw.engine.Fault;
import org.riftsaw.engine.Service;
import org.riftsaw.engine.ServiceLocator;
import org.switchyard.Exchange;
import org.switchyard.ExchangeState;
import org.switchyard.HandlerException;
import org.switchyard.Message;
import org.switchyard.Scope;
import org.switchyard.ServiceDomain;
import org.switchyard.ServiceReference;
import org.switchyard.SynchronousInOutHandler;
import org.switchyard.component.bpel.BPELFault;
import org.switchyard.config.model.implementation.bpel.BPELComponentImplementationModel;
import org.switchyard.component.common.label.EndpointLabel;
import org.switchyard.config.model.composite.ComponentReferenceModel;
import org.switchyard.exception.DeliveryException;
import org.switchyard.exception.SwitchYardException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class implements the service locator interface to retrieve a
 * reference to an external service (provided by switchyard) for use
 * by a BPEL process instance.
 *
 */
public class RiftsawServiceLocator implements ServiceLocator {

    private static final Logger LOG = Logger.getLogger(RiftsawServiceLocator.class);
    
    private static final long DEFAULT_TIMEOUT = 120000;

    private Map<QName, ServiceDomain> _serviceDomains = new HashMap<QName, ServiceDomain>();
    private java.util.Map<QName, RegistryEntry> _registry=new java.util.HashMap<QName, RegistryEntry>();
    private long _waitTimeout = DEFAULT_TIMEOUT;
    
    /**
     * This is the constructor for the riftsaw service locator.
     *
     */
    public RiftsawServiceLocator() {
    }
    
    /**
     * Add a service -> service domain mapping.
     * @param serviceName service name
     * @param serviceDomain The service domain
     */
    public void addServiceDomain(QName serviceName, ServiceDomain serviceDomain) {
        _serviceDomains.put(serviceName, serviceDomain);
    }
    
    /**
     * Remove a service -> service domain mapping.
     * @param serviceName the service name
     */
    public void removeServiceDomain(QName serviceName) {
        _serviceDomains.remove(serviceName);
    }

    /**
     * This method returns the service domain for a given service.
     * @param serviceName service name
     * @return The service domain
     */
    public ServiceDomain getServiceDomain(QName serviceName) {
        return _serviceDomains.get(serviceName);
    }
    
    /**
     * This method returns the service associated with the supplied
     * process, service and port.
     * 
     * @param processName The process name
     * @param serviceName The service name
     * @param portName The port name
     * @return The service or null if not found
     */
    public Service getService(QName processName, QName serviceName, String portName) {
        // Currently need to just use the local part, without the version number, to
        // lookup the registry entry
        int index=processName.getLocalPart().indexOf('-');
        QName localProcessName=new QName(processName.getNamespaceURI(),
                    processName.getLocalPart().substring(0, index));
        
        RegistryEntry re=_registry.get(localProcessName);
        
        if (re == null) {
            LOG.error("No service references found for process '"+localProcessName+"'");
            return (null);
        }
        
        Service ret=re.getService(serviceName, portName, _serviceDomains.get(serviceName));
        
        if (ret == null) {
            LOG.error("No service found for '"+serviceName+"' (port "+portName+")");
        }
        
        return (ret);
    }
    
    /**
     * This method registers a component reference against the service BPEL
     * process, for use when it calls out to the external service.
     * 
     * @param crm The component reference
     */
    public void initialiseReference(ComponentReferenceModel crm) {
        
        // Find the BPEL implementation associated with the reference
        if (crm.getComponent() != null
                    && crm.getComponent().getImplementation() instanceof BPELComponentImplementationModel) {
            BPELComponentImplementationModel impl=
                    (BPELComponentImplementationModel)crm.getComponent().getImplementation();
            
            QName processName=impl.getProcessQName();
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Register reference "+crm.getName()+" ("+crm.getQName()+") for process "+processName);
            }
            
            RegistryEntry re=_registry.get(processName);
            
            if (re == null) {
                re = new RegistryEntry();
                _registry.put(processName, re);
            }
            
            javax.wsdl.Definition wsdl=WSDLHelper.getWSDLDefinition(crm.getInterface().getInterface());
            javax.wsdl.PortType portType=WSDLHelper.getPortType(crm.getInterface().getInterface(), wsdl);
            
            re.register(portType, crm.getQName());

        } else {
            throw new SwitchYardException("Could not find BPEL implementation associated with reference");
        }
        
    }
    
    /**
     * This class provides a registry entry for use in looking up the
     * appropriate service to use for an external BPEL invoke.
     *
     */
    public class RegistryEntry {

        private java.util.List<javax.wsdl.PortType> _portTypes=
                    new java.util.Vector<javax.wsdl.PortType>();
        private java.util.List<QName> _services=
                    new java.util.Vector<QName>();

        /**
         * This method registers the wsdl, port type and service details.
         *
         * @param portType The port type
         * @param service The SwitchYard service
         */
        public void register(javax.wsdl.PortType portType, QName service) {
            _portTypes.add(portType);
            _services.add(service);
        }
        
        /**
         * This method returns the service associated with the supplied service and
         * port names.
         * 
         * @param serviceName The service name
         * @param portName The port name
         * @param serviceDomain The service domain
         * @return The service or null if not found
         */
        public Service getService(QName serviceName, String portName, ServiceDomain serviceDomain) {
            Service ret = null;
            for (int index = 0, count = _services.size(); index < count; ++index) {
                if (serviceName.equals(_services.get(index))) {
                    ServiceReference sref = serviceDomain.getServiceReference(serviceName);
                    if (sref != null) {
                        ret = new ServiceProxy(sref, _portTypes.get(index));
                    }
                    break;
                }
            }
            if (ret == null) {
                LOG.error("No service found for '" + serviceName);
            }
            return ret;
        }
    }

    /**
     * This class represents a service proxy, used by the BPEL engine to invoke
     * and external service. The proxy intercepts the request and applies
     * it to the appropriate switchyard service.
     *
     */
    public class ServiceProxy implements Service {
        
        private ServiceReference _serviceReference=null;
        private javax.wsdl.PortType _portType=null;
        
        /**
         * The constructor for the service proxy.
         * 
         * @param sref The service reference
         * @param portType The port type
         */
        public ServiceProxy(ServiceReference sref, javax.wsdl.PortType portType) {
            _serviceReference = sref;
            _portType = portType;
        }

        /**
         * {@inheritDoc}
         */
        public Element invoke(String operationName, Element mesg,
                Map<String, Object> headers) throws Exception {
            
            // Unwrap the first two levels, to remove the part wrapper
            mesg = WSDLHelper.unwrapMessagePart(mesg);
            
            // Need to create an exchange
            SynchronousInOutHandler rh = new SynchronousInOutHandler();
            Exchange exchange=_serviceReference.createExchange(operationName, rh);

            Message req = exchange.createMessage();
            req.setContent(mesg);
            if (headers != null) {
                Set<String> keys = headers.keySet();
                for (String key : keys) {
                    exchange.getContext(req).setProperty(key,headers.get(key)).addLabels(EndpointLabel.SOAP.label());
                }

                // Clear the headers in preparation for response headers
                headers.clear();
            }
            
            exchange.send(req);

            try {
                exchange = rh.waitForOut(_waitTimeout);
            } catch (DeliveryException e) {
                throw new HandlerException("Timed out after " + _waitTimeout
                        + " ms waiting on synchronous response from target service '"
                        + _serviceReference.getName() + "'.");
            }
            
            Message resp=exchange.getMessage();
            
            if (resp == null) {
                throw new Exception("Response not returned from operation '"
                           + operationName
                           + "' on service: "+_serviceReference.getName());                
            }
            
            // Process header values associated with the response
            for (org.switchyard.Property p : exchange.getContext().getProperties(Scope.MESSAGE)) {
                if (p.hasLabel(EndpointLabel.SOAP.label())) {
                    headers.put(p.getName(), p.getValue());
                }
            }
            
            // Check for exception - but don't rethrow a BPEL
            // fault as it will be converted to a message
            // response
            if (resp.getContent() instanceof Exception
                    && !(resp.getContent() instanceof BPELFault)) {
                throw (Exception)resp.getContent();
            }
            
            Element respelem=(Element)resp.getContent(Node.class);
            
            javax.wsdl.Operation operation=_portType.getOperation(operationName, null, null);
            
            if (exchange.getState() == ExchangeState.FAULT) {
                QName faultCode=null;
                
                if (respelem instanceof SOAPFault) {
                    SOAPFault fault=(SOAPFault)respelem;
                    
                    respelem = (Element)fault.getDetail().getFirstChild();
                    
                    faultCode = fault.getFaultCodeAsQName();
                }
                
                Element newfault=WSDLHelper.wrapFaultMessagePart(respelem, operation, null);

                throw new Fault(faultCode, newfault);
            }
            
            Element newresp=WSDLHelper.wrapResponseMessagePart(respelem, operation);
            
            return ((Element)newresp);
        }
    }
    
}
