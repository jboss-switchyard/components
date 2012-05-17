/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
 
package org.switchyard.component.soap.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.log4j.Logger;
import org.switchyard.ExchangePattern;
import org.switchyard.common.type.Classes;
import org.switchyard.common.xml.XMLHelper;
import org.switchyard.component.soap.PortName;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Contains utility methods to examine/manipulate WSDLs.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 */
public final class WSDLUtil {

    private static final Logger LOGGER = Logger.getLogger(WSDLUtil.class);

    /**
     * SOAP 1.1 QName namespace.
     */
    public static final String WSDL_SOAP11_URI = "http://schemas.xmlsoap.org/wsdl/soap/";

    /**
     * SOAP 1.2 QName namespace.
     */
    public static final String WSDL_SOAP12_URI = "http://schemas.xmlsoap.org/wsdl/soap12/";

    private WSDLUtil() {
    }

    /**
     * Read the WSDL document and create a WSDL Definition.
     *
     * @param wsdlLocation location pointing to a WSDL XML definition.
     * @return the Definition.
     * @throws WSDLException If unable to read the WSDL
     */
    public static Definition readWSDL(final String wsdlLocation) throws WSDLException {
        InputStream inputStream = null;
        try {
            URL url = getURL(wsdlLocation);
            inputStream = url.openStream();
            InputSource source = new InputSource(inputStream);
            source.setSystemId(url.toString());
            Document wsdlDoc = XMLHelper.getDocument(source);
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            WSDLReader reader = wsdlFactory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            return reader.readWSDL(url.toString(), wsdlDoc);
        } catch (Exception e) {
            throw new WSDLException(WSDLException.OTHER_ERROR,
                    "Unable to read WSDL at '"
                    + wsdlLocation, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioe) {
                    LOGGER.error(ioe);
                }
            }
        }
    }

    /**
     * Read the WSDL document accessible via the specified
     * URI into a StreamSource.
     *
     * @param wsdlURI a URI (can be a filename or URL) pointing to a
     * WSDL XML definition.
     * @return the StreamSource.
     * @throws WSDLException If unable to read the WSDL
     */
    public static StreamSource getStream(final String wsdlURI) throws WSDLException {
        try {
            URL url = getURL(wsdlURI);
            InputStream inputStream = url.openStream();
            StreamSource inputSource = new StreamSource(inputStream);
            inputSource.setSystemId(url.toString());
            return inputSource;
        } catch (Exception e) {
            throw new WSDLException(WSDLException.OTHER_ERROR,
                    "Unable to resolve WSDL document at '"
                    + wsdlURI, e);
        }
    }

    /**
     * Convert a path/uri to a URL.
     *
     * @param path a path or URI.
     * @return the URL.
     * @throws MalformedURLException If the url path is not valid
     */
    public static URL getURL(final String path) throws MalformedURLException {
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file://")) {
            return new URL(null, path);
        } else {
            URL url;
            try {
                url = Classes.getResource(path, WSDLUtil.class);
            } catch (IOException ioe) {
                url = null;
            }
            if (url == null) {
                File localFile = new File(path);
                url = localFile.toURI().toURL();
            }
            return url;
        }
    }

    /**
     * Get the Service from the WSDL given a PortName.
     * If the PortName.getServiceQName() is empty (QName("")) then this method returns the first found Service.
     *
     * @param wsdlLocation location pointing to a WSDL XML definition.
     * @param portName the PortName.
     * @return the Service.
     * @throws WSDLException If the Service could not be retrieved.
     */
    public static Service getService(final String wsdlLocation, final PortName portName) throws WSDLException {
        Definition definition = readWSDL(wsdlLocation);
        Service service = null;
        if (portName.getServiceQName().equals(new QName(""))) {
            service = (Service) definition.getServices().values().iterator().next();
            portName.setServiceQName(service.getQName());
        } else {
            String namespace = portName.getNamespaceURI();        
            if (namespace.equals(XMLConstants.NULL_NS_URI)) {
                namespace = definition.getTargetNamespace();
            }
            QName serviceQName = new QName(namespace, portName.getServiceName());
            Iterator<Service> services = definition.getServices().values().iterator();
            while (services.hasNext()) {
                Service wsdlService = services.next();
                if (wsdlService.getQName().equals(serviceQName)) {
                    service =  wsdlService;
                    break;
                }
            }
        }
        if (service == null) {
            throw new WSDLException("Could not find service " + portName + " in the WSDL " + wsdlLocation, null);
        }
        return service;
    }

    /**
     * Get the Port from the Service given a port name string. If the PortName.getName() is null then this method returns the first found Port.
     *
     * @param wsdlService the Service to be queried for.
     * @param portName the PortName.
     * @return the Webservice Port.
     * @throws WSDLException If the Port could not be found.
     */
    public static Port getPort(final Service wsdlService, final PortName portName) throws WSDLException {
        String name = portName.getName();
        Port port = null;
        if ((name == null) || (name.length() == 0)) {
            port = (Port) wsdlService.getPorts().values().iterator().next();
        } else {
            Iterator<Port> ports = wsdlService.getPorts().values().iterator();
            while (ports.hasNext()) {
                Port wsdlPort = ports.next();
                if (wsdlPort.getName().equals(name)) {
                    port = wsdlPort;
                    break;
                }
            }
        }
        if (port == null) {
            throw new WSDLException("Could not find port " + portName + " in the Service " + wsdlService.getQName(), null);
        }
        return port;
    }

    /**
     * Get the SOAP {@link Operation} instance for the specified SOAP operation name.
     * @param port The WSDL port.
     * @param elementName The SOAP Body element name.
     * @return The Operation instance, or null if the operation was not found on the port.
     */
    public static Operation getOperation(Port port, String elementName) {
        
        List<Operation> operations = port.getBinding().getPortType().getOperations();
        
        for (Operation operation : operations) {
            Part part = (Part)operation.getInput().getMessage().getParts().values().iterator().next();
            if (elementName.equals(part.getElementName().getLocalPart())) {
                return operation;
            }
        }
        return null;
    }

    /**
     * Get the SOAP Binding Id for the specified {@link Port}.
     *
     * @param port The WSDL port.
     * @return The SOAPBinding Id found on the port.
     */
    public static String getBindingId(Port port) {
        String bindingId = SOAPBinding.SOAP11HTTP_BINDING;
        List<ExtensibilityElement> extElements = port.getExtensibilityElements();
        for (ExtensibilityElement extElement : extElements) {
            if (extElement.getElementType().getNamespaceURI().equals(WSDL_SOAP12_URI)) {
                bindingId = SOAPBinding.SOAP12HTTP_BINDING;
            }
            break;
        }
        return bindingId;
    }

    /**
     * Check if we are invoking a @Oneway annotated method.
     *
     * @param port The WSDL service port.
     * @param elementName The SOAP Body element name.
     * @return True if there is no response to be expected.
     */
    public static boolean isOneWay(final Port port, final String elementName) {
        // Overloaded methods not supported
        // Encrypted messages will be treated as request-response as it cannot be decrypted
        Operation operation = getOperation(port, elementName);
        return isOneWay(operation);
    }
    
    /**
     * Check if we are invoking a @Oneway annotated method.
    *
    * @param operation The WSDL Operation.
    * @return True if there is no response to be expected.
    */
   public static boolean isOneWay(final Operation operation) {
       boolean isOneWay = false;
       if (operation != null) {
           isOneWay = operation.getStyle().equals(OperationType.ONE_WAY);
       }
       return isOneWay;
   }

   /**
     * Get the SOAP {@link BindingOperation} instance for the specified SOAP operation name.
     * @param port The WSDL port.
     * @param elementName The SOAP Body element name.
     * @return The BindingOperation instance, or null if the operation was not found on the port.
     */
    public static BindingOperation getBindingOperation(Port port, String elementName) {
        Operation operation = getOperation(port, elementName);
        if (operation != null) {
            List<BindingOperation> bindingOperations = port.getBinding().getBindingOperations();
            for (BindingOperation bindingOperation : bindingOperations) {
                if (bindingOperation.getName().equals(operation.getName())) {
                    return bindingOperation;
                }
            }
        }
        return null;
    }

    /**
     * Get the soapAction value for a given operation.
     *
     * @param port The WSDL service port.
     * @param elementName The SOAP Body element name.
     * @return the soapAction value if it exists.
     */
    public static String getSoapAction(final Port port, final String elementName) {
        // Overloaded methods not supported
        BindingOperation operation = getBindingOperation(port, elementName);
        return getSoapAction(operation);
    }

    /**
     * Get the soapAction value for a given operation.
     *
     * @param operation The WSDL BindingOperation.
     * @return the soapAction value if it exists.
     */
    public static String getSoapAction(final BindingOperation operation) {
        String soapActionUri = "";
        if (operation != null) {
            List<ExtensibilityElement> extElements = operation.getExtensibilityElements();
            for (ExtensibilityElement extElement : extElements) {
                if (extElement instanceof SOAPOperation) {
                    soapActionUri = ((SOAPOperation) extElement).getSoapActionURI();
                    break;
                } else if (extElement instanceof SOAP12Operation) {
                    SOAP12Operation soapOperation = ((SOAP12Operation) extElement);
                    Boolean soapActionRequired = soapOperation.getSoapActionRequired();
                    if ((soapActionRequired == null) || soapActionRequired) {
                        soapActionUri = soapOperation.getSoapActionURI();
                    }
                    break;
                }
            }
        }
        return soapActionUri;
    }

    /**
     * Get the methods Input message's name.
     *
     * @param port The WSDL service port.
     * @param operationName The name of the operation obtained from SOAP message.
     * @return The local name of the input message.
     */
    public static String getMessageLocalName(final Port port, final String operationName) {
        QName messageName = getMessageQName(port, operationName);
        if (messageName != null) {
            return messageName.getLocalPart();
        }
        return null;
    }

    /**
     * Get the methods Input message's name.
     *
     * @param port The WSDL service port.
     * @param operationName The name of the operation obtained from SOAP message.
     * @return The QName name of the input message.
     */
    public static QName getMessageQName(final Port port, final String operationName) {
        QName messageName = null;
        // Overloaded methods not supported
        // Encrypted messages will be treated as request-response as it cannot be decrypted
        Operation operation = getOperation(port, operationName);
        if (operation != null) {
            messageName = operation.getInput().getMessage().getQName();
        }
        return messageName;
    }

    /**
     * Given a Port construct the Exchange Contracts for all Operations.
     *
     * @param port The WSDL service port.
     * @param service The SwitchYard service.
     * @return A Map of exchange contracts.
     * @throws WebServicePublishException If the WSDL does not match the Service operations. 
     
    public static Map<String, BaseExchangeContract> getContracts(final Port port, final org.switchyard.ServiceReference service) throws WebServicePublishException {
        Map<String, BaseExchangeContract> contracts = new HashMap<String, BaseExchangeContract>();
        List<Operation> operations = port.getBinding().getPortType().getOperations();
        if ((operations == null) || operations.isEmpty()) {
            throw new WebServicePublishException("Invalid WSDL. No operations found.");
        }
        for (Operation operation : operations) {
            String name = operation.getName();
            ServiceOperation targetServiceOperation = service.getInterface().getOperation(name);
            if (targetServiceOperation == null) {
                throw new WebServicePublishException("WSDL Operation " + name + " not found in Service " + service.getName());
            }
            ExchangePattern wsdlExchangePattern = getExchangePattern(operation);
            if (targetServiceOperation.getExchangePattern() != wsdlExchangePattern) {
                throw new WebServicePublishException("WSDL Operation " + name + " does not match Service Operation " + targetServiceOperation.getName());
            }
            BaseExchangeContract exchangeContract = new BaseExchangeContract(targetServiceOperation);
            BaseInvocationContract soapMetaData = exchangeContract.getInvokerInvocationMetaData();
            List<Part> parts = operation.getInput().getMessage().getOrderedParts(null);
            if (parts.isEmpty()) {
                throw new WebServicePublishException("WSDL Operation " + name + " does not have any input Message parts");
            }
            // Only one Part (one child of the soap:body) allowed per WS-I Basic Profile similar to Document/Literal wrapped
            QName inputMessageQName = parts.get(0).getElementName();
            soapMetaData.setInputType(inputMessageQName);
            soapMetaData.setFaultType(SOAP_FAULT_MESSAGE_TYPE);

            if (!isOneWay(operation)) {
                parts = operation.getOutput().getMessage().getOrderedParts(null);
                if (parts.isEmpty()) {
                    throw new WebServicePublishException("WSDL Operation " + name + " does not have any ouput Message parts");
                }
                // Only one Part (one child of the soap:body) allowed per WS-I Basic Profile similar to Document/Literal wrapped
                QName outputMessageQName = parts.get(0).getElementName();
                soapMetaData.setOutputType(outputMessageQName);
            }
            contracts.put(name, exchangeContract);
        }
        return contracts;
    }
    */

    /**
     * Get the exchange pattern for the specified WS Operation.
     *
     * @param operation The operation to check for.
     * @return The Exchange Pattern.
     */
    public static ExchangePattern getExchangePattern(final Operation operation) {
        if (operation.getStyle().equals(OperationType.ONE_WAY)) {
            return ExchangePattern.IN_ONLY;
        } else {
            return ExchangePattern.IN_OUT;
        }
    }
}
