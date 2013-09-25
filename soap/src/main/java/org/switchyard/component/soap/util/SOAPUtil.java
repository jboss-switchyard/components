/*
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors.
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
 
package org.switchyard.component.soap.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;

import javax.activation.DataSource;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

import org.jboss.logging.Logger;
import org.switchyard.Context;
import org.switchyard.Property;
import org.switchyard.common.codec.Base64;
import org.switchyard.common.xml.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.switchyard.component.soap.SOAPLogger;
import org.switchyard.component.soap.SOAPMessages;

/**
 * Contains utility methods to examine/manipulate SOAP Messages.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 */
public final class SOAPUtil {
    private static final Logger LOGGER = Logger.getLogger(SOAPUtil.class);

    /**
     * SwitchYard Context key.
     */
    public static final String SWITCHYARD_CONTEXT = "SWITCHYARD_CONTEXT";

    /**
     * SOAP 1.1 namespace.
     */
    public static final String SOAP11_URI = "http://schemas.xmlsoap.org/soap/envelope/";

    /**
     * SOAP 1.2 namespace.
     */
    public static final String SOAP12_URI = "http://www.w3.org/2003/05/soap-envelope";

    /**
     * WS-A namespace.
     */
    public static final String WSA_URI = "http://www.w3.org/2005/08/addressing";

    /**
     * MTOM XOP namespace.
     */
    public static final String MTOM_XOP_URI = "http://www.w3.org/2004/08/xop/include";

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

    /**
     * The WS-A Action QName.
     */
    public static final QName WSA_ACTION_QNAME = new QName(WSA_URI, "Action");

    /**
     * The WS-A From QName.
     */
    public static final QName WSA_FROM_QNAME = new QName(WSA_URI, "From");

    /**
     * The WS-A MessageID QName.
     */
    public static final QName WSA_MESSAGEID_QNAME = new QName(WSA_URI, "MessageID");

    /**
     * The WS-A ReplyTo QName.
     */
    public static final QName WSA_REPLYTO_QNAME = new QName(WSA_URI, "ReplyTo");

    /**
     * The WS-A FaultTo QName.
     */
    public static final QName WSA_FAULTTO_QNAME = new QName(WSA_URI, "FaultTo");

    /**
     * The WS-A RelatesTo QName.
     */
    public static final QName WSA_RELATESTO_QNAME = new QName(WSA_URI, "RelatesTo");

    /**
     * The WS-A To QName.
     */
    public static final QName WSA_TO_QNAME = new QName(WSA_URI, "To");
    /**
     * The MTOM/XOP Include QName.
     */
    public static final QName MTOM_XOP_INCLUDE_QNAME = new QName(MTOM_XOP_URI, "Include");

    /**
     * The WS-A Action QName String.
     */
    public static final String WSA_ACTION_STR = WSA_ACTION_QNAME.toString();

    /**
     * The WS-A From QName String.
     */
    public static final String WSA_FROM_STR = WSA_FROM_QNAME.toString();

    /**
     * The WS-A FaultTo QName String.
     */
    public static final String WSA_FAULTTO_STR = WSA_FAULTTO_QNAME.toString();

    /**
     * The WS-A ReplyTo QName String.
     */
    public static final String WSA_REPLYTO_STR = WSA_REPLYTO_QNAME.toString();

    /**
     * The WS-A RelatesTo QName String.
     */
    public static final String WSA_RELATESTO_STR = WSA_RELATESTO_QNAME.toString();

    /**
     * The WS-A To QName String.
     */
    public static final String WSA_TO_STR = WSA_TO_QNAME.toString();

    private static final boolean RETURN_STACK_TRACES = false;
    private static final String INDENT_FEATURE = "{http://xml.apache.org/xslt}indent-amount";
    private static final String INDENT_AMOUNT = "4";
    private static final String CID_STR = "cid:";
    private static final String HREF_STR = "href";

    /** SOAP Message Factory holder. */
    private static final MessageFactory SOAP11_MESSAGE_FACTORY;
    private static final MessageFactory SOAP12_MESSAGE_FACTORY;

    private SOAPUtil() {
    }
    
