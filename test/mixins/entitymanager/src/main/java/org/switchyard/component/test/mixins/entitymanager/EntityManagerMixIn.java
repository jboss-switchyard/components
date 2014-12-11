/*
 * EntityManagerMixIn.java
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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import org.enhydra.jdbc.pool.StandardXAPoolDataSource;
import org.enhydra.jdbc.standard.StandardXADataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.switchyard.component.test.mixins.cdi.CDIMixIn;
import org.switchyard.component.test.mixins.naming.NamingMixIn;
import org.switchyard.component.test.mixins.transaction.TransactionMixIn;
import org.switchyard.test.MixInDependencies;
import org.switchyard.test.mixins.AbstractTestMixIn;

/**
 *
 * @author Johnathan Ingram (jingram@rogueware.org)
 */
@MixInDependencies(required = {CDIMixIn.class, NamingMixIn.class, TransactionMixIn.class})
public class EntityManagerMixIn extends AbstractTestMixIn {

   protected static final Logger log = LoggerFactory.getLogger(EntityManagerMixIn.class);

   private static final Map<String, EntityManagerFactory> emfs = Collections.synchronizedMap(new LinkedHashMap<String, EntityManagerFactory>());
   private static String primaryPU;
   private static TransactionMixIn transactionMixIn;
   private static NamingMixIn namingMixIn;

   @Override
   public void initialize() {
      super.initialize();

      // Get a reference to the TransactionMixIn
      try {
         transactionMixIn = getTestKit().getMixIn(TransactionMixIn.class);
      } catch (Exception ex) {
         log.error("Unable to obtain a reference to the transaction manager mix-in");
      }

      try {
         namingMixIn = getTestKit().getMixIn(NamingMixIn.class);
      } catch (Exception ex) {
         log.error("Unable to obtain a reference to the naming mix-in");
      }
   }

   public void createEntityManagerFactory(String persistenceUnitName, String jtaDataSource, String datasourceDriverName, String datasourceUrlr, String datasourceUser, String dataSourcePassword) throws Exception {
      // Create a transaction aware XA datasource and bind it to JNDI      
      try {
         StandardXADataSource sXaDs = new StandardXADataSource();
         sXaDs.setUrl(datasourceUrlr);
         sXaDs.setDriverName(datasourceDriverName);
         sXaDs.setUser(datasourceUser);
         sXaDs.setPassword(dataSourcePassword);
         sXaDs.setTransactionManager(transactionMixIn.getTransactionManager());

         StandardXAPoolDataSource xaPoolDS = new StandardXAPoolDataSource();
         xaPoolDS.setTransactionManager(transactionMixIn.getTransactionManager());         // Always set first
         xaPoolDS.setDataSource((XADataSource) sXaDs);
         xaPoolDS.setUser(datasourceUser);
         xaPoolDS.setPassword(dataSourcePassword);
         xaPoolDS.setMaxSize(10);
         xaPoolDS.setMinSize(5);
         xaPoolDS.setDeadLockMaxWait(30 * 1000);

         // Need to bind delegate DataSource 
         InitialContext initialContext = namingMixIn.getInitialContext();
         initialContext.bind(jtaDataSource, new DataSourceDelegate(xaPoolDS));
      } catch (Exception ex) {
         log.error("Unable to create the datasource for JNDI '{}' with driver='{}'", jtaDataSource, datasourceDriverName, ex);
         throw ex;
      }

      // Create the entity manager factory (EMF)
      try {

         EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnitName);

         emfs.put(persistenceUnitName, emf);
         if (null == primaryPU) {
            primaryPU = persistenceUnitName;
         }
      } catch (Exception ex) {
         log.error("Unable to create the entity manager factory for persistence unit name '{}'", persistenceUnitName, ex);
         throw ex;
      }
   }

   protected static String getDefaultPersistenceUnitName() {
      return primaryPU;
   }

   protected static EntityManagerFactory getEntityManagerFactory() {
      if (null != primaryPU) {
         return emfs.get(primaryPU);
      } else {
         log.error("Unable to obtain the default entity manager as no entity manager factories have been set with 'createEntityManagerFactory'");
         return null;
      }
   }

   protected static EntityManagerFactory getEntityManagerFactory(String pu) {
      if (emfs.containsKey(pu)) {
         return emfs.get(pu);
      } else {
         log.error("Unable to obtain entity manager for persistence unit '{}' as no entity manager factory has been set", pu);
         return null;
      }
   }

   protected static EntityManager createEntityManager(String persistenceUnitName) {
      EntityManagerFactory emf = null;
      if (null == (emf = emfs.get(persistenceUnitName))) {
         return null;
      }
      EntityManager em = emf.createEntityManager();
      return em;
   }

   protected static TransactionManager getTransactionManager() {
      if (null != transactionMixIn || null == transactionMixIn.getTransactionManager()) {
         return transactionMixIn.getTransactionManager();
      } else {
         log.error("Unable to obtain transaction manager as either transaction mix-in or transaction manager is null");
         return null;
      }
   }
}
