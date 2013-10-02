package org.switchyard.component.common.knowledge;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
/**
 * <p/>
 * This file is using the subset 34600-34699 for logger messages.
 * <p/>
 *
 */
@MessageLogger(projectCode = "SWITCHYARD")
public interface CommonKnowledgeLogger {
    /**
     * A root logger with the category of the package name.
     */
    CommonKnowledgeLogger ROOT_LOGGER = Logger.getMessageLogger(CommonKnowledgeLogger.class, CommonKnowledgeLogger.class.getPackage().getName());

    /**
     * problemDisposing method definition.
     * @param disposalClassSimpleName disposalClassSimpleName
     * @param tMessage tMessage
     */
    @LogMessage(level = Level.WARN)
    @Message(id = 34600, value = "problem disposing [%s]: %s")
    void problemDisposing(String disposalClassSimpleName, String tMessage);

    /**
     * problemClosingEntityManagerFactory method definition.
     * @param tMessage tMessage
     */
    @LogMessage(level = Level.WARN)
    @Message(id = 34601, value = "problem closing EntityManagerFactory: %s")
    void problemClosingEntityManagerFactory(String tMessage);

    /**
     * problemClosingKieRuntimeLogger method definition.
     * @param tMessage tMessage
     */
    @LogMessage(level = Level.WARN)
    @Message(id = 34602, value = "problem closing KieRuntimeLogger: %s")
    void problemClosingKieRuntimeLogger(String tMessage);

    /**
     * problemStopppingKieScanner method definition.
     * @param tMessage tMessage
     */
    @LogMessage(level = Level.WARN)
    @Message(id = 34603, value = "problem stoppping KieScanner: %s")
    void problemStopppingKieScanner(String tMessage);

    /**
     * problemDisposingKieSession method definition.
     * @param tMessage tMessage
     */
    @LogMessage(level = Level.WARN)
    @Message(id = 34604, value = "problem disposing KieSession: %s")
    void problemDisposingKieSession(String tMessage);

    /**
     * problemDisposingKnowledgeAgent method definition.
     * @param tMessage tMessage
     */
    @LogMessage(level = Level.WARN)
    @Message(id = 34605, value = "problem disposing KnowledgeAgent: %s")
    void problemDisposingKnowledgeAgent(String tMessage);

}

