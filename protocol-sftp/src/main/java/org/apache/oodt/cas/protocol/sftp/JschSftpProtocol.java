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

//OODT imports
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;
import org.apache.oodt.cas.protocol.sftp.auth.HostKeyAuthentication;
import org.apache.oodt.cas.protocol.util.ProtocolFileFilter;
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFile;

//JSCH imports
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

//JDK imports
import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 * 
 * An implement of an SFTP provider based on <a
 * href="http://www.jcraft.org">Jcraft's</a> <a
 * href="http://www.jcraft.org/jsch/">JSCH</a> library.
 * 
 * @author bfoster
 * @version $Revision$
 */
public class JschSftpProtocol implements Protocol {

  private Session session;

  private ChannelSftp sftpChannel;

  private ProtocolFile homeDir;

  private int port;
  
  private static final JSch jsch = new JSch();

  public JschSftpProtocol() {
  	this(22);
  }
 
  public JschSftpProtocol(int port) {
    session = null;
    sftpChannel = null;
    this.port = port;
  }
  
  public void cd(ProtocolFile file) throws ProtocolException {
    try {
      sftpChannel.cd(file.getPath());
    } catch (Exception e) {
      throw new ProtocolException("Failed to cd to " + file + " : "
          + e.getMessage());
    }
  }
  
  public void cdRoot() throws ProtocolException {
  	cd(new ProtocolFile(ProtocolFile.SEPARATOR, true));
  }
  
  public void cdHome() throws ProtocolException {
  	cd(homeDir);
  }

  public void connect(String host, final Authentication auth) throws ProtocolException {
    try {
    	if (auth instanceof HostKeyAuthentication) {
    		jsch.setKnownHosts(((HostKeyAuthentication) auth).getHostKeyFile());    		
    	} else {
    		jsch.setKnownHosts(System.getProperty("user.home") + "/.ssh/known_hosts");
    	}
      session = jsch.getSession(auth.getUser(), host, this.port);
      session.setUserInfo(new UserInfo() {
				public String getPassphrase() {
					return (auth instanceof HostKeyAuthentication) ? ((HostKeyAuthentication) auth)
							.getPassphrase() : null;
				}
				public String getPassword() {
					return auth.getPass();
				}
				public boolean promptPassphrase(String arg0) {
					return (auth instanceof HostKeyAuthentication && ((HostKeyAuthentication) auth)
							.getPassphrase() != null);
				}
				public boolean promptPassword(String arg0) {
					return true;
				}
				public boolean promptYesNo(String arg0) {
					return false;
				}
				public void showMessage(String arg0) {}
      });
      session.connect();
      sftpChannel = (ChannelSftp) session.openChannel("sftp");
      sftpChannel.connect();
      homeDir = pwd();
    } catch (Exception e) {
      throw new ProtocolException("Failed to connect to host " + host + " : "
          + e.getMessage(), e);
    }
  }

  public void close() throws ProtocolException {
    session.disconnect();
  }

  public void get(ProtocolFile fromFile, File toFile)
      throws ProtocolException {
    try {
      sftpChannel.get(fromFile.getPath(), toFile
          .getAbsolutePath());
    } catch (Exception e) {
      throw new ProtocolException("Failed to download " + fromFile + " : "
          + e.getMessage());
    }
  }
  
  public void put(File fromFile, ProtocolFile toFile) throws ProtocolException {
	  try {
		  sftpChannel.put(fromFile.getAbsolutePath(), toFile.getPath());
	  } catch (Exception e) {
		  throw new ProtocolException("Failed to put file '" + fromFile + "' : " + e.getMessage(), e);
	  }
  }
  
  public List<ProtocolFile> ls() throws ProtocolException {
    try {
      Vector<ChannelSftp.LsEntry> sftpFiles = (Vector<ChannelSftp.LsEntry>) sftpChannel
          .ls(sftpChannel.pwd());
      Vector<ProtocolFile> returnFiles = new Vector<ProtocolFile>();
      for (ChannelSftp.LsEntry sftpFile : sftpFiles) {
        String path = this.pwd().getPath();
        returnFiles.add(new ProtocolFile(path + "/" + sftpFile.getFilename(), sftpFile
                .getAttrs().isDir()));
      }
      return returnFiles;
    } catch (Exception e) {
      throw new ProtocolException("Failed to get file list : " + e.getMessage());
    }
  }

	public List<ProtocolFile> ls(ProtocolFileFilter filter)
			throws ProtocolException {
    try {
      Vector<ChannelSftp.LsEntry> sftpFiles = (Vector<ChannelSftp.LsEntry>) sftpChannel
          .ls(sftpChannel.pwd());
      Vector<ProtocolFile> returnFiles = new Vector<ProtocolFile>();
      for (ChannelSftp.LsEntry sftpFile : sftpFiles) {
        String path = this.pwd().getPath();
        ProtocolFile pFile = new ProtocolFile(path + "/" + sftpFile.getFilename(), sftpFile
            .getAttrs().isDir());
        if (filter.accept(pFile)) {
        	returnFiles.add(pFile);
        }
      }
      return returnFiles;
    } catch (Exception e) {
      throw new ProtocolException("Failed to get file list : " + e.getMessage());
    }
	}
	
  public ProtocolFile pwd() throws ProtocolException {
    try {
      return new ProtocolFile(sftpChannel.pwd(), true);
    } catch (Exception e) {
      throw new ProtocolException("Failed to pwd : " + e.getMessage());
    }
  }

  public boolean connected() {
    return session.isConnected();
  }

  public void delete(ProtocolFile file) throws ProtocolException {
    try {
      sftpChannel.rm(file.getPath());
    } catch (Exception e) {
      throw new ProtocolException("Failed to download file '" + file + "' : " + e.getMessage(), e);
    }
  }

}
