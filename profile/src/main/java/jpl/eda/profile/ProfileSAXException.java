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


package jpl.eda.profile;

import org.xml.sax.SAXException;

/**
 * An XML-related SAX exception from a profile server.
 *
 * @author Kelly
 */
public class ProfileSAXException extends ProfileException {
	/**
	 * Create a profile SAX exception.
	 *
	 * @param cause The SAX exception that caused this profile exception.
	 */
	public ProfileSAXException(SAXException cause) {
		super(cause);
	}
}
