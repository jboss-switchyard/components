/*
 * EntityManagerProducer.java
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
import java.lang.reflect.Proxy;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Johnathan Ingram (jingram@rogueware.org)
 */
public class EntityManagerProducer implements Serializable {

   private static final Logger log = LoggerFactory.getLogger(EntityManagerProducer.class);

   @Produces
   @Named
   public EntityManager createEntityManager(InjectionPoint injectionPoint) {
      // Create the delegate to act on behald of the entity manager
      //  in context of a thread and transaction
      String unitName = null != getUnitNameFromAnnotation(injectionPoint) ? getUnitNameFromAnnotation(injectionPoint) : EntityManagerMixIn.getDefaultPersistenceUnitName();
      EntityManagerDelegate emd = new EntityManagerDelegate(unitName);
      EntityManager em = (EntityManager) Proxy.newProxyInstance(
              EntityManagerDelegate.class
              .getClassLoader(),
              new Class[]{EntityManager.class},
              emd);

      log.trace("Created entity manager delegate for pu '{}' injected into '{}' class member '{}'", unitName, injectionPoint.getMember().getDeclaringClass().getName(), injectionPoint.getMember().getName());
      return em;
   }

   private String getUnitNameFromAnnotation(InjectionPoint injectionPoint) {
      PersistenceContext annotation = injectionPoint.getAnnotated().getAnnotation(PersistenceContext.class);
      if (null != annotation) {
         if (null != annotation.unitName() && 0 != annotation.unitName().length()) {
            return annotation.unitName();
         }
      }
      return null;
   }
}
