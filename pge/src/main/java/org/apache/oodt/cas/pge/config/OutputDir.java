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
package org.apache.oodt.cas.pge.config;

//Apache imports
import org.apache.commons.lang.Validate;

/**
 * An output directory where PGE will create files.
 * 
 * @author bfoster
 * @version $Revision$
 */
public class OutputDir {

   private String path;
   private boolean createBeforeExe;

   public OutputDir() {
      path = null;
      createBeforeExe = false;
   }

   public OutputDir(String path, boolean createBeforeExe) {
      setPath(path);
      setCreateBeforeExe(createBeforeExe);
   }

   public void setPath(String path) {
      Validate.notNull(path, "path cannot be null");

      this.path = path;
   }

   public String getPath() {
      return path;
   }

   public void setCreateBeforeExe(boolean createBeforeExe) {
      this.createBeforeExe = createBeforeExe;
   }

   public boolean isCreateBeforeExe() {
      return createBeforeExe;
   }
}
