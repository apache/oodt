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

package org.apache.oodt.xmlps.mapping.funcs;

//JDK imports
import java.util.Properties;

//OODT imports
import org.apache.oodt.xmlps.structs.CDEValue;

/**
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class ReplaceFunc implements MappingFunc {

    private String orig;

    private String with;

    public ReplaceFunc() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.xmlps.mapping.funcs.MappingFunc#translate(org.apache.oodt.xmlps.structs.CDEValue)
     */
    public CDEValue translate(CDEValue orig) {
        CDEValue cdeVal = new CDEValue(orig.getCdeName(), orig.getVal());
        String newVal = orig.getVal().equals(this.orig) ? with : orig.getVal();
        cdeVal.setVal(newVal);
        return cdeVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.xmlps.mapping.funcs.MappingFunc#configure(java.util.Properties)
     */
    public void configure(Properties props) {
        orig = props.getProperty("orig");
        with = props.getProperty("with");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.xmlps.mapping.funcs.MappingFunc#inverseTranslate(org.apache.oodt.xmlps.structs.CDEValue)
     */
    public CDEValue inverseTranslate(CDEValue orig) {
        CDEValue cdeVal = new CDEValue(orig.getCdeName(), orig.getVal());
        String newVal = orig.getVal().equals(this.with) ? this.orig : orig
                .getVal();
        cdeVal.setVal(newVal);
        return cdeVal;
    }

}
