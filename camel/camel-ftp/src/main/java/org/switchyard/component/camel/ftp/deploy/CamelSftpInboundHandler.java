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
package org.switchyard.component.camel.ftp.deploy;

import javax.xml.namespace.QName;

import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang.StringUtils;
import org.switchyard.ServiceDomain;
import org.switchyard.common.camel.SwitchYardCamelContext;
import org.switchyard.component.camel.common.handler.InboundHandler;
import org.switchyard.component.camel.ftp.model.CamelSftpBindingModel;
import org.switchyard.component.camel.ftp.model.FtpCamelComponentMessages;

/**
 * Inbound handler for Sftp binding. Creates additional route elements for
 * service bindings.
 */
public class CamelSftpInboundHandler extends InboundHandler<CamelSftpBindingModel> {

    /**
     * Sole constructor.
     *
     * @param camelBindingModel The CamelBindingModel.
     * @param camelContext The camel context instance.
     * @param serviceName The target service name.
     * @param domain the service domain.
     */
    public CamelSftpInboundHandler(CamelSftpBindingModel camelBindingModel,
        SwitchYardCamelContext camelContext, QName serviceName, ServiceDomain domain) {
        super(camelBindingModel, camelContext, serviceName, domain);
    }

    @Override
    protected RouteDefinition createRouteDefinition() {
        CamelSftpBindingModel bindingModel = getBindingModel();
        if (!StringUtils.isEmpty(bindingModel.getPrivateKeyFilePassphrase()) && StringUtils.isNotEmpty(bindingModel.getPrivateKeyPassphrase())) {
            throw FtpCamelComponentMessages.MESSAGES.multiplePassPhraseKeyDefinition();
        }
        return super.createRouteDefinition();
    }

}
