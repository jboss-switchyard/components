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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for {@link EntityManagerQMixIn}.
 * 
 * @author Johnathan Ingram (jingram@rogueware.org)
 *
 */
public class EntityManagerMixInSelfInjectTest {
    
    private EntityManagerMixIn EntityManagerMixIn;
    
    @BeforeClass
    public static void setup() {
    }
    
    @AfterClass
    public static void tearDown() {
    }

    @Test
    public void getClientSession() {
       int i = 1;
    }    
}
