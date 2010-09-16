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

package org.apache.oodt.xmlps.structs;

//JDK imports
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.commons.util.DateConvert;
import org.apache.oodt.xmlquery.Result;

/**
 * 
 * <p>
 * A {@link List} of {@link CDERow}s returned from a query against a
 * Product Server.
 * </p>.
 */
public class CDEResult {

    private List<CDERow> rows;

    private static final String ROW_TERMINATOR = "$";

    public CDEResult() {
        rows = new Vector<CDERow>();
    }

    public Result toResult() {
        String strVal = toString();
        if (strVal == null || (strVal != null && strVal.equals(""))) {
            return null;
        }

        Result r = new Result();
        r.setID(DateConvert.isoFormat(new Date()));
        r.setMimeType("text/plain");
        r.setResourceID("UNKNOWN");
        r.setValue(toString());
        return r;
    }

    public String toString() {
        StringBuffer rStr = new StringBuffer();
        if (rows != null && rows.size() > 0) {
            for (Iterator<CDERow> i = rows.iterator(); i.hasNext();) {
                CDERow row = i.next();
                rStr.append(row.toString());
                rStr.append(ROW_TERMINATOR);
            }

            rStr.deleteCharAt(rStr.length() - 1);
        }

        return rStr.toString();

    }

    /**
     * @return the rows
     */
    public List<CDERow> getRows() {
        return rows;
    }

    /**
     * @param rows
     *            the rows to set
     */
    public void setRows(List<CDERow> rows) {
        this.rows = rows;
    }

}
