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
package org.apache.oodt.cas.protocol.action;

//JDK imports
import java.io.File;
import java.net.URI;

//OODT imports
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.protocol.system.ProtocolManager;

/**
 * A {@link ProtocolAction} which will downlaod a url
 *
 * @author bfoster
 */
public class DownloadAction extends ProtocolAction {
	
	private String urlString;
	private String toDir;
	
	public void performAction(ProtocolManager protocolManager) throws Exception {
		URI uri = new URI(urlString);
		Protocol protocol = protocolManager.getProtocolBySite(
				new URI(uri.getScheme(), uri.getHost(), null, null),
				getAuthentication(),
				null);
		protocol.get(new ProtocolFile(uri.getPath(), false),
				(toDir != null ? new File(toDir).getAbsoluteFile() : new File(
						".").getAbsoluteFile()));
	}
	
	public void setUrl(String urlString) {
		this.urlString = urlString;
	}
	
	public void setToDir(String toDir) {
		this.toDir = toDir;
	}

}
