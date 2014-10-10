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
