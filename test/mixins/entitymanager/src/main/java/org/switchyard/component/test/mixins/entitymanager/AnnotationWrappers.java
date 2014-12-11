/*
 * AnnotationWrappers.java
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

import java.lang.annotation.Annotation;

/**
 *
 * @author Johnathan Ingram (jingram@rogueware.org)
 */
public class AnnotationWrappers {

   private AnnotationWrappers() {
   }

   public static class Inject {

      @javax.inject.Inject
      public static Object field;

      public static Annotation getAnnotation() {
         try {
            return Inject.class.getField("field").getAnnotation(javax.inject.Inject.class);
         } catch (Exception ex) {
            return null;
         }
      }
   }

   @javax.inject.Named
   public static class Named {

      public static Annotation getAnnotation() {
         try {
            return Named.class.getAnnotation(javax.inject.Named.class);
         } catch (Exception ex) {
            return null;
         }
      }
   }

   @javax.inject.Singleton
   public static class Singleton {

      public static Annotation getAnnotation() {
         try {
            return Dependent.class.getAnnotation(javax.inject.Singleton.class);
         } catch (Exception ex) {
            return null;
         }
      }
   }

   @javax.enterprise.context.Dependent
   public static class Dependent {

      public static Annotation getAnnotation() {
         try {
            return Dependent.class.getAnnotation(javax.enterprise.context.Dependent.class);
         } catch (Exception ex) {
            return null;
         }
      }
   }

   @javax.enterprise.context.RequestScoped
   public static class RequestScoped {

      public static Annotation getAnnotation() {
         try {
            return RequestScoped.class.getAnnotation(javax.enterprise.context.RequestScoped.class);
         } catch (Exception ex) {
            return null;
         }
      }
   }

   @javax.enterprise.context.ApplicationScoped
   public static class ApplicationScoped {

      public static Annotation getAnnotation() {
         try {
            return ApplicationScoped.class.getAnnotation(javax.enterprise.context.ApplicationScoped.class);
         } catch (Exception ex) {
            return null;
         }
      }
   }

   @javax.enterprise.inject.Default
   public static class Default {

      public static Annotation getAnnotation() {
         try {
            return Default.class.getAnnotation(javax.enterprise.inject.Default.class);
         } catch (Exception ex) {
            return null;
         }
      }
   }
}
