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
 
package org.switchyard.component.soap.util;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.log4j.Logger;
import org.switchyard.common.xml.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Contains utility methods to examine/manipulate SOAP Messages.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 */
public final class SOAPUtil {
    private static final Logger LOGGER = Logger.getLogger(SOAPUtil.class);

    /**
     * SOAP 1.1 namespace.
     */
    public static final String SOAP11_URI = "http://schemas.xmlsoap.org/soap/envelope/";

    /**
     * SOAP 1.2 namespace.
     */
    public static final String SOAP12_URI = "http://www.w3.org/2003/05/soap-envelope";

    /**
     * SOAP 1.1 Server Fault Qname.
     */
    public static final QName SOAP11_SERVER_FAULT_TYPE = new QName(SOAP11_URI, "Server");

    /**
     * SOAP 1.1 Fault QName.
     */
    public static final QName SOAP11_FAULT_MESSAGE_TYPE = new QName(SOAP11_URI, "Fault");

    /**
     * SOAP 1.2 Server Fault Qname.
     */
    public static final QName SOAP12_RECEIVER_FAULT_TYPE = new QName(SOAP12_URI, "Receiver");

    /**
     * SOAP 1.2 Fault QName.
     */
    public static final QName SOAP12_FAULT_MESSAGE_TYPE = new QName(SOAP12_URI, "Fault");

    private static final boolean RETURN_STACK_TRACES = false;

    /** SOAP Message Factory holder. */
    private static final MessageFactory SOAP11_MESSAGE_FACTORY;
    private static final MessageFactory SOAP12_MESSAGE_FACTORY;

    private SOAPUtil() {
    }
    
    /**
     * Retrieves the first element name in the SOAP Envelope's body.
     *
     * @param soapMessage The SOAP message.
     * @return The first Element name.
     * @throws SOAPException If the SOAP message is invalid
     */
    public static String getFirstBodyElement(final SOAPMessage soapMessage) throws SOAPException {
        String operationName = null;
        SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
        
        if (body != null) {
            Iterator<Node> nodes = body.getChildElements();
            Node node = null;
            while (nodes.hasNext()) {
                node = nodes.next();
                if (node instanceof Element) {
                    operationName = node.getLocalName();
                }
            }
        }
        
        return operationName;
    }

    /**
     * Determines if the envelope is SOAP 1.1 or 1.2.
     *
     * @param soapMessage The SOAPMessage
     * @return The true if envelope is SOAP 1.2
     * @throws SOAPException If the envelope could not be read
     */
    public static Boolean isSOAP12(SOAPMessage soapMessage) throws SOAPException {
        return soapMessage.getSOAPPart().getEnvelope().getNamespaceURI().equals(SOAP12_URI);
    }

    /**
     * Adds a SOAP 1.1 or 1.2 Fault element to the SOAPBody.
     *
     * @param soapMessage The SOAPMessage
     * @return The SOAPFault that was added
     * @throws SOAPException If the fault could not be generated
     */
    public static SOAPFault addFault(SOAPMessage soapMessage) throws SOAPException {
        if (isSOAP12(soapMessage)) {
            return soapMessage.getSOAPBody().addFault(SOAP12_FAULT_MESSAGE_TYPE, "Send failed");
        } else {
            return soapMessage.getSOAPBody().addFault(SOAP12_FAULT_MESSAGE_TYPE, "Send failed");
        }
    }

