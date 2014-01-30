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

package org.apache.oodt.cas.filemgr.versioning;

//JDK imports
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.exceptions.VersioningException;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;

/**
 * 
 * Store products under /[AcquisitionDate]/[Filename].
 * 
 * Assumes the existence of:
 * 
 * <pre>
 *   &lt;code&gt;AcquisitionDate&lt;/code&gt; - if present, takes as is, and assumes 
 *   it is in the yyMMdd format.&lt;br/&gt;
 *   &lt;code&gt;StartDateTime&lt;/code&gt; - if present, assumes it is in yyyy-MM-ddTHH:mm:ss.000Z, 
 *   or ISO8601 format, reformats it to yyMMdd, and then uses it for the value
 *   of &lt;code&gt;AcquisitionDate&lt;/code&gt;&lt;br/&gt;
 * </pre>
 * 
 * If neither of the above met fields are present, the Versioner will generate a
 * new DateTime, and use the value of it (formatted as yyMMdd) for the
 * AcquisitionDate.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class AcquisitionDateVersioner extends MetadataBasedFileVersioner {

  private String filePathSpec = "/[AcquisitionDate]/[Filename]";

  protected final static String ACQUISITION_DATE = "AcquisitionDate";

  protected static String ACQ_DATE_FORMAT = "yyMMdd";

  protected final static String START_DATE_TIME = "StartDateTime";

  public AcquisitionDateVersioner() {
    setFilePathSpec(filePathSpec);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.filemgr.versioning.MetadataBasedFileVersioner#
   * createDataStoreReferences(org.apache.oodt.cas.filemgr.structs.Product,
   * org.apache.oodt.cas.metadata.Metadata)
   */
  @Override
  public void createDataStoreReferences(Product product, Metadata metadata)
      throws VersioningException {

    // compute AcquisitionDate
    // grab CAS.ProductReceivedTime
    // parse it as an ISO8601 date
    // then reformat and add using ACQ_DATE_FORMAT
    if (metadata.getMetadata(ACQUISITION_DATE) == null) {
      SimpleDateFormat acqDateFormatter = new SimpleDateFormat(ACQ_DATE_FORMAT);
      acqDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
      Date casProdReceivedTime = new Date();
      if (metadata.getMetadata(START_DATE_TIME) != null) {
        try {
          metadata.replaceMetadata(ACQUISITION_DATE, PathUtils
              .doDynamicReplacement("[FORMAT(yyyy-MM-dd'T'HH:mm:ss.SSS'Z',"
                  + metadata.getMetadata(START_DATE_TIME) + ","
                  + ACQ_DATE_FORMAT + ")]"));
        } catch (Exception e) {
          throw new VersioningException("Failed to parse StartDateTime : "
              + e.getMessage(), e);
        }
      } else {
        metadata.replaceMetadata(ACQUISITION_DATE, acqDateFormatter
            .format(casProdReceivedTime));
      }
    }

    super.createDataStoreReferences(product, metadata);
  }

}
