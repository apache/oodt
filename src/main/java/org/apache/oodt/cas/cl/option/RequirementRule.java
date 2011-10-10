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


package org.apache.oodt.cas.cl.option;

//OODT imports
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class RequirementRule {

    private List<String> optionValues;
    private String optionLongName;
    private boolean requireAllValues;

    public RequirementRule() {
        this.optionValues = new LinkedList<String>();
        this.requireAllValues = false;
    }

    public List<String> getOptionValues() {
        return optionValues;
    }

    public void setOptionValues(List<String> optionValues) {
        this.optionValues = optionValues;
    }

    public boolean isRequireAllValues() {
        return requireAllValues;
    }

    public void setRequireAllValues(boolean requireAllValues) {
        this.requireAllValues = requireAllValues;
    }

    public String getOptionLongName() {
        return optionLongName;
    }

    public void setOptionLongName(String optionLongName) {
        this.optionLongName = optionLongName;
    }

    public String toString() {
        return (this.requireAllValues ? "All" : "At least one")
                + " of the following values must be specified for option --"
                + this.optionLongName + " " + this.optionValues;
    }

}
