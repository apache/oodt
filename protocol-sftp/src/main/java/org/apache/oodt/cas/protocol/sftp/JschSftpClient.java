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

package org.apache.oodt.cas.pushpull.protocol.sftp;

//OODT imports
import org.apache.oodt.cas.pushpull.exceptions.ProtocolException;
import org.apache.oodt.cas.pushpull.protocol.Protocol;
import org.apache.oodt.cas.pushpull.protocol.ProtocolFile;
import org.apache.oodt.cas.pushpull.protocol.ProtocolPath;

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
public class JschSftpClient extends Protocol {

  private Session session;

  private ChannelSftp sftpChannel;

  private static final JSch jsch = new JSch();

  private String host;

  private boolean abort;

  public JschSftpClient() {
    super("sftp");
    session = null;
    sftpChannel = null;
    host = null;
  }

  protected void chDir(ProtocolPath path) throws ProtocolException {
    try {
      sftpChannel.cd(path.getPathString());
    } catch (Exception e) {
      throw new ProtocolException("Failed to cd to " + path + " : "
          + e.getMessage());
    }
  }

  public void cdToRoot() throws ProtocolException {
    try {
      chDir(new ProtocolPath("/", true));
    } catch (Exception e) {
      throw new ProtocolException("Failed to cd to root : " + e.getMessage());
    }
  }

  protected void connect(String host, final String username,
      final String password) throws ProtocolException {
    try {
      System.out.println(System.getProperty("user.home") + "/.ssh/known_hosts");
      jsch.setKnownHosts(System.getProperty("user.home") + "/.ssh/known_hosts");
      session = jsch.getSession(username, this.getRemoteSite().getURL()
          .getHost(), 22);
      session.setPassword(password);
      session.connect();
      sftpChannel = (ChannelSftp) session.openChannel("sftp");
      sftpChannel.connect();
    } catch (Exception e) {
      throw new ProtocolException("Failed to connect to host " + host + " : "
          + e.getMessage());
    }
  }

  public void disconnectFromServer() throws ProtocolException {
    session.disconnect();
  }

  public void getFile(ProtocolFile file, File toLocalFile)
      throws ProtocolException {
    try {
      this.abort = false;
      SftpProgressMonitor monitor = new SftpProgressMonitor() {
        public boolean count(long arg0) {
          return JschSftpClient.this.abort;
        }

        public void end() {

        }

        public void init(int arg0, String arg1, String arg2, long arg3) {

        }
      };
      sftpChannel.get(file.getProtocolPath().getPathString(), toLocalFile
          .getAbsolutePath());
    } catch (Exception e) {
      throw new ProtocolException("Failed to download " + file + " : "
          + e.getMessage());
    }
  }

  public void abortCurFileTransfer() {
    this.abort = true;
  }

  public List<ProtocolFile> listFiles() throws ProtocolException {
    try {
      Vector<ChannelSftp.LsEntry> sftpFiles = (Vector<ChannelSftp.LsEntry>) sftpChannel
          .ls(sftpChannel.pwd());
      Vector<ProtocolFile> returnFiles = new Vector<ProtocolFile>();
      for (ChannelSftp.LsEntry sftpFile : sftpFiles) {
        String path = this.pwd().getProtocolPath().getPathString();
        returnFiles.add(new ProtocolFile(this.getRemoteSite(),
            new ProtocolPath(path + "/" + sftpFile.getFilename(), sftpFile
                .getAttrs().isDir())));
      }
      return returnFiles;
    } catch (Exception e) {
      throw new ProtocolException("Failed to get file list : " + e.getMessage());
    }
  }

  public ProtocolFile getCurrentWorkingDir() throws ProtocolException {
    try {
      return new ProtocolFile(this.getRemoteSite(), new ProtocolPath(
          sftpChannel.pwd(), true));
    } catch (Exception e) {
      throw new ProtocolException("Failed to pwd : " + e.getMessage());
    }
  }

  public boolean isConnected() throws ProtocolException {
    return session.isConnected();
  }

  @Override
  protected boolean deleteFile(ProtocolFile file) {
    try {
      sftpChannel.rm(file.getProtocolPath().getPathString());
      return true;
    } catch (Exception e) {
      return false;
    }
  }

}
