/**
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
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFile;

//JSCH imports
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;

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

  private static final JSch jsch = new JSch();

  public JschSftpProtocol() {
    session = null;
    sftpChannel = null;
  }

  public void cd(ProtocolFile file) throws ProtocolException {
    try {
      sftpChannel.cd(file.getPath());
    } catch (Exception e) {
      throw new ProtocolException("Failed to cd to " + file + " : "
          + e.getMessage());
    }
  }

  public void connect(String host, Authentication auth) throws ProtocolException {
    try {
      System.out.println(System.getProperty("user.home") + "/.ssh/known_hosts");
      jsch.setKnownHosts(System.getProperty("user.home") + "/.ssh/known_hosts");
      session = jsch.getSession(auth.getUser(), host, 22);
      session.setPassword(auth.getPass());
      session.connect();
      sftpChannel = (ChannelSftp) session.openChannel("sftp");
      sftpChannel.connect();
    } catch (Exception e) {
      throw new ProtocolException("Failed to connect to host " + host + " : "
          + e.getMessage());
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
