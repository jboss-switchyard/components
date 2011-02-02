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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

/**
 * Contains utility methods to examine/manipulate WSDLs.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 */
public final class WSDLUtil {

    private static final Map<Object, Definition> DEFINITIONS_MAP = Collections.synchronizedMap(new HashMap<Object, Definition>());

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
        Definition definition = null;
        if (DEFINITIONS_MAP.containsKey(wsdlLocation)) {
            definition = (Definition) DEFINITIONS_MAP.get(wsdlLocation);
        } else {
            WSDLFactory wsdlFactory = WSDLFactory.newInstance();
            WSDLReader reader = wsdlFactory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            definition = reader.readWSDL(wsdlLocation);
            DEFINITIONS_MAP.put(wsdlLocation, definition);
        }
        return definition;
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
                    "Unable to resolve imported document at '"
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
    private static URL getURL(final String path) throws MalformedURLException {
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file://")) {
            return new URL(null, path);
        } else {
            File localFile = new File(path);
            return localFile.toURL();
        }
    }

    /**
     * Get the Service from the WSDL given a port name string.
     *
     * The string can be in the form "{namespaceURI}serviceName:portName", with the "{namespaceURI}" and "serviceName:" part being optional. 
     * If the string is null then this method returns the first found Service. If no portName is available then use "serviceName:".
     *
     * @param wsdlLocation location pointing to a WSDL XML definition.
     * @param servicePort The port name.
     * @return the Service.
     * @throws WSDLException If the Service could not be retrieved.
     */
    public static Service getService(final String wsdlLocation, final String servicePort) throws WSDLException {
        Definition definition = readWSDL(wsdlLocation);
        QName serviceQName = null;
        if (servicePort != null) {
            int idx = servicePort.lastIndexOf(":") + 1;
            int idxQ1 = servicePort.indexOf("{") + 1;
            int idxQ2 = servicePort.indexOf("}");
            if (idx > 0) {
                String namespace = null;
                if (idxQ1 > 0) {
                    namespace = servicePort.substring(idxQ1, idxQ2);
                } else {
                    namespace = definition.getTargetNamespace();
                }
                idxQ2++;
                String serviceName = servicePort.substring(idxQ2, idx - 1);
                serviceQName = new QName(namespace, serviceName);
            }
        }
        if (serviceQName == null) {
            return (Service) definition.getServices().values().iterator().next();
        } else {
            Iterator<Service> services = definition.getServices().values().iterator();
            while (services.hasNext()) {
                Service wsdlService = services.next();
                if (wsdlService.getQName().equals(serviceQName)) {
                    return wsdlService;
                }
            }
        }
        throw new WSDLException("Could not find service " + servicePort + " in the WSDL " + wsdlLocation, null);
    }

    /**
     * Get the Port from the Service given a port name string.
     *
     * The string can be in the form "{namespaceURI}serviceName:portName", with the "{namespaceURI}" and "serviceName:" part being optional. 
     * If the string is null then this method returns the first found Service. If no portName is available then use "serviceName:".
     *
     * @param wsdlService The Service to be queried for.
     * @param servicePort The port name.
     * @return the Webservice Port.
     * @throws WSDLException If the Port could not be found.
     */
    public static Port getPort(final Service wsdlService, final String servicePort) throws WSDLException {
        String portName = null;
        if (servicePort != null) {
            int idx = servicePort.lastIndexOf(":") + 1;
            int idx2 = servicePort.length();
            portName = servicePort.substring(idx, idx2);
        }
        if ((portName == null) || (portName.length() == 0)) {
            return (Port) wsdlService.getPorts().values().iterator().next();
        } else {
            Iterator<Port> ports = wsdlService.getPorts().values().iterator();
            while (ports.hasNext()) {
                Port port = ports.next();
                if (port.getName().equals(portName)) {
                    return port;
                }
            }
        }
        throw new WSDLException("Could not find port " + portName + " in the Service " + wsdlService.getQName(), null);
    }
}
