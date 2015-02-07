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
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;

/**
 * 
 * A query with a <code>StartDateTime</code> and an <code>EndDateTime</code>
 * range that identifies a set of matching {@link Product}s.
 * 
 * @author mattmann
 * @version $Revision$
 */
public class TemporalQuery extends AbstractPCSQuery {

  private String startDateTime;

  private String endDateTime;

  private String temporalFld;

  public TemporalQuery(FileManagerUtils fm, String startDateTime,
      String endDateTime, String temporalFld) {
    super(fm);
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.temporalFld = temporalFld;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.pcs.query.PCSQuery#buildQuery()
   */
  public Query buildQuery() {
    Query query = new Query();
    RangeQueryCriteria crit = new RangeQueryCriteria();
    crit.setElementName(this.temporalFld);
    crit.setInclusive(true);

    if (this.startDateTime != null) {
      crit.setStartValue(this.startDateTime);
    }

    if (this.endDateTime != null) {
      crit.setEndValue(this.endDateTime);
    }

    query.addCriterion(crit);
    return query;
  }

}
