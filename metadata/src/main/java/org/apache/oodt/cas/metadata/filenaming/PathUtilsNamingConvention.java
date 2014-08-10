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
package org.apache.oodt.cas.metadata.filenaming;

//JDK imports
import java.io.File;
import java.io.IOException;

import java.util.List;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.NamingConventionException;
import org.apache.oodt.cas.metadata.util.PathUtils;

/**
 * A {@link NamingConvention} which utilizes {@link PathUtils}.
 *
 * @author bfoster (Brian Foster)
 * @author mattmann (Chris Mattmann)
 */
public class PathUtilsNamingConvention implements NamingConvention {

   private String namingConv;

   private Metadata tmpReplaceMet;
   
   public PathUtilsNamingConvention(){
	   this.tmpReplaceMet = new Metadata();
   }
   
   public File rename(File file, Metadata metadata)
         throws NamingConventionException {
      try {
         Validate.notNull(file, "Must specify file");
         Validate.notNull(metadata, "Must specify metadata");

         File newFile = new File(file.getParentFile(),
               PathUtils.doDynamicReplacement(namingConv, metadata));
         if (!file.renameTo(newFile)) {
            throw new IOException("Renaming file [" + file + "] to [" + newFile
                  + "] returned false");
         }
         return newFile;
      } catch (Exception e) {
         throw new NamingConventionException("Failed to renaming file [" + file
               + "] : " + e.getMessage(), e);
      }
   }

   public void setNamingConv(String namingConv) {
      this.namingConv = namingConv;
   }
   
	public void addTmpReplaceMet(String key, List<String> values) {
		this.tmpReplaceMet.replaceMetadata(key, values);
	}
	
	public Metadata getTmpReplaceMet() {
		return this.tmpReplaceMet;
	}
}
