package org.switchyard.component.camel.config.model.amqp;

import org.switchyard.component.camel.config.model.generic.GenericMqBindingModel;
import org.switchyard.component.camel.config.model.jms.CamelJmsBindingModel;

/**
 *
 * Represents the configuration settings for an Amqp endpoint in Camel.
 * According to the Camel Documentation, Camel JMS and Camel AMQP configurations share
 * all options, thus the reason to extend {@link CamelJmsBindingModel}.
 *
 * @author: <a href="mailto:eduardo.devera@gmail.com">Eduardo de Vera</a>
 */
public interface CamelAmqpBindingModel extends GenericMqBindingModel {
}
