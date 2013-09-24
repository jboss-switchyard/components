package org.switchyard.component.common.knowledge;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.switchyard.SwitchYardException;

/**
 * <p/>
 * This file is using the subset 35000-35399 for logger messages.
 * <p/>
 *
 */
@MessageBundle(projectCode = "SWITCHYARD")
public interface CommonKnowledgeMessages {
    /**
     * The default messages.
     */
    CommonKnowledgeMessages MESSAGES = Messages.getBundle(CommonKnowledgeMessages.class);

    /**
     * unknownExpressionType method definition.
     * @param expressionType the expressionType
     * @return IllegalArgumentException
     */
    @Message(id = 35000, value = "Unknown expression type: %s")
    IllegalArgumentException unknownExpressionType(String expressionType);

    /**
     * serviceNameNull method definition.
     * @return SwitchYardException
     */
    @Message(id = 35001, value = "ServiceName == null")
    SwitchYardException serviceNameNull();

    /**
     * serviceDomainNull method definition.
     * @return SwitchYardException
     */
    @Message(id = 35002, value = "ServiceDomain == null")
    SwitchYardException serviceDomainNull();

    /**
     * serviceReferenceNull method definition.
     * @param serviceName the serviceName
     * @return SwitchYardException
     */
    @Message(id = 35003, value = "ServiceReference [%s] == null")
    SwitchYardException serviceReferenceNull(String serviceName);

    /**
     * manifestContainerBaseNameRequiredInConfigurationForPersistentSessions method definition.
     * @return SwitchYardException
     */
    @Message(id = 35006, value = "manifest container baseName required in configuration for persistent sessions")
    SwitchYardException manifestContainerBaseNameRequiredInConfigurationForPersistentSessions();

    /**
     * containerScanIntervalMustBePositive method definition.
     * @return IllegalArgumentException
     */
    @Message(id = 35007, value = "container scanInterval must be positive")
    IllegalArgumentException containerScanIntervalMustBePositive();

    /**
     * couldNotUseNullNameToRegisterChannel method definition.
     * @param channelClassName channelClassName
     * @return SwitchYardException
     */
    @Message(id = 35008, value = "Could not use null name to register channel: %s")
    SwitchYardException couldNotUseNullNameToRegisterChannel(String channelClassName);

    /**
     * couldNotLoadListenerClass method definition.
     * @param listenerModelClass listenerModelClass
     * @return SwitchYardException
     */
    @Message(id = 35009, value = "Could not load listener class: %s")
    SwitchYardException couldNotLoadListenerClass(String listenerModelClass);

    /**
     * couldNotInstantiateListenerClass method definition.
     * @param listenerClassName listenerClassName
     * @return SwitchYardException
     */
    @Message(id = 35010, value = "Could not instantiate listener class: %s")
    SwitchYardException couldNotInstantiateListenerClass(String listenerClassName);

    /**
     * cannotRegisterOperation method definition.
     * @param type type
     * @param name name
     * @return SwitchYardException
     */
    @Message(id = 35011, value = "cannot register %s operation due to duplicate name: %s")
    SwitchYardException cannotRegisterOperation(String type, String name);

}

