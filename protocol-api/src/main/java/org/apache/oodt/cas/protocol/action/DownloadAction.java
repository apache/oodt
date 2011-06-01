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
import java.net.URISyntaxException;

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
	
	protected URI uri;
	protected String toDir;
	protected Protocol usedProtocol;
	
	public void performAction(ProtocolManager protocolManager) throws Exception {
		usedProtocol = createProtocol(protocolManager);
		ProtocolFile fromFile = createProtocolFile();
		String toFilename = fromFile.equals(ProtocolFile.ROOT)
				|| fromFile.getName().isEmpty() ? uri.getHost() : fromFile
				.getName();
		File toFile = (toDir != null) ? new File(toDir, toFilename) : new File(toFilename);
		usedProtocol.get(fromFile, toFile.getAbsoluteFile());
	}
	
	public void setUrl(String urlString) throws URISyntaxException {
		uri = new URI(urlString);
	}
	
	public void setToDir(String toDir) {
		this.toDir = toDir;
	}

	protected Protocol getUsedProtocol() {
		return usedProtocol;
	}
	
	protected ProtocolFile createProtocolFile() throws URISyntaxException {
		return new ProtocolFile(uri.getPath(), false);
	}
	
	protected Protocol createProtocol(ProtocolManager protocolManager) throws URISyntaxException {
		return protocolManager.getProtocolBySite(
				new URI(uri.getScheme(), uri.getHost(), null, null),
				getAuthentication(),
				null);
	}
}
