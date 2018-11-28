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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

//SshTools imports
import com.sshtools.daemon.platform.NativeProcessProvider;
import com.sshtools.j2ssh.io.DynamicBuffer;

/**
 * This is a shell provider that prints a message saying that we don't support
 * shell access, and then closes the connection.
 */
public final class UnsupportedShellProcessProvider extends
		NativeProcessProvider {

	private static final String MESSAGE = "This server does not provide shell access, only SFTP. Goodbye.\n";

	private DynamicBuffer stdin = new DynamicBuffer();
	private DynamicBuffer stderr = new DynamicBuffer();
	private DynamicBuffer stdout = new DynamicBuffer();

	@Override
	public boolean createProcess(final String command, final Map environment)
			throws IOException {
		return true;
	}

	@Override
	public String getDefaultTerminalProvider() {
		return "UnsupportedShell";
	}

	@Override
	public void kill() {
		try {
			stdin.getInputStream().close();
			stdin.getOutputStream().close();
		} catch (Exception ex) {
		}
		try {
			stdout.getInputStream().close();
			stdout.getOutputStream().close();
		} catch (Exception ex1) {
		}
		try {
			stderr.getInputStream().close();
			stderr.getOutputStream().close();
		} catch (Exception ex2) {
		}
	}

	@Override
	public void start() throws IOException {
		stdin.getOutputStream().write(MESSAGE.getBytes());
	}

	@Override
	public boolean stillActive() {
		try {
			return stdin.getInputStream().available() > 0;
		} catch (IOException ex) {
			return false;
		}
	}

	@Override
	public boolean supportsPseudoTerminal(final String term) {
		return true;
	}

	@Override
	public boolean allocatePseudoTerminal(final String term, final int cols,
			final int rows, final int width, final int height, final String modes) {
		return true;
	}

	@Override
	public int waitForExitCode() {
		return 0;
	}

	public InputStream getInputStream() throws IOException {
		return stdin.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return stdout.getOutputStream();
	}

	public InputStream getStderrInputStream() {
		return stderr.getInputStream();
	}

}
