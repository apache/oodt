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
package org.apache.oodt.cas.protocol.config;

import java.util.List;
import java.util.Map;

import org.apache.oodt.cas.protocol.ProtocolFactory;

/**
 * Mock {@link SpringProtocolConfig}
 * 
 * @author bfoster
 */
public class MockSpringProtocolConfig extends SpringProtocolConfig {

	private static final String CONFIG_FILE = "src/testdata/test-protocol-config.xml";

	public MockSpringProtocolConfig() {
		super(CONFIG_FILE);
	}

	public String getConfigFile() {
		return CONFIG_FILE;
	}

	public Map<String, List<ProtocolFactory>> getFactoryMap() {
		return factoryMap;
	}
}
