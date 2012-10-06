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

package org.switchyard.component.soap;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;

import org.switchyard.HandlerException;
import org.switchyard.component.soap.util.SOAPUtil;
import org.switchyard.config.model.Scannable;

/**
 * {@link HandlerException} to SOAP 1.2 fault transformer.
 *
 * @param <F> From type.
 * @param <T> To type.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
@Scannable(false)
public class HandlerSOAP12ExceptionTransformer<F extends HandlerException, T extends SOAPMessage> extends DefaultSOAP12ExceptionTransformer<F, T> {

    @Override
    public QName getFrom() {
        return toMessageType(HandlerException.class);
    }

    @Override
    public QName getTo() {
        return SOAPUtil.SOAP12_FAULT_MESSAGE_TYPE;
    }
}