    /**
     * Generates a SOAP 1.1 or 1.2 Fault Message based on binding id and Exception passed.
     *
     * @param th The Exception
     * @param bindingId SOAPBinding type
     * @return The SOAP Message containing the Fault
     * @throws SOAPException If the message could not be generated
     */
    public static SOAPMessage generateFault(final Throwable th, final String bindingId) throws SOAPException {
        if (bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING) || bindingId.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
            return generateSOAP12Fault(th);
        } else {
            return generateSOAP11Fault(th);
        }
    }

    /**
     * Generates a SOAP 1.1 Fault Message based on the Exception passed.
     *
     * @param th The Exception.
     * @return The SOAP Message containing the Fault.
     * @throws SOAPException If the message could not be generated.
     */
    public static SOAPMessage generateSOAP11Fault(final Throwable th) throws SOAPException {
        SOAPMessage faultMsg = SOAP11_MESSAGE_FACTORY.createMessage();
        return generateFault(th, faultMsg, SOAP11_SERVER_FAULT_TYPE);
    }

    /**
     * Generates a SOAP 1.2 Fault Message based on the Exception passed.
     *
     * @param th The Exception.
     * @return The SOAP Message containing the Fault.
     * @throws SOAPException If the message could not be generated.
     */
    public static SOAPMessage generateSOAP12Fault(final Throwable th) throws SOAPException {
        final SOAPMessage faultMsg = SOAP12_MESSAGE_FACTORY.createMessage();
        return generateFault(th, faultMsg, SOAP12_RECEIVER_FAULT_TYPE);
    }

    private static SOAPMessage generateFault(final Throwable th, final SOAPMessage faultMsg, final QName faultQname) throws SOAPException {
        if (LOGGER.isDebugEnabled()) {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            th.printStackTrace(pw);
            pw.flush();
            pw.close();
            LOGGER.debug(sw.toString());
        }
        if (th instanceof SOAPFaultException) {
            // Copy the Fault from the exception
            SOAPFault exFault = ((SOAPFaultException) th).getFault();
            SOAPFault fault = faultMsg.getSOAPBody().addFault(exFault.getFaultCodeAsQName(), exFault.getFaultString());
            fault.addNamespaceDeclaration(fault.getElementQName().getPrefix(), faultQname.getNamespaceURI());
            fault.setFaultActor(exFault.getFaultActor());
            if (exFault.hasDetail()) {
                Detail exDetail = exFault.getDetail();
                Detail detail = fault.addDetail();
                for (Iterator<DetailEntry> entries = exDetail.getDetailEntries(); entries.hasNext();) {
                    Node entryImport = detail.getOwnerDocument().importNode(entries.next(), true);
                    detail.appendChild(entryImport);
                }
            }
        } else {
            if (RETURN_STACK_TRACES) {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                th.printStackTrace(pw);
                pw.flush();
                pw.close();
                faultMsg.getSOAPBody().addFault(faultQname, sw.toString());
            } else {
                String message = th.getMessage();
                if (message == null) {
                    message = th.toString();
                }
                faultMsg.getSOAPBody().addFault(faultQname, message);
            }
        }
        return faultMsg;
    }

    /**
     * Create a new document based on a SOAP Message.
     * @param soapRes the SOAP Message
     * @return the new document
     * @throws ParserConfigurationException for errors during creation
     * @throws XMLStreamException If the SOAP message could not be read
     */
    public static Document parseAsDom(String soapRes) throws ParserConfigurationException, XMLStreamException {
        if (!soapRes.trim().startsWith("<?xml")) {
            // without this, java 7 xml processing is unhappy (you get a NPE due to a null xml version when starting the document)
            soapRes = "<?xml version='1.0'?>\n" + soapRes;
        }
        final XMLEventReader reader = XMLHelper.getXMLEventReader(new StringReader(soapRes));
        return XMLHelper.createDocument(reader);
    }

    /**
     * Generate String representation of SOAP message from javax.xml.soap.SOAPMessage.
     * @param msg SOAPMessage to parse
     * @return String representation of SOAP message
     */
    public static String soapMessageToString(SOAPMessage msg) {
        try {
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            StringWriter sw = new StringWriter();
            DOMSource source = new DOMSource(msg.getSOAPPart().getDocumentElement());
            StreamResult result = new StreamResult(sw);
            transformer.transform(source, result);
            return sw.toString();
        } catch (Exception e) {
            LOGGER.error("Could not parse SOAP Message", e);
            return null;
        }
    }

    /**
     * Creates a SOAP Message of version 1.1 or 1.2 based on binding id. The binding Id 
     * can be one of javax.xml.ws.soap.SOAPBinding ids.
     * 
     * @param bindingId SOAPBinding type
     * @return javax.xml.soap.SOAPMessage
     * @throws SOAPException If the message could not be generated.
     */
    public static SOAPMessage createMessage(String bindingId) throws SOAPException {
        SOAPMessage message = null;
        if (bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING) || bindingId.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
            message = SOAP12_MESSAGE_FACTORY.createMessage();
        } else {
            message = SOAP11_MESSAGE_FACTORY.createMessage();
        }
        return message;
    }

    /**
     * Returns the SOAP Message factory of version 1.1 or 1.2 based on binding id. The binding Id 
     * can be one of javax.xml.ws.soap.SOAPBinding ids.
     * 
     * @param bindingId SOAPBinding type
     * @return javax.xml.soap.MessageFactory
     */
    public static MessageFactory getFactory(String bindingId) {
        MessageFactory factory = null;
        if (bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING) || bindingId.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
            factory = SOAP12_MESSAGE_FACTORY;
        } else {
            factory = SOAP11_MESSAGE_FACTORY;
        }
        return factory;
    }

    static {
        MessageFactory soapMessageFactory = null;
        try {
            soapMessageFactory = MessageFactory.newInstance();
        } catch (final SOAPException soape) {
            LOGGER.error("Could not instantiate SOAP 1.1 Message Factory", soape);
        }
        SOAP11_MESSAGE_FACTORY = soapMessageFactory;

        soapMessageFactory = null;
        try {
            soapMessageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        } catch (final SOAPException soape) {
            LOGGER.error("Could not instantiate SOAP 1.2 Message Factory", soape);
        }
        SOAP12_MESSAGE_FACTORY = soapMessageFactory;
    }
}
