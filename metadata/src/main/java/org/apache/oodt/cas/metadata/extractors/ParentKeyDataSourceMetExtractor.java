/**
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.oodt.cas.metadata.extractors;

import java.io.File;
import java.util.List;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * Supports data source key look via parent key if files key is no good.
 * 
 * Expected file name:
 * <parent_key>-<primary_key>.<post_fix>
 * 
 * @author bfoster@apache.com (Brian Foster)
 */
public class ParentKeyDataSourceMetExtractor extends DataSourceMetExtractor {

  private String key;
  
  @Override
  protected Metadata extrMetadata(File file) throws MetExtractionException {
    try {
      key = getPrimaryKey(file);
      return super.extrMetadata(file);
    } catch (MetExtractionException e) {
      key = getParentKey(file);
      if (key != null) {
        return super.extrMetadata(file);
      } else {
        throw e;
      }
    }
  }

  @VisibleForTesting
  protected String getKey(File file) {
    return key;
  }

  private String getPrimaryKey(File file) {
    String key = getKeyAtIndex(file, Index.PRIMARY);
    return key == null ? super.getKey(file) : key;
  }

  private String getParentKey(File file) {
    return getKeyAtIndex(file, Index.PARENT);
  }

  private String getKeyAtIndex(File file, Index index) {
    String key = super.getKey(file);
    List<String> splitKey = Lists.newArrayList(Splitter.on("_").split(key));
    if (splitKey.size() == 2) {
      return splitKey.get(index.getNumeric());
    } else {
      return null;
    }
  }

  private enum Index {
    PRIMARY(1),
    PARENT(0);
    
    private int index;

    Index(int index) {
      this.index = index;
    }

    public int getNumeric() {
      return index;
    }
  }
}
