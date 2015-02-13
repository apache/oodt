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

package org.apache.oodt.cas.filemgr.structs.type.examples;

//APACHE imports
import org.apache.commons.lang.StringUtils;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.type.ValueReplaceTypeHandler;

/**
 * Handler metadata element values whose original format is: \d{1,2}.\d{0,2}
 * 
 * Return Catalog value format:  \d\d.\d\d
 * Return original value format: \d{1,2}.\d{1,2}
 * 
 * @author bfoster
 * @version $Revision$
 * 
 */
public class MajorMinorVersionTypeHandler extends ValueReplaceTypeHandler {

    
    @Override
    protected String getCatalogValue(String origValue) {
        String[] origValueSplit = origValue.split("\\.");
        String majorVersion = origValueSplit[0];
        String minorVersion = origValueSplit[1];
        return StringUtils.leftPad(majorVersion, 2, "0") + "." 
            + StringUtils.rightPad(minorVersion, 2, "0");
    }

    @Override
    protected String getOrigValue(String databaseValue) {
        return Double.toString(Double.parseDouble(databaseValue));
    }

}
