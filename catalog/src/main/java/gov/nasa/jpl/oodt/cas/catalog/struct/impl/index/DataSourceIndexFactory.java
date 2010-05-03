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

package gov.nasa.jpl.oodt.cas.catalog.struct.impl.index;

//OODT imports
import gov.nasa.jpl.oodt.cas.catalog.struct.Index;
import gov.nasa.jpl.oodt.cas.catalog.struct.IndexFactory;

/**
 * 
 * Factory for creating DataSourceIndex(s).
 * 
 * @author bfoster
 *
 */
public class DataSourceIndexFactory implements IndexFactory {

    protected String jdbcUrl;
    protected String user;
    protected String pass;
    protected String driver;
    protected boolean useUTF8;
	
    public DataSourceIndexFactory() {
    	this.useUTF8 = false;
    }
    
	public Index createIndex() {
		return new DataSourceIndex(this.user, this.pass, this.driver, this.jdbcUrl, this.useUTF8);
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
	
	public void setUseUTF8(boolean useUTF8) {
		this.useUTF8 = useUTF8;
	}

}
