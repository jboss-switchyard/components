/*
 * BasicBean.java
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

import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * BasicBean
 *
 * @author rschamm
 */
@Default
public class BasicBean {

	@PersistenceContext
	private EntityManager emOne; // should default to mixin-test-one

	@PersistenceContext(unitName = "mixin-test-two")
	private EntityManager emTwo;

	public static final String ORIGINAL_NAME = "Peter";
	public static final String UPDATED_NAME_1 = "Peter!!!";
	public static final String UPDATED_NAME_2 = "Peter???";

	public void testUpdate() throws Exception {

		try
		{
			//
			// Using emOne
			//
			Query query = emOne.createQuery("update BasicTable set name = :name");
			query.setParameter("name", UPDATED_NAME_1);
			query.executeUpdate();

			//
			// Using emTwo
			//
			query = emTwo.createQuery("update BasicTable set name = :name");
			query.setParameter("name", UPDATED_NAME_2);
			query.executeUpdate();
		}
		catch (Exception ex)
		{
			System.out.println("Failed to update name. Error:" + ex.getMessage());
			ex.printStackTrace();
			throw ex;
		}
	}

}
