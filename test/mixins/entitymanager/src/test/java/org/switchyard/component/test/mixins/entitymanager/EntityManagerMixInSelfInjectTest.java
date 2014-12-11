/*
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

import javax.inject.Inject;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.component.test.mixins.cdi.CDIMixIn;
import org.switchyard.component.test.mixins.naming.NamingMixIn;
import org.switchyard.component.test.mixins.transaction.TransactionMixIn;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;

/**
 * Unit test for {@link EntityManagerQMixIn}.
 *
 * @author Johnathan Ingram (jingram@rogueware.org)
 *
 */
@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(mixins = {CDIMixIn.class, NamingMixIn.class, TransactionMixIn.class, EntityManagerMixIn.class})
public class EntityManagerMixInSelfInjectTest {

   //private NamingMixIn namingMixIn;
   //private TransactionMixIn transactionMixIn;
   private EntityManagerMixIn entityManagerMixIn;

   @Inject
   BasicBean tb;

   @Before
   public void setup() throws Exception {
      entityManagerMixIn.createEntityManagerFactory("mixin-test-one", "java:jboss/datasources/MixInTestOne", "org.h2.Driver", "jdbc:h2:mem:testDB-one", "sa", "sa");
   }

   @Test
   public void getClientSession() {
      assertNotNull("Entity Manager not injected", tb.getEm());
   }

}
