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
package org.apache.oodt.cas.protocol.http;

//OODT imports
import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.protocol.ProtocolFile;

//JDK imports
import java.net.URL;

/**
 * HTTP extension of {@link ProtocolFile}
 * 
 * @author bfoster
 * @version $Revision$
 */
public class HttpFile extends ProtocolFile {

	private static final long serialVersionUID = -7780059889413081800L;

	private URL link;

	public HttpFile(String virtualPath, boolean isDir, URL link) {
		this(null, virtualPath, isDir, link);
	}
	
	public HttpFile(HttpFile parent, String virtualPath, boolean isDir, URL link) {
		super(parent, virtualPath, isDir);
		Validate.notNull(link, "URL link must not be NULL");
		this.link = link;
	}

	public URL getLink() {
		return this.link;
	}
}
