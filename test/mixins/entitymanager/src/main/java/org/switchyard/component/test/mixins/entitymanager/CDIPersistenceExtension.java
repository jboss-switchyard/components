/*
 * CDIPersistenceExtension.java
 * 
 * Copyright 2014 Johnathan Ingram (jingram@rogueware.org)
 * All rights reserved.
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
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Johnathan Ingram (jingram@rogueware.org)
 */
public class CDIPersistenceExtension implements Extension {

   private transient static final Logger log = LoggerFactory.getLogger(CDIPersistenceExtension.class);


   // Ensure all javax.persistence.EntityManager fields with @PersistenceContext are annotated @Inject
   public <T> void processJPAAnnotations(@Observes ProcessAnnotatedType<T> pat) {
      final AnnotatedType<T> at = pat.getAnnotatedType();
      
      // Override any javax.persistence.EntityManager fields @PersistenceContext annotations with @Inject
      AnnotatedType<T> wrapped = new AnnotatedType<T>() {

         @Override
         public Class<T> getJavaClass() {
            return at.getJavaClass();
         }

         @Override
         public Set<AnnotatedConstructor<T>> getConstructors() {
            return at.getConstructors();
         }

         @Override
         public Set<AnnotatedMethod<? super T>> getMethods() {
            return at.getMethods();
         }

         @Override
         public Set<AnnotatedField<? super T>> getFields() {
            Set<AnnotatedField<? super T>> result = new HashSet<AnnotatedField<? super T>>();
            for (final AnnotatedField af : at.getFields()) {
               // If there is a field with the @PersistenceContext of type javax.persistence.EntityManager,
               //   make sure the field has the @Inject 
               if (javax.persistence.EntityManager.class.equals(af.getJavaMember().getType())
                     && af.isAnnotationPresent(PersistenceContext.class)) {
                  result.add(addAnnotation(af, AnnotationWrappers.Inject.getAnnotation()));
               } else {
                  result.add(af);
               }
            }

            return result;
         }

         @Override
         public Type getBaseType() {
            return at.getBaseType();
         }

         @Override
         public Set<Type> getTypeClosure() {
            return at.getTypeClosure();
         }

         @Override
         public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            return at.getAnnotation(annotationType);
         }

         @Override
         public Set<Annotation> getAnnotations() {
            return at.getAnnotations();
         }

         @Override
         public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return at.isAnnotationPresent(annotationType);
         }
      };

      pat.setAnnotatedType(wrapped);
   }
	 
	 
   public static AnnotatedField addAnnotation(final AnnotatedField af, final Annotation... annotations) {

      return new AnnotatedField() {
         @Override
         public Field getJavaMember() {
            return af.getJavaMember();
         }

         @Override
         public boolean isStatic() {
            return af.isStatic();
         }

         @Override
         public AnnotatedType getDeclaringType() {
            return af.getDeclaringType();
         }

         @Override
         public Type getBaseType() {
            return af.getBaseType();
         }

         @Override
         public Set<Type> getTypeClosure() {
            return af.getTypeClosure();
         }

         @Override
         public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            for (Annotation a : annotations) {
               if (a.annotationType().equals(annotationType)) {
                  return (T) a;
               }
            }
            return af.getAnnotation(annotationType);
         }

         @Override
         public Set<Annotation> getAnnotations() {
            Set<Annotation> result = new HashSet<Annotation>(af.getAnnotations());
            result.addAll(Arrays.asList(annotations));
            return result;
         }

         @Override
         public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            for (Annotation a : annotations) {

               if (a.annotationType().equals(annotationType)) {
                  return true;
               }
            }

            return af.isAnnotationPresent(annotationType);
         }
      };
   }	 
}
