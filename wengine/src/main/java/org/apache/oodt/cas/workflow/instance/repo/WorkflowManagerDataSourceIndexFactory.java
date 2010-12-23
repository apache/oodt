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
package org.apache.oodt.cas.workflow.instance.repo;

//OODT imports
import org.apache.oodt.cas.catalog.struct.IndexFactory;
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Factory for creating a cas-catalog workflow data source index
 * <p>
 */
public class WorkflowManagerDataSourceIndexFactory implements IndexFactory {

    protected String jdbcUrl;
    protected String user;
    protected String pass;
    protected String driver;
	
	public WorkflowManagerDataSourceIndex createIndex() {
		try {
			return new WorkflowManagerDataSourceIndex(DatabaseConnectionBuilder.buildDataSource(this.user, this.pass, this.driver, this.jdbcUrl));
		}catch (Exception e) {
			//log
			return null;
		}
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}
	
}
