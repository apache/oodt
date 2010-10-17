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

package org.apache.oodt.cas.pushpull.protocol.ftp;

//JDK imports
import java.io.File;
import java.util.List;
import java.util.Vector;

//Globus imports
import org.globus.ftp.FTPClient;
import org.globus.ftp.FileInfo;

//OODT imports
import org.apache.oodt.cas.pushpull.exceptions.ProtocolException;
import org.apache.oodt.cas.pushpull.protocol.Protocol;
import org.apache.oodt.cas.pushpull.protocol.ProtocolFile;
import org.apache.oodt.cas.pushpull.protocol.ProtocolPath;

/**
* 
* @author bfoster
* @version $Revision$
* 
*/
public class CogJGlobusFtpClient extends Protocol {

  private FTPClient ftp;

  private boolean isConnected;

  public CogJGlobusFtpClient() {
      super("ftp");
  }

  protected void chDir(ProtocolPath path) throws ProtocolException {
      try {
          ftp.changeDir(path.getPathString());
      } catch (Exception e) {
          throw new ProtocolException("Failed to cd to " + path + " : "
                  + e.getMessage());
      }
  }

  @Override
  public void cdToRoot() throws ProtocolException {
      try {
          chDir(new ProtocolPath("/", true));
      } catch (Exception e) {
          throw new ProtocolException("Failed to cd to root : "
                  + e.getMessage());
      }
  }

  public void connect(String host, String username, String password)
          throws ProtocolException {
      try {
          ftp = new FTPClient(host, 21);
      } catch (Exception e) {
          throw new ProtocolException("Failed to connect to: " + host + " : "
                  + e.getMessage());
      }
      isConnected = true;

      try {
          ftp.authorize(username, password);
          ftp.setActive(ftp.setLocalPassive());
      } catch (Exception e) {
          throw new ProtocolException("Failed to login to: " + host + " : "
                  + e.getMessage());
      }
  }

  public void disconnectFromServer() throws ProtocolException {
      try {
          ftp.close();
          isConnected = false;
      } catch (Exception e) {
          throw new ProtocolException("Error disconnecting from "
                  + this.getRemoteSite().getURL() + " : " + e.getMessage());
      }
  }

  public void getFile(ProtocolFile file, File toLocalFile)
          throws ProtocolException {
      try {
          ftp.setActive(ftp.setLocalPassive());
          ftp.get(file.getProtocolPath().getPathString(), toLocalFile);
      } catch (Exception e) {
          throw new ProtocolException("Failed to download: " + file.getName()
                  + " : " + e.getMessage());
      }
  }

  public void abortCurFileTransfer() throws ProtocolException {
      try {
          ftp.abort();
      } catch (Exception e) {
          throw new ProtocolException("Failed to abort file transfer : "
                  + e.getMessage());
      }
  }

  public List<ProtocolFile> listFiles() throws ProtocolException {
      try {
          ftp.setActive(ftp.setLocalPassive());
          Vector<FileInfo> fileList = (Vector<FileInfo>) ftp.list("*", null);
          Vector<ProtocolFile> returnList = new Vector<ProtocolFile>();
          String path = this.pwd().getProtocolPath().getPathString();
          for (FileInfo file : fileList) {
              returnList.add(new ProtocolFile(this.getRemoteSite(),
                      new ProtocolPath(path + "/" + file.getName(), file
                              .isDirectory())));
          }
          return returnList;
      } catch (Exception e) {
          throw new ProtocolException("Failed to get list of files : "
                  + e.getMessage());
      }
  }

  public ProtocolFile getCurrentWorkingDir() throws ProtocolException {
      try {
          return new ProtocolFile(this.getRemoteSite(), new ProtocolPath(ftp
                  .getCurrentDir(), true));
      } catch (Exception e) {
          throw new ProtocolException("Failed to pwd : " + e.getMessage());
      }
  }

  public boolean isConnected() {
      return isConnected;
  }

  public void sendNoOP() {
      try {
          // ftp.getCurrentDir();
      } catch (Exception e) {
      }
  }

  @Override
  protected boolean deleteFile(ProtocolFile file) {
      try {
          ftp.deleteFile(file.getProtocolPath().getPathString());
          return true;
      } catch (Exception e) {
          return false;
      }
  }

}
