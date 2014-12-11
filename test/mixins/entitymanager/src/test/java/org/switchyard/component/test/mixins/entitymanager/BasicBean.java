/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.switchyard.component.test.mixins.entitymanager;

import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author jingram
 */
@Default
public class BasicBean {
   
   @PersistenceContext
   private EntityManager em;

   public EntityManager getEm() {
      return em;
   }
   

   
}
