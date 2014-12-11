/*
 * DataSourceDelegate.java
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

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author Johnathan Ingram (jingram@rogueware.org)
 */
public class DataSourceDelegate implements javax.sql.DataSource, Serializable {

   private DataSource ds;

   public DataSourceDelegate(DataSource ds) {
      this.ds = ds;
   }

   @Override
   public Connection getConnection() throws SQLException {
      return ds.getConnection();
   }

   @Override
   public Connection getConnection(String username, String password) throws SQLException {
      return ds.getConnection(username, password);
   }

   @Override
   public PrintWriter getLogWriter() throws SQLException {
      return ds.getLogWriter();
   }

   @Override
   public void setLogWriter(PrintWriter out) throws SQLException {
      ds.setLogWriter(out);
   }

   @Override
   public void setLoginTimeout(int seconds) throws SQLException {
      ds.setLoginTimeout(seconds);
   }

   @Override
   public int getLoginTimeout() throws SQLException {
      return ds.getLoginTimeout();
   }

   @Override
   public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return ds.getParentLogger();
   }

   @Override
   public <T> T unwrap(Class<T> iface) throws SQLException {
      return ds.unwrap(iface);
   }

   @Override
   public boolean isWrapperFor(Class<?> iface) throws SQLException {
      return ds.isWrapperFor(iface);
   }
}
