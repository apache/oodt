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

package org.apache.oodt.commons;

import java.io.IOException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** XML entity resolver for the configuration file.
 *
 * This resolver attempts to use a locally accessible configuration.dtd so that we can
 * bootstrap enterprise applications without http access.  You see, The config file
 * specifies the list of entity directories, but the config file itself is an XML document
 * that refers to its doctype entity.  We therefore resolve the config DTD to a
 * classpath-acessible copy.
 *
 * @author Kelly
 */
class ConfigurationEntityResolver implements EntityResolver {
	public InputSource resolveEntity(String publicID, String systemID) throws SAXException, IOException {
		if (Configuration.DTD_FPI.equals(publicID) || Configuration.DTD_OLD_FPI.equals(publicID)) {
		  return new InputSource(Configuration.class.getResourceAsStream("Configuration.dtd"));
		}
		return null;
	}
}
