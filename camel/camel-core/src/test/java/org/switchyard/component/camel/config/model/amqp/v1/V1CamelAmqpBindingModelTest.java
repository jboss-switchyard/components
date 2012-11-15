package org.switchyard.component.camel.config.model.amqp.v1;

import junit.framework.Assert;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.switchyard.component.camel.config.model.amqp.CamelAmqpBindingModel;
import org.switchyard.component.camel.config.model.v1.V1BaseCamelModelTest;
import org.switchyard.config.model.Validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author: <a href="mailto:eduardo.devera@gmail.com">Eduardo de Vera</a>
 * Date: 11/11/12
 * Time: 3:59 PM
 */
public class V1CamelAmqpBindingModelTest extends V1BaseCamelModelTest<CamelAmqpBindingModel> {

    private static final String CAMEL_XML = "switchyard-amqp-binding-beans.xml";
    private static final String CAMEL_INVALID_XML = "switchyard-invalid-amqp-binding-beans.xml";

    private static final String QUEUE = "test_queue";
    private static String TOPIC = "esb_in_topic";
    private static String CONNECTION_FACTORY = "connFactory";
    private static String USERNAME = "camel";
    private static String PASSWORD = "isMyFriend";
    private static String CLIENT_ID = "esb_in";
    private static String DURABLE_SUBSCRIPTION_NAME = "esb_in_sub";
    private static Integer CONCURRENT_CONSUMERS = 5;
    private static Integer MAX_CONCURRENT_CONSUMERS = 15;
    private static Boolean DISABLE_REPLY_TO = true;
    private static Boolean PRESERVE_MESSAGE_QOS = true;
    private static Boolean DELIVERY_PERSISTENT = false;
    private static Integer PRIORITY = 9;
    private static Boolean EXPLICIT_QOS_ENABLED = true;
    private static String REPLY_TO = "esb_out";
    private static String REPLY_TO_TYPE= "Shared";
    private static Integer REQUEST_TIMEOUT = 300;
    private static String SELECTOR = "DEST='ESB'";
    private static Integer TIME_TO_LIVE = 3600;
    private static Boolean TRANSACTED = true;

    private static final String COMPONENT_URI = "amqp:topic:esb_in_topic?connectionFactory=connFactory&" +
        "username=camel&password=isMyFriend&clientId=esb_in&durableSubscriptionName=esb_in_sub&" +
        "concurrentConsumers=5&maxConcurrentConsumers=15&disableReplyTo=true&preserveMessageQos=true&" +
        "deliveryPersistent=false&priority=9&explicitQosEnabled=true&replyTo=esb_out&replyToType=Shared&" +
        "requestTimeout=300&selector=DEST='ESB'&timeToLive=3600&transacted=true";

    @Test
    public void validConfigurationFromFile() throws Exception {
        final CamelAmqpBindingModel bindingModel = getFirstCamelBinding(CAMEL_XML);
        final Validation validateModel = bindingModel.validateModel();

        assertTrue(validateModel.isValid());
        assertModel(bindingModel);
        assertEquals(COMPONENT_URI, bindingModel.getComponentURI().toString());
    }

    @Test
    public void invalidConfigurationFromFile() throws Exception {
        final CamelAmqpBindingModel bindingModel = getFirstCamelBinding(CAMEL_INVALID_XML);
        final Validation validateModel = bindingModel.validateModel();

        assertFalse(validateModel.isValid());
    }

    @Test
    public void invalidConfigurationQueueAndTopic() {
        final CamelAmqpBindingModel bindingModel = new V1CamelAmqpBindingModel();
        bindingModel.setConnectionFactory(CONNECTION_FACTORY);
        bindingModel.setQueue(QUEUE);
        bindingModel.setTopic(TOPIC);

        String uri = bindingModel.getComponentURI().toString();
        assertTrue(uri.startsWith("amqp:queue"));
        assertFalse(bindingModel.validateModel().isValid());
    }

    @Test
    public void invalidConfigurationNoDestination() {
        final CamelAmqpBindingModel model = new V1CamelAmqpBindingModel();
        model.setConnectionFactory(CONNECTION_FACTORY);
        assertFalse(model.validateModel().isValid());
    }

    @Test
    public void compareCreatedWithWritenConfigurations() throws Exception {
        String refXml = getFirstCamelBinding(CAMEL_XML).toString();
        String newXml = createModel().toString();
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = XMLUnit.compareXML(refXml, newXml);
        assertTrue(diff.toString(), diff.similar());
    }

    private CamelAmqpBindingModel createModel() {
        return (CamelAmqpBindingModel)
            new V1CamelAmqpBindingModel()
                .setTopic(TOPIC)
                .setConnectionFactory(CONNECTION_FACTORY)
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setClientId(CLIENT_ID)
                .setDurableSubscriptionName(DURABLE_SUBSCRIPTION_NAME)
                .setConcurrentConsumers(CONCURRENT_CONSUMERS)
                .setMaxConcurrentConsumers(MAX_CONCURRENT_CONSUMERS)
                .setDisableReplyTo(DISABLE_REPLY_TO)
                .setPreserveMessageQos(PRESERVE_MESSAGE_QOS)
                .setDeliveryPersistent(DELIVERY_PERSISTENT)
                .setPriority(PRIORITY)
                .setExplicitQosEnabled(EXPLICIT_QOS_ENABLED)
                .setReplyTo(REPLY_TO)
                .setReplyToType(REPLY_TO_TYPE)
                .setRequestTimeout(REQUEST_TIMEOUT)
                .setSelector(SELECTOR)
                .setTimeToLive(TIME_TO_LIVE)
                .setTransacted(TRANSACTED);

    }

    private void assertModel(CamelAmqpBindingModel model) {
        Assert.assertEquals(TOPIC, model.getTopic());
        Assert.assertEquals(CONNECTION_FACTORY, model.getConnectionFactory());
        Assert.assertEquals(USERNAME, model.getUsername());
        Assert.assertEquals(PASSWORD, model.getPassword());
        Assert.assertEquals(CLIENT_ID, model.getClientId());
        Assert.assertEquals(DURABLE_SUBSCRIPTION_NAME, model.getDurableSubscriptionName());
        Assert.assertEquals(CONCURRENT_CONSUMERS, model.getConcurrentConsumers());
        Assert.assertEquals(MAX_CONCURRENT_CONSUMERS, model.getMaxConcurrentConsumers());
        Assert.assertEquals(DISABLE_REPLY_TO, model.isDisableReplyTo());
        Assert.assertEquals(PRESERVE_MESSAGE_QOS, model.isPreserveMessageQos());
        Assert.assertEquals(DELIVERY_PERSISTENT, model.isDeliveryPersistent());
        Assert.assertEquals(PRIORITY, model.getPriority());
        Assert.assertEquals(EXPLICIT_QOS_ENABLED, model.isExplicitQosEnabled());
        Assert.assertEquals(REPLY_TO, model.getReplyTo());
        Assert.assertEquals(REPLY_TO_TYPE, model.getReplyToType());
        Assert.assertEquals(REQUEST_TIMEOUT, model.getRequestTimeout());
        Assert.assertEquals(SELECTOR, model.getSelector());
        Assert.assertEquals(TIME_TO_LIVE, model.getTimeToLive());
        Assert.assertEquals(TRANSACTED, model.isTransacted());
    }
}
