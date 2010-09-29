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
package org.apache.oodt.cas.catalog.mapping;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

//OODT imports
import org.apache.oodt.commons.database.DatabaseConnectionBuilder;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Factory for creating DataSourceIngestMapper
 * <p>
 */
public class DataSourceIngestMapperFactory implements IngestMapperFactory {

    protected String jdbcUrl;
    protected String user;
    protected String pass;
    protected String driver;
	
	public DataSourceIngestMapperFactory() {}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	@Required
	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getUser() {
		return user;
	}
	
	@Required
	public void setUser(String user) {
		this.user = user;
	}

	public String getPass() {
		return pass;
	}

	@Required
	public void setPass(String pass) {
		this.pass = pass;
	}

	public String getDriver() {
		return driver;
	}

	@Required
	public void setDriver(String driver) {
		this.driver = driver;
	}
	
	public IngestMapper createMapper() {
		try {
			return new DataSourceIngestMapper(user, pass,
	                driver, jdbcUrl);
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
