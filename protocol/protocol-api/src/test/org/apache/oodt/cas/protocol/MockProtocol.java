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
package org.apache.oodt.cas.protocol;

//JDK imports
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;
import org.apache.oodt.cas.protocol.util.ProtocolFileFilter;

/**
 * Mock {@link Protocol} for testing
 * 
 * @author bfoster
 */
public class MockProtocol implements Protocol {

	private boolean connected;
	private ProtocolFile cwd;
	private String factoryId;
	
	private List<ProtocolFile> deletedFiles;
	private List<ProtocolFile> putFiles;
	private List<ProtocolFile> getFiles;
	private int timesConnected;
	
	public MockProtocol(String factoryId) {
		connected = false;
		this.factoryId = factoryId;
		deletedFiles = new ArrayList<ProtocolFile>();
		putFiles = new ArrayList<ProtocolFile>();
		getFiles = new ArrayList<ProtocolFile>();
		timesConnected = 0;
	}
	
	public String getFactoryId() {
		return factoryId;
	}
	
	public void connect(String host, Authentication authentication)
			throws ProtocolException {
		connected = true;
		deletedFiles.clear();
		putFiles.clear();
		getFiles.clear();
		timesConnected++;
	}
	
	public int getTimesConnected() {
		return timesConnected;
	}

	public void close() throws ProtocolException {
		connected = false;
	}

	public boolean connected() {
		return connected;
	}

	public void cd(ProtocolFile file) throws ProtocolException {
		cwd = file;
	}
	
	public void cdHome() throws ProtocolException {
		cwd = new ProtocolFile("", true);
	}
	
	public void cdRoot() throws ProtocolException {
		cwd = new ProtocolFile("/", true);
	}

	public void get(ProtocolFile fromFile, File toFile)
			throws ProtocolException {
		getFiles.add(fromFile);
	}

	public List<ProtocolFile> getGetFiles() {
		return getFiles;
	}
	
	public void put(File fromFile, ProtocolFile toFile)
			throws ProtocolException {
		putFiles.add(toFile);
	}
	
	public List<ProtocolFile> getPutFiles() {
		return putFiles;
	}

	public ProtocolFile pwd() throws ProtocolException {
		return cwd;
	}

	public List<ProtocolFile> ls() throws ProtocolException {
		return Collections.emptyList();
	}

	public List<ProtocolFile> ls(ProtocolFileFilter filter) throws ProtocolException {
		return Collections.emptyList();
	}

	public void delete(ProtocolFile file) throws ProtocolException {
		deletedFiles.add(file);
	}
	
	public List<ProtocolFile> getDeletedFiles() {
		return deletedFiles;
	}

}
