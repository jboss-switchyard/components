/*
 * EntityManagerDelegate.java
 * 
 * Copyright 2014 Johnathan Ingram (jingram@rogueware.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. *
 * 
 */
package org.switchyard.component.test.mixins.entitymanager;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRequiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Johnathan Ingram (jingram@rogueware.org)
 */
public class EntityManagerDelegate implements InvocationHandler, Serializable {

   private transient static final Logger log = LoggerFactory.getLogger(EntityManagerDelegate.class);
   private transient static final ThreadLocal<Map<String, Map<Integer, EntityManager>>> threadEntityManagerByTransaction = new ThreadLocal<Map<String, Map<Integer, EntityManager>>>();

   private String unitName;

   public EntityManagerDelegate(String unitName) {
      this.unitName = unitName;
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      EntityManager em = getEntityManagerForCurrentTransaction();
      try {
         return method.invoke(em, args);
      } catch (InvocationTargetException ex) {
         throw ex.getCause();
      }
   }

   private EntityManager getEntityManagerForCurrentTransaction() throws SystemException, TransactionRequiredException {
		if (null == unitName) {
         unitName = EntityManagerMixIn.getDefaultPersistenceUnitName();
		}
      long threadId = Thread.currentThread().getId();
      EntityManager em = null;

      // Make sure we have the thread var for this thread managing entity managers
      Map<String, Map<Integer, EntityManager>> emsPUs = threadEntityManagerByTransaction.get();
      if (null == emsPUs) {
         emsPUs = new HashMap<String, Map<Integer, EntityManager>>();
         threadEntityManagerByTransaction.set(emsPUs);  // Map scoped per thread for each PU, its thread id and em
      }

      // Make sure we have a list of thread and em for this pu
      Map<Integer, EntityManager> ems;
      if (!emsPUs.containsKey(unitName)) {
         ems = new HashMap<Integer, EntityManager>();
         emsPUs.put(unitName, ems);
      } else {
         ems = emsPUs.get(unitName);
      }

      // If there is a transaction, create new entity manager for transaction or return existing one
      TransactionManager tm = EntityManagerMixIn.getTransactionManager();
      if (null != tm && null != tm.getTransaction() && tm.getStatus() != Status.STATUS_NO_TRANSACTION) {
         int transactionId = tm.getTransaction().hashCode();
         if (ems.containsKey(transactionId)) {
            // return the existing em for the transaction
            em = ems.get(transactionId);
            log.trace("Returning entity manager for thread id '{}', pu '{}' and transaction id '{}'", threadId, unitName, transactionId);
         } else {
				// Create an em for the transaction
            // Register synchronization to cleanup entity manager when transaction completes
            try {
               tm.getTransaction().registerSynchronization(new EntityManagerTransactionSynchronization(transactionId));
            } catch (Exception ex) {
               log.error("Unable to register transaction synchronization for entity manager cleanup for thread id '{}', pu '{}' and transaction id '{}'", threadId, unitName, transactionId);
               throw new IllegalStateException(String.format("Unable to register transaction synchronization for entity manager cleanup for thread id %d, pu %s and transaction id %d", threadId, unitName, transactionId));
            }
            em = EntityManagerMixIn.createEntityManager(unitName);

            if (null != em) {
               ems.put(transactionId, em);
               em.joinTransaction();

               log.trace("Created entity manager for thread id '{}', pu '{}' and transaction id '{}'", threadId, unitName, transactionId);
            } else {
               log.warn("Unable to create entity manager for thread id '{}', pu '{}' and transaction id '{}'", threadId, unitName, transactionId);
            }
         }
      } else {
         // No transaction
         throw new IllegalStateException(String.format("No transaction available to scope enity manager for thread id %d", threadId));
      }
      return em;
   }

   private class EntityManagerTransactionSynchronization implements Synchronization {

      private final int transactionId;

      protected EntityManagerTransactionSynchronization(int transactionId) {
         this.transactionId = transactionId;
      }

      @Override
      public void beforeCompletion() {
         long threadId = Thread.currentThread().getId();
         try {
            Map<String, Map<Integer, EntityManager>> emsPUs = threadEntityManagerByTransaction.get();
            if (null == emsPUs) {
               return;
            }
            Map<Integer, EntityManager> ems = emsPUs.get(unitName);
            if (null == ems) {
               return;
            }

            if (ems.containsKey(transactionId)) {
               EntityManager em = ems.get(transactionId);
               // Make sure nothing further can happen on the entity manager
               if (null != em) {
                  em.close();
                  log.trace("Closed entity manager for thread id '{}', pu '{}' and transaction id '{}' on before transaction completion", threadId, unitName, transactionId);
               }
            }
         } catch (Throwable ex) {
            log.error("Unknown exception before transaction completion for thread id '{}', pu '{}' and transaction id '{}'", threadId, unitName, transactionId, ex);
         }
      }

      @Override
      public void afterCompletion(int status) {
         // Don't care about status, just release the entity manager associated with the transaction
         long threadId = Thread.currentThread().getId();

         try {
            Map<String, Map<Integer, EntityManager>> emsPUs = threadEntityManagerByTransaction.get();
            if (null == emsPUs) {
               return;
            }
            Map<Integer, EntityManager> ems = emsPUs.get(unitName);
            if (null == ems) {
               return;
            }

            if (ems.containsKey(transactionId)) {
               EntityManager em = ems.remove(transactionId);
               em = null;
               log.trace("Deleted entity manager for thread id '{}', pu '{}' and transaction id '{}' on after transaction completion", threadId, unitName, transactionId);
            }
         } catch (Throwable ex) {
            log.error("Unknown exception after transaction completion for thread id '{}', pu '{}' and transaction id '{}'", threadId, unitName, transactionId, ex);
         }
      }
   }
}
