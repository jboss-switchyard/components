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
package org.switchyard.component.camel.common.transaction;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.switchyard.component.camel.CommonCamelMessages;

/**
 * Simple factory which creates TransactedManager instances.
 * 
 * @author Daniel Bevenius
 */
public final class TransactionManagerFactory {

    /**
     * JBoss AS specific UserTransaction JNDI name.
     */
    public static final String JBOSS_USER_TRANSACTION = "java:jboss/UserTransaction";

    /**
     * JBoss AS specific TransactionManager JNDI name.
     */
    public static final String JBOSS_TRANSACTION_MANANGER = "java:jboss/TransactionManager";

    /**
     * JBoss AS specific TransactionSynchronizationRegistry JNDI name.
     */
    public static final String JBOSS_TRANSACTION_SYNC_REG = "java:jboss/TransactionSynchronizationRegistry";

    /**
     * Configuration name for the JtaTransactionManager.
     */
    public static final String TM = "jtaTransactionManager";

    private static final TransactionManagerFactory INSTANCE = new TransactionManagerFactory();

    private TransactionManagerFactory() {
    }

    /**
     * Gets the singleton instance.
     * 
     * @return TransactionManagerFactory the singleton instance.
     */
    public static TransactionManagerFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Factory method that creates a {@link PlatformTransactionManager}.
     * 
     * @return {@link PlatformTransactionManager} the created PlatformTransactionManager.
     */
    public PlatformTransactionManager create() {
        final JtaTransactionManager transactionManager = new JtaTransactionManager();

        if (isBound(JBOSS_USER_TRANSACTION)) {
            transactionManager.setUserTransactionName(JBOSS_USER_TRANSACTION);
            transactionManager.setTransactionManagerName(JBOSS_TRANSACTION_MANANGER);
            transactionManager.setTransactionSynchronizationRegistryName(JBOSS_TRANSACTION_SYNC_REG);
        } else if (isBound(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME)) {
            transactionManager.setUserTransactionName(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME);
        } else {
            throw CommonCamelMessages.MESSAGES.couldNotCreateAJtaTransactionManagerAsNoTransactionManagerWasFoundJBOSSUSERTRANSACTION(JBOSS_USER_TRANSACTION, JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME);
        }

        // Initialize the transaction manager.
        transactionManager.afterPropertiesSet();
        return transactionManager;
    }

    private boolean isBound(final String jndiName) {
       return lookupInJndi(jndiName) != null; 
    }

    private Object lookupInJndi(final String name) {
        InitialContext context = null;
        try {
            context = new InitialContext();
            return context.lookup(name);
        } catch (final NamingException e) {
            return null;
        } catch (final Exception e) {
            throw CommonCamelMessages.MESSAGES.unexpectedExceptionRetrieving(name, e);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (final Exception e) {
                    throw CommonCamelMessages.MESSAGES.unexpectedErrorClosingInitialContext(e);
                }
            }
        }
    }

}
