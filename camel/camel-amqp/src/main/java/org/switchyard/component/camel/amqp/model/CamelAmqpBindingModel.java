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
package org.switchyard.component.camel.amqp.model;

import org.switchyard.component.camel.jms.model.CamelJmsBindingModel;

/**
 * Represents the configuration settings for an Amqp endpoint in Camel.
 *
 * According to the Camel Documentation, Camel JMS and Camel AMQP configurations share
 * all options, thus the reason to extend {@link GenericMqBindingModel}.
 *
 * @author: <a href="mailto:eduardo.devera@gmail.com">Eduardo de Vera</a>
 */
public interface CamelAmqpBindingModel extends CamelJmsBindingModel {
}
