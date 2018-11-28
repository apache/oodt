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

/**
 * 
 * <p>
 * A cde name and a value returned from a query against a Product Server
 * </p>.
 */
public class CDEValue {

    private String cdeName;

    private String val;

    public CDEValue() {

    }

    public CDEValue(String cdeName, String val) {
        this.cdeName = cdeName;
        this.val = val;
    }

    /**
     * @return the cdeName
     */
    public String getCdeName() {
        return cdeName;
    }

    /**
     * @param cdeName
     *            the cdeName to set
     */
    public void setCdeName(String cdeName) {
        this.cdeName = cdeName;
    }

    /**
     * @return the val
     */
    public String getVal() {
        return val;
    }

    /**
     * @param val
     *            the val to set
     */
    public void setVal(String val) {
        this.val = val;
    }

    public String toString(){
        return "[cdeName="+this.cdeName+",val="+this.val+"]";
    }
}
