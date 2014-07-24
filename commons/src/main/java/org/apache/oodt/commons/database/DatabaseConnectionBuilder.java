/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.commons.database;

//JDK imports
import javax.sql.DataSource;




//APACHE imports
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A class to build database connections from JDBC information.
 * </p>.
 */
public final class DatabaseConnectionBuilder {

  /**
   * Static method which can be used to build the database connection.
   * @param user the username associated with any connection
   * @param pass the users password
   * @param driver the JDBS driver class name to be used for making connections
   * @param url the JDBC URL at which the databse end point resides.
   * @return a configured {@link javax.sql.DataSource} which we can use to make connections to.
   */
    public static final DataSource buildDataSource(String user, String pass,
            String driver, String url) {

        DataSource ds = null;

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ignore) {
        }

        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(url, user, pass);
        
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
                connectionFactory, null);

        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        ObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<PoolableConnection>(poolableConnectionFactory); 
        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        @SuppressWarnings("unused")
        PoolingDataSource<PoolableConnection> dataSource =
        new PoolingDataSource<PoolableConnection>(connectionPool);

        return ds;
    }

}
