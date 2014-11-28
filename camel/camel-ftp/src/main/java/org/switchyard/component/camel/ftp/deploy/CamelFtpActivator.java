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

import org.switchyard.common.camel.SwitchYardCamelContext;
import org.switchyard.component.camel.common.deploy.BaseBindingActivator;
import org.switchyard.component.camel.common.handler.InboundHandler;
import org.switchyard.component.camel.ftp.model.CamelSftpBindingModel;

/**
 * Camel ftp activator.
 */
public class CamelFtpActivator extends BaseBindingActivator {

    /**
     * Creates new activator instance.
     *
     * @param context
     *            Camel context.
     * @param types
     *            Activation types.
     */
    public CamelFtpActivator(SwitchYardCamelContext context, String[] types) {
        super(context, types);
    }

    @SuppressWarnings("unchecked")
    protected <T extends CamelSftpBindingModel> InboundHandler<T> createInboundHandler(QName serviceName, T binding) {
        return (InboundHandler<T>) new CamelSftpInboundHandler(binding, getCamelContext(), serviceName, getServiceDomain());
    }

}
