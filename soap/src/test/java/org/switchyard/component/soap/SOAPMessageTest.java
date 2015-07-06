/*
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors.
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
package org.switchyard.component.soap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.component.test.mixins.http.HTTPMixIn;
import org.switchyard.test.MockHandler;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;
import org.switchyard.test.SwitchYardTestKit;

/**
 * Tests SOAP envelopes.
 */
@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(config = "soap-switchyard.xml", mixins = { HTTPMixIn.class })
public class SOAPMessageTest {

    private static final String SOAP11_ENDPOINT = "http://localhost:18001/soap11/HelloWebService";
    private static final String SOAP12_ENDPOINT = "http://localhost:18001/soap12/HelloSOAP12Service";

    //@formatter:off
    private static final String RESPONSE =
              "<test:sayHelloResponse xmlns:test=\"urn:switchyard-component-soap:test-ws:1.0\">"
            + "   <return>Hello, SwitchYard!</return>"
            + "</test:sayHelloResponse>";
    private static final String FAULT = "<message>ERROR!</message>";
    //@formatter:on

    private SwitchYardTestKit _testKit;
    private HTTPMixIn _httpMixIn;

    private MockHandler _mock;

    @Before
    public void setUp() {
        _mock = _testKit.registerInOutService("HelloSOAPService");
    }

    @Test
    public void toSOAP11Endpoint_soap11() {
        _mock.replyWithOut(RESPONSE);
        _httpMixIn.postResourceAndTestXML(SOAP11_ENDPOINT, "soap11-request.xml", "soap11-response.xml");
    }

    @Test
    public void toSOAP11Endpoint_soap11_fault() {
        _mock.replyWithFault(FAULT);
        _httpMixIn.postResourceAndTestXML(SOAP11_ENDPOINT, "soap11-request.xml", "soap11-fault.xml");
    }

    @Ignore("Does not pass due to CXF-4794 in CXF 2.6.6")
    @Test
    public void toSOAP11Endpoint_soap12() {
        _mock.replyWithOut(RESPONSE);
        _httpMixIn.postResourceAndTestXML(SOAP11_ENDPOINT, "soap12-request.xml", "soap11-fault-mismatch.xml");
    }

    @Test
    public void toSOAP12Endpoint_soap11() {
        _mock.replyWithOut(RESPONSE);
        _httpMixIn.postResourceAndTestXML(SOAP12_ENDPOINT, "soap11-request.xml", "soap11-response.xml");
    }

    @Test
    public void toSOAP12Endpoint_soap11_fault() {
        _mock.replyWithFault(FAULT);
        _httpMixIn.postResourceAndTestXML(SOAP12_ENDPOINT, "soap11-request.xml", "soap11-fault.xml");
    }

    @Test
    public void toSOAP12Endpoint_soap12() {
        _mock.replyWithOut(RESPONSE);
        _httpMixIn.postResourceAndTestXML(SOAP12_ENDPOINT, "soap12-request.xml", "soap12-response.xml");
    }

    @Test
    public void toSOAP12Endpoint_soap12_fault() {
        _mock.replyWithFault(FAULT);
        _httpMixIn.postResourceAndTestXML(SOAP12_ENDPOINT, "soap12-request.xml", "soap12-fault.xml");
    }

}