    /**
     * Retrieves the first element name in the SOAP Envelope's body.
     *
     * @param soapMessage The SOAP message.
     * @return The first Element QName.
     * @throws SOAPException If the SOAP message is invalid
     */
    public static QName getFirstBodyElement(final SOAPMessage soapMessage) throws SOAPException {
        QName operationName = null;
        SOAPBody body = soapMessage.getSOAPPart().getEnvelope().getBody();
        
        if (body != null) {
            Iterator<Node> nodes = body.getChildElements();
            Node node = null;
            while (nodes.hasNext()) {
                node = nodes.next();
                if (node instanceof Element) {
                    operationName = new QName(node.getNamespaceURI(), node.getLocalName());
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
     * Determines if the envelope has addressing action header.
     *
     * @param soapMessage The SOAPMessage
     * @return The action addressing header
     * @throws SOAPException If the envelope could not be read
     */
    public static String getAddressingAction(SOAPMessage soapMessage) throws SOAPException {
        String action = null;
        Iterator<SOAPHeaderElement> headers = soapMessage.getSOAPPart().getEnvelope().getHeader().examineAllHeaderElements();
        while (headers.hasNext()) {
            SOAPHeaderElement element = headers.next();
            if (element.getElementQName().equals(WSA_ACTION_QNAME)) {
                action = element.getValue();
                break;
            }
        }
        return action;
    }

    /**
     * Add/Replace all WS-A headers if found on the context.
     *
     * @param soapContext The SOAPMessageContext
     * @return The modified envelope
     * @throws SOAPException If the envelope could not be read
     */
    public static SOAPEnvelope addReplaceAddressingHeaders(SOAPMessageContext soapContext) throws SOAPException {
        SOAPEnvelope soapEnvelope = soapContext.getMessage().getSOAPPart().getEnvelope();
        Context context = (Context)soapContext.get(SWITCHYARD_CONTEXT);
        if (context != null) {
            soapEnvelope = addReplaceHeader(soapEnvelope, context, WSA_ACTION_STR);
            soapEnvelope = addReplaceHeader(soapEnvelope, context, WSA_FROM_STR);
            soapEnvelope = addReplaceHeader(soapEnvelope, context, WSA_TO_STR);
            soapEnvelope = addReplaceHeader(soapEnvelope, context, WSA_REPLYTO_STR);
            soapEnvelope = addReplaceHeader(soapEnvelope, context, WSA_FAULTTO_STR);
            soapEnvelope = addReplaceHeader(soapEnvelope, context, WSA_RELATESTO_STR);
        }
        return soapEnvelope;
    }

    /**
     * Replace a header node if the envelope has one or else add it.
     *
     * @param soapEnvelope The SOAPEnvelope
     * @param context The SwitchYard Context
     * @param property The context property
     * @return The modified envelope
     * @throws SOAPException If the envelope could not be read
     */
    public static SOAPEnvelope addReplaceHeader(SOAPEnvelope soapEnvelope, Context context, String property) throws SOAPException {
        Node header = (Node)context.getPropertyValue(property);
        if (header == null) {
            // When a ReplyTo header was added in Camel messgae header JAX-WS did not generate a MessageID
            // and ReplyTo headers. So allow lower case replyto header to be set in Camel
            header = (Node)context.getPropertyValue(property.toLowerCase());
        }
        if (header != null) {
            NodeList headers = soapEnvelope.getHeader().getElementsByTagNameNS(header.getNamespaceURI(), header.getLocalName());
            if (headers.getLength() == 1) {
                ((javax.xml.soap.Node)headers.item(0)).detachNode();
            }
            Node domNode = soapEnvelope.getHeader().getOwnerDocument().importNode((Node)header, true);
            soapEnvelope.getHeader().appendChild(domNode);
        }
        return soapEnvelope;
    }

    /**
     * Set the WS-A MessageID to context.
     *
     * @param context The SOAPMessageContext
     * @throws SOAPException If the envelope could not be read
     */
    public static void setMessageIDtoContext(SOAPMessageContext context) throws SOAPException {
        SOAPEnvelope soapEnvelope = context.getMessage().getSOAPPart().getEnvelope();
        String messageID = getMessageID(soapEnvelope);
        if (messageID != null) {
            context.put(WSA_MESSAGEID_QNAME.getLocalPart(), messageID);
            context.setScope(WSA_MESSAGEID_QNAME.getLocalPart(), Scope.APPLICATION);
        }
    }

    /**
     * Get the WS-A MessageID from the envelope.
     *
     * @param soapEnvelope The SOAPEnvelope
     * @return The message id if found, null otehrwise
     * @throws SOAPException If the envelope could not be read
     */
    public static String getMessageID(SOAPEnvelope soapEnvelope) throws SOAPException {
        NodeList headers = soapEnvelope.getHeader().getElementsByTagNameNS(WSA_ACTION_QNAME.getNamespaceURI(), WSA_ACTION_QNAME.getLocalPart());
        if (headers.getLength() == 1) {
            return ((javax.xml.soap.Node)headers.item(0)).getValue();
        }
        return null;
    }

    /**
     * Get the To header if it is set.
     *
     * @param context The SwitchYard Context
     * @return The To address
     */
    public static String getToAddress(Context context) {
        String address = null;
        Property toProp = context.getProperty(WSA_TO_STR);
        if (toProp == null) {
            toProp = context.getProperty(WSA_TO_STR.toLowerCase());
        }
        if (toProp != null) {
            Element toEl = (Element)toProp.getValue();
            address = toEl.getFirstChild().getNodeValue();
        }
        return address;
    }

    /**
     * Expand xop inlines.
     *
     * @param element the element to expand
     * @param attachmentMap the attachment Map
     * @return the expanded element
     * @throws IOException if the xop content could not be expanded
     */
    public static Element expandXop(Element element, Map<String, DataSource> attachmentMap) throws IOException {
        if (element != null) {
            NodeList children = element.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    QName name = new QName(node.getNamespaceURI(), node.getLocalName());
                    if (name.equals(MTOM_XOP_INCLUDE_QNAME)) {
                        String contentId = XMLHelper.getAttribute((Element)node, "", HREF_STR);
                        if (contentId.startsWith(CID_STR)) {
                            contentId = contentId.substring(4);
                        }
                        contentId = URLDecoder.decode(contentId, "UTF-8");
                        if (attachmentMap.get(contentId) == null) {
                            throw SOAPMessages.MESSAGES.noAttachmentFoundWithName(contentId);
                        }
                        InputStream is = attachmentMap.get(contentId).getInputStream();
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        byte[] buff = new byte[128];
                        int read;
                        try {
                            while ((read = is.read(buff)) != -1) {
                                os.write(buff, 0, read);
                            }
                        } finally {
                            os.flush();
                            os.close();
                            is.close();
                        }
                        // Encode attachment and add it here
                        String imageString = Base64.encode(os.toByteArray());
                        Node content = element.getOwnerDocument().createTextNode(imageString);
                        element.removeChild(node);
                        element.appendChild(content);
                        attachmentMap.remove(contentId);
                    } else {
                        expandXop((Element)node, attachmentMap);
                    }
                }
            }
            return element;
        }
        return null;
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
            return soapMessage.getSOAPBody().addFault(SOAP12_FAULT_MESSAGE_TYPE, 
                    SOAPMessages.MESSAGES.sendFailed());
        } else {
            return soapMessage.getSOAPBody().addFault(SOAP11_FAULT_MESSAGE_TYPE, 
                    SOAPMessages.MESSAGES.sendFailed());
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
     * @throws IOException if the source could not be read 
     * @throws SAXException if any parser error occurs
     */
    public static Document parseAsDom(final String soapRes) throws ParserConfigurationException, IOException, SAXException {
        // Note: Using DOM approach rather than Event based because, comments are not handled properly.
        // An END_DOCUMENT event is sent when a comment is encountered and that leads to:
        // org.w3c.dom.DOMException: HIERARCHY_REQUEST_ERR: An attempt was made to insert a node where it is not permitted.
        return XMLHelper.getDocumentFromString(soapRes);
    }

    /**
     * Generate String representation of SOAP message from javax.xml.soap.SOAPMessage.
     * @param msg SOAPMessage to parse
     * @return String representation of SOAP message
     */
    public static String soapMessageToString(SOAPMessage msg) {
        String str = null;
        if (msg != null) {
            try {
                str = XMLHelper.toPretty(msg.getSOAPPart().getDocumentElement());
            } catch (Exception e) {
                SOAPLogger.ROOT_LOGGER.couldNotParseSOAPMessage(e);
            }
        }
        return str;
    }

    /**
     * Pretty print a Document element.
     * @param element the Document element to print
     * @param out PrintStream to print to.
     */
    public static void prettyPrint(Element element, PrintStream out) {
        try {
            out.println(XMLHelper.toPretty(element));
        } catch (Exception e) {
            SOAPLogger.ROOT_LOGGER.couldNotParseSOAPMessage(e);
        }
    }

    /**
     * Pretty print a SOAP message.
     * @param msg SOAPMessage to print
     * @param out PrintStream to print to.
     */
    public static void prettyPrint(SOAPMessage msg, PrintStream out) {
        prettyPrint(msg.getSOAPPart().getDocumentElement(), out);
    }

    /**
     * Pretty print a SOAP message.
     * @param msg SOAPMessage to print
     * @param out PrintStream to print to.
     */
    public static void prettyPrint(String msg, PrintStream out) {
        try {
            prettyPrint(XMLHelper.getDocumentFromString(msg).getDocumentElement(), out);
        } catch (Exception e) {
            SOAPLogger.ROOT_LOGGER.couldNotParseMessageString(e);
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
            SOAPLogger.ROOT_LOGGER.couldNotInstantiateSOAP11MessageFactory(soape);
        }
        SOAP11_MESSAGE_FACTORY = soapMessageFactory;

        soapMessageFactory = null;
        try {
            soapMessageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        } catch (final SOAPException soape) {
            SOAPLogger.ROOT_LOGGER.couldNotInstantiateSOAP12MessageFactory(soape);
        }
        SOAP12_MESSAGE_FACTORY = soapMessageFactory;
    }
}
