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

//JDK imports
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

//Google imports

/**
 * Configuration information about which files should be staged and where.
 *
 * @author bfoster (Brian Foster)
 */
public class FileStagingInfo {

   private final String stagingDir;
   private final boolean forceStaging;
   private final Set<String> filePaths;
   private final Set<String> productIds;

   public FileStagingInfo(String stagingDir) {
      this(stagingDir, false);
   }

   public FileStagingInfo(String stagingDir, boolean forceStaging) {
      this.stagingDir = stagingDir;
      this.forceStaging = forceStaging;
      filePaths = Sets.newHashSet();
      productIds = Sets.newHashSet();
   }

   public void addFilePath(String filePath) {
      filePaths.add(filePath);
   }

   public void addFilePaths(List<String> filePaths) {
      this.filePaths.addAll(filePaths);
   }

   public List<String> getFilePaths() {
      return Lists.newArrayList(filePaths);
   }

   public void addProductId(String productId) {
      productIds.add(productId);
   }

   public void addProductIds(List<String> productIdsInc) {
      productIds.addAll(productIdsInc);
   }

   public List<String> getProductIds() {
      return Lists.newArrayList(productIds);
   }

   public String getStagingDir() {
      return stagingDir;
   }

   public boolean isForceStaging() {
      return forceStaging;
   }
}
