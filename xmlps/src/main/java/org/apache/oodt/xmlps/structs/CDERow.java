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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;


/**
 * 
 * <p>
 * A row of {@link CDEValue}s, returned
 * from a query against a Product Server.
 * </p>.
 */
public class CDERow {

    private List<CDEValue> vals;
    
    private static final String COL_SEPARATOR = "\t";

    public CDERow() {
        vals = new Vector<CDEValue>();
    }

    public String toString() {
        StringBuilder rStr = new StringBuilder();
        if (vals != null && vals.size() > 0) {
            for (Iterator<CDEValue> i = vals.iterator(); i.hasNext();) {
                CDEValue v = i.next();
                rStr.append(v.getVal() + COL_SEPARATOR);
            }

            rStr.deleteCharAt(rStr.length() - 1);
        }

        return rStr.toString();
    }

    /**
     * @return the vals
     */
    public List<CDEValue> getVals() {
        return vals;
    }

    /**
     * @param vals
     *            the vals to set
     */
    public void setVals(List<CDEValue> vals) {
        this.vals = vals;
    }

}
