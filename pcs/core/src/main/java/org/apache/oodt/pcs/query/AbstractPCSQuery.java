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

package org.apache.oodt.pcs.query;

//OODT imports
import org.apache.oodt.pcs.util.FileManagerUtils;
import org.apache.oodt.cas.filemgr.structs.Element;

/**
 * Abstract functionality for PCS queries.
 * 
 * @author mattmann
 * @version $Revision$
 */
public abstract class AbstractPCSQuery implements PCSQuery {

  /* interface to the file manager so we can look up elem defs */
  protected FileManagerUtils fm = null;

  /**
   * Constructs a new AbstractOCOQuery using the given {@link FileManagerUtils}
   * file manager interface.
   * 
   * @param fm
   *          The interface to the File Manager.
   */
  protected AbstractPCSQuery(FileManagerUtils fm) {
    this.fm = fm;
  }

  /**
   * Wrapper method around .
   * 
   * @param elemName
   *          The name of the metadata element to obtain the ID for.
   * @return The String ID of the given <code>elemName</code>.
   */
  protected String getElemId(String elemName) {
    Element elem = fm.safeGetElementByName(elemName);
    if (elem == null) {
      return null;
    }

    return elem.getElementId();
  }

}
