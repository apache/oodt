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
package org.apache.oodt.cas.protocol.sftp;

//JDK imports
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.UUID;

//Apache imports
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//SshTools imports
import com.sshtools.daemon.platform.NativeAuthenticationProvider;
import com.sshtools.daemon.platform.PasswordChangeException;

/**
 * This authentication provider provides no authentication at all, and just lets
 * anybody in so you should never use it!
 * 
 * It is really just for testing.
 */
public class DummyAuthenticationProvider extends NativeAuthenticationProvider {

	Log log = LogFactory.getLog(DummyAuthenticationProvider.class);

	private HashMap<String, String> userHomeDirs;

	public DummyAuthenticationProvider() {
		log.error("DummyAuthenticationProvider is in use. This is only for testing.");
		userHomeDirs = new HashMap<String, String>();
	}

	@Override
	public boolean changePassword(String username, String oldpassword,
			String newpassword) {
		return false;
	}

	@Override
	public String getHomeDirectory(String username) throws IOException {
		String homeDir = userHomeDirs.get(username);
		if (homeDir != null) {
			return homeDir;
		}
		return setupHomeDir(username);
	}

	@Override
	public void logoffUser() throws IOException {

	}

	@Override
	public boolean logonUser(String username, String password)
			throws PasswordChangeException, IOException {
		return true;
	}

	@Override
	public boolean logonUser(String username) throws IOException {
		return true;
	}

	private String setupHomeDir(String username) throws IOException {
		File tempFile = File.createTempFile("bogus", "bogus");
		File tmpDir = tempFile.getParentFile();
		tempFile.delete();
		FileUtils.forceDeleteOnExit(tmpDir);
		File homeDirFile = new File(tmpDir, "JschSftpTest/" + UUID.randomUUID());
		File sshTestDir = new File(homeDirFile, "sshTestDir");
		sshTestDir.mkdirs();
		PrintStream ps = null;
		try {
			ps = new PrintStream(new FileOutputStream(new File(sshTestDir, "sshTestFile")));
			ps.println("This is a ");
			ps.println("test");
			ps.println("file to test SFTP GET");
		} catch (IOException e) {
			throw e;
		} finally {
			try { ps.close(); } catch (Exception ignore) {}
		}
		System.out.println("HOME: " + homeDirFile.getAbsolutePath());
		userHomeDirs.put(username, homeDirFile.getAbsolutePath());
		return homeDirFile.getAbsolutePath();
	}
}
