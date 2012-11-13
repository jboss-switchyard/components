package org.switchyard.component.camel.config.model.amqp.v1;

import java.net.URI;

import org.switchyard.component.camel.config.model.amqp.CamelAmqpBindingModel;
import org.switchyard.component.camel.config.model.generic.v1.V1GenericMqBindingModel;
import org.switchyard.config.Configuration;
import org.switchyard.config.model.Descriptor;

/**
 * @author: <a href="mailto:eduardo.devera@gmail.com">Eduardo de Vera</a>
 * Date: 11/11/12
 * Time: 3:30 PM
 */
public class V1CamelAmqpBindingModel extends V1GenericMqBindingModel implements CamelAmqpBindingModel{

    public static final String AMQP = "amqp";

    public V1CamelAmqpBindingModel() {
        super(AMQP);
    }

    public V1CamelAmqpBindingModel(Configuration config, Descriptor descriptor) {
        super(config, descriptor);
    }

    @Override
    public URI getComponentURI() {
        return super.getComponentURI(AMQP);
    }
}
