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

package org.apache.oodt.cas.pushpull.protocol;

//OODT imports
import org.apache.oodt.cas.protocol.ProtocolFile;

/**
 *
 * Extends {@link ProtocolFile} and links it to a {@link RemoteSite}.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class RemoteSiteFile extends ProtocolFile {

  private RemoteSite site;

  public RemoteSiteFile(ProtocolFile file, RemoteSite site){
     this(file.getPath(), file.isDir(), site);
  }

  /**
   * @param parent
   * @param path
   * @param isDir
   * @param site
   */
  public RemoteSiteFile(ProtocolFile parent, String path, boolean isDir,
      RemoteSite site) {
    super(parent, path, isDir);
    this.site = site;
  }

  /**
   * @param path
   * @param isDir
   * @param site
   */
  public RemoteSiteFile(String path, boolean isDir, RemoteSite site) {
    super(path, isDir);
    this.site = site;
  }

  /**
   * @return the site
   */
  public RemoteSite getSite() {
    return site;
  }

  /**
   * @param site
   *          the site to set
   */
  public void setSite(RemoteSite site) {
    this.site = site;
  }

  public RemoteSiteFile getRemoteParent() {
    ProtocolFile parent = super.getParent();
    return new RemoteSiteFile(parent.getPath(), parent.isDir(), this.site);
  }

  @Override
public RemoteSiteFile getAbsoluteFile() {
    ProtocolFile parent = super.getAbsoluteFile();
    return new RemoteSiteFile(parent.getPath(), parent.isDir(), this.site);
  }

}
