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


package org.apache.oodt.cas.crawl.util;

//JDK imports
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class CasPropertyList extends LinkedList<String> {

    private static final long serialVersionUID = -5956500397967590724L;

    public void setValues(String values) {
        this.addAll(this.getAsList(values));
    }

    private List<String> getAsList(String values) {
        Vector<String> propList = new Vector<String>();
        StringTokenizer st = new StringTokenizer(values, ",");
        while (st.hasMoreTokens()) {
            propList.add(st.nextToken().trim());
        }
        return propList;
    }

}
