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

package org.switchyard.component.soap.util;


import java.io.File;

import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.switchyard.test.mixins.HTTPMixIn;

/**
 * A very simple client to send a SOAP message; used by the quickstarts.
 *
 * @author Nick Cross <ncross@redhat.com> (C) 2012 Red Hat Inc.
 */
public class SOAPClient {
    /**
     * Usage: [-debug] <soap-endpoint> <xml-file>
     *
     * @param args
     * @throws IOException
     * @throws HttpException
     */
    public static void main(String[] args) {
        File sourceFile;
        FileRequestEntity requestEntity;
        int index = 0;

        HTTPMixIn soapMixIn = new HTTPMixIn().setRequestHeader("SOAPAction", "");
        soapMixIn.initialize();

        if (args.length > 0 && args[0].equals("-debug")) {
            index++;
            soapMixIn.setDumpMessages(true);
            if (args.length != 3) {
                usage();
            }
        }
        else if (args.length != 2) {
            usage ();
        }

        sourceFile = new File(args[index+1]);

        if (!sourceFile.canRead()) {
            System.err.println("Unable to read " + args[index+1]);
            usage();
        }

        requestEntity = new FileRequestEntity(sourceFile, "text/xml; charset=utf-8");
        String result = soapMixIn.postFile(args[index], requestEntity);
        System.out.println("SOAP Reply:\n" + result);
    }


    private static void usage() {
        System.out.println("Usage: SOAPClient [-debug] <soap-url> <xml-file>");
        System.exit(1);
    }
}
