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
package org.apache.oodt.cas.protocol.ftp;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

//APACHE imports
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;
import org.apache.oodt.cas.protocol.util.ProtocolFileFilter;

/**
 * This class is responsible for FTP transfers. It is built as a wrapper around
 * Apache's FTPClient class in order to connect it into the Crawler's Protocol
 * infrastructure.
 *
 * @author bfoster
 *
 */
public class CommonsNetFtpProtocol implements Protocol {

	private final FTPClient ftp;
	private String homeDir;

	/**
	 * Creates a new FtpClient
	 */
	public CommonsNetFtpProtocol() {
		ftp = new FTPClient();
	}

	/**
	 * {@inheritDoc}
	 */
	public void connect(String host, Authentication auth)
			throws ProtocolException {
		// server cannot be null
		if (host == null) {
			throw new ProtocolException("Tried to connect to server == NULL");
		}

		try {
			ftp.connect(host);
			ftp.enterLocalPassiveMode();
		} catch (Exception e) {
			throw new ProtocolException("Failed to connect to server : "
					+ e.getMessage());
		}

		try {
			// try logging in
			if (!ftp.login(auth.getUser(), auth.getPass())) {
				throw new ProtocolException("Failed logging into host " + host
						+ " as user " + auth.getUser());
			}

			// set file type to binary
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);

			homeDir = ftp.printWorkingDirectory();
		} catch (Exception e) {
			// login failed
			throw new ProtocolException("Exception thrown while logging into host "
					+ host + " as user " + auth.getUser());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ProtocolFile pwd() throws ProtocolException {
		try {
			return new ProtocolFile(ftp.printWorkingDirectory(), true);
		} catch (Exception e) {
			throw new ProtocolException("Failed to pwd : " + e.getMessage());
		}
	}

	public List<ProtocolFile> ls() throws ProtocolException {
		try {
			String path = this.pwd().getPath();
			FTPFile[] files = ftp.listFiles();
			List<ProtocolFile> returnFiles = new LinkedList<ProtocolFile>();
			for (int i = 0; i < files.length; i++) {
				FTPFile file = files[i];
				if (file == null)
					continue;
				returnFiles.add(new ProtocolFile(path + "/" + file.getName(), file
						.isDirectory()));
			}
			return returnFiles;
		} catch (Exception e) {
			throw new ProtocolException("Failed to get file list : " + e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ProtocolFile> ls(ProtocolFileFilter filter)
			throws ProtocolException {
		try {
			FTPFile[] files = ftp.listFiles();
			List<ProtocolFile> returnFiles = new LinkedList<ProtocolFile>();
			for (int i = 0; i < files.length; i++) {
				FTPFile file = files[i];
				if (file == null)
					continue;
				String path = this.pwd().getPath();
				ProtocolFile pFile = new ProtocolFile(path + "/" + file.getName(), file
						.isDirectory());
				if (filter.accept(pFile)) {
					returnFiles.add(pFile);
				}
			}
			return returnFiles;
		} catch (Exception e) {
			throw new ProtocolException("Failed to get file list : " + e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void get(ProtocolFile fromFile, File toFile) throws ProtocolException {
		// file or toLocalFile cannot be null
		if (fromFile == null || toFile == null) {
			throw new ProtocolException(
					"Can't download file -> ProtocolFile == null || toLocalFile == null");
		}

		// download file
		OutputStream os = null;
		try {
			os = new FileOutputStream(toFile);
			if (!ftp.retrieveFile(fromFile.getName(), os)) {
				throw new ProtocolException("Failed to download file "
						+ fromFile.getName());
			}
		} catch (Exception e) {
			// download failed
			toFile.delete();
			throw new ProtocolException("FAILED to download: " + fromFile.getName()
					+ " : " + e.getMessage(), e);
		} finally {
			// close output stream
			if (os != null)
				try {
					os.close();
				} catch (Exception e) {
					toFile.delete();
					throw new ProtocolException("Failed to close outputstream : "
							+ e.getMessage(), e);
				}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void put(File fromFile, ProtocolFile toFile) throws ProtocolException {
		try {
			ftp.storeFile(toFile.getPath(), new FileInputStream(fromFile));
		} catch (Exception e) {
			throw new ProtocolException("Failed to put file '" + fromFile + "' : "
					+ e.getMessage(), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void cd(ProtocolFile file) throws ProtocolException {
		try {
			if (!ftp.changeWorkingDirectory(file.getPath()))
				throw new Exception("Directory change method returned false");
		} catch (Exception e) {
			throw new ProtocolException("Failed to cd to " + file.getPath() + " : "
					+ e.getMessage());
		}
	}

	public void cdRoot() throws ProtocolException {
		cd(new ProtocolFile(ProtocolFile.SEPARATOR, true));
	}

	public void cdHome() throws ProtocolException {
		cd(new ProtocolFile(homeDir, true));
	}

	/**
	 * {@inheritDoc}
	 */
	public void close() throws ProtocolException {
		try {
			ftp.disconnect();
		} catch (Exception e) {
			throw new ProtocolException("Failed to disconnect from server");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean connected() {
		return ftp.isConnected();
	}

	/**
	 * {@inheritDoc}
	 */
	public void delete(ProtocolFile file) throws ProtocolException {
		try {
			ftp.deleteFile(file.getPath());
		} catch (Exception e) {
			throw new ProtocolException("Failed to delete file '" + file.getPath()
					+ "' : " + e.getMessage(), e);
		}
	}
}
