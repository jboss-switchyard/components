/*
 * EntityManagerMixInTest.java
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.inject.Inject;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.switchyard.component.test.mixins.cdi.CDIMixIn;
import org.switchyard.component.test.mixins.naming.NamingMixIn;
import org.switchyard.component.test.mixins.transaction.TransactionMixIn;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;

/**
 * EntityManagerMixInTest
 *
 * @author rschamm
 */
@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(mixins =
{
	CDIMixIn.class, NamingMixIn.class, TransactionMixIn.class, EntityManagerMixIn.class
})
public class EntityManagerMixInTest {

	private TransactionMixIn transactionMixIn;
	private EntityManagerMixIn entityManagerMixIn;

	private static final String persistenceUnitNameOne = "mixin-test-one";
	private static final String jtaDataSourceOne = "java:jboss/datasources/MixInTestOne";
	private static final String datasourceUrlOne = "jdbc:h2:mem:testDB-one";

	private static final String persistenceUnitNameTwo = "mixin-test-two";
	private static final String jtaDataSourceTwo = "java:jboss/datasources/MixInTestTwo";
	private static final String datasourceUrlTwo = "jdbc:h2:mem:testDB-two";

	private static final String datasourceDriverName = "org.h2.Driver";
	private static final String datasourceUser = "sa";
	private static final String dataSourcePassword = "sa";

	private static Connection connectionOne;
	private static Connection connectionTwo;

	@Inject
	BasicBean basicBean;

	@BeforeClass
	public static void setUp() throws Exception {
		connectionOne = DriverManager.getConnection(datasourceUrlOne, datasourceUser, dataSourcePassword);
		connectionTwo = DriverManager.getConnection(datasourceUrlTwo, datasourceUser, dataSourcePassword);
	}

	@Before
	public void setup() throws Exception {
		// PU one
		entityManagerMixIn.createEntityManagerFactory(
				persistenceUnitNameOne,
				jtaDataSourceOne,
				datasourceDriverName,
				datasourceUrlOne,
				datasourceUser,
				dataSourcePassword);

		// PU two
		entityManagerMixIn.createEntityManagerFactory(
				persistenceUnitNameTwo,
				jtaDataSourceTwo,
				datasourceDriverName,
				datasourceUrlTwo,
				datasourceUser,
				dataSourcePassword);

		initSQL(connectionOne);
		initSQL(connectionTwo);
	}

	@AfterClass
	public static void shutDown() throws SQLException {
		cleanSQL(connectionOne);
		cleanSQL(connectionTwo);
	}

	@Test
	public void testUpdateCommit() throws Exception {
		assertEquals(BasicBean.ORIGINAL_NAME, getName(connectionOne));
		assertEquals(BasicBean.ORIGINAL_NAME, getName(connectionTwo));

		transactionMixIn.getUserTransaction().begin();
		basicBean.testUpdate();
		transactionMixIn.getUserTransaction().commit();

		assertEquals(BasicBean.UPDATED_NAME_1, getName(connectionOne));
		assertEquals(BasicBean.UPDATED_NAME_2, getName(connectionTwo));
	}

	@Test
	public void testUpdateRollback() throws Exception {
		assertEquals(BasicBean.ORIGINAL_NAME, getName(connectionOne));
		assertEquals(BasicBean.ORIGINAL_NAME, getName(connectionTwo));

		transactionMixIn.getUserTransaction().begin();
		basicBean.testUpdate();
		transactionMixIn.getUserTransaction().rollback();

		assertEquals(BasicBean.ORIGINAL_NAME, getName(connectionOne));
		assertEquals(BasicBean.ORIGINAL_NAME, getName(connectionTwo));
	}

	private String getName(Connection c) throws SQLException {
		String name = null;
		String sql = "SELECT name FROM BasicTable";
		Statement stmt = c.createStatement();
		ResultSet rs = stmt.executeQuery(sql);
		if (rs.next())
		{
			name = rs.getString("name");
		}
		rs.close();
		stmt.close();
		return name;
	}

	private void initSQL(Connection c) throws SQLException {
		c.prepareStatement("DROP TABLE BasicTable IF EXISTS").execute();
		c.prepareStatement("CREATE TABLE BasicTable( ID BIGINT, NAME VARCHAR2(50),  DESCRIPTION VARCHAR2(100))").execute();
		c.prepareStatement("INSERT INTO BasicTable (id, name, description) VALUES (1, '" + BasicBean.ORIGINAL_NAME + "', 'Description')").execute();
	}

	private static void cleanSQL(Connection c) throws SQLException {
		if (!c.isClosed())
		{
			c.close();
		}
	}
}
