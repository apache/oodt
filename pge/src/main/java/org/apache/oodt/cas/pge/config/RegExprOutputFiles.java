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


package org.apache.oodt.cas.pge.config;

import org.apache.oodt.cas.metadata.filenaming.PathUtilsNamingConvention;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A regular expression class to define what OutputFiles
 * to identify after running a PGE
 * </p>.
 */
public class RegExprOutputFiles {

    private String regExp;

    private String converterClass;

    private Object[] args;

    private PathUtilsNamingConvention renamingConv;
    
    public RegExprOutputFiles(String regExp, String converterClass, 
    		PathUtilsNamingConvention renamingConv, Object[] args) {
        this.regExp = regExp;
        this.converterClass = converterClass;
        this.renamingConv = renamingConv;
        this.args = args;
    }
    
    public PathUtilsNamingConvention getRenamingConv() {
    	return this.renamingConv;
    }

    public String getRegExp() {
        return this.regExp;
    }

    public String getConverterClass() {
        return this.converterClass;
    }

    public Object[] getArgs() {
        return this.args;
    }

}
