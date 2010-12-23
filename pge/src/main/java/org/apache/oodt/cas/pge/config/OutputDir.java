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

//JDK imports
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An output dir for PGE execution
 * </p>.
 */
public class OutputDir {

    private String path;

    private List<RegExprOutputFiles> regExprOutputFilesList;

    private boolean createBeforeExe;

    public OutputDir(String path, boolean createBeforeExe) {
        this.path = path;
        this.createBeforeExe = createBeforeExe;
        this.regExprOutputFilesList = new LinkedList<RegExprOutputFiles>();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public void addRegExprOutputFiles(RegExprOutputFiles regExprOutputFiles) {
        this.regExprOutputFilesList.add(regExprOutputFiles);
    }

    public List<RegExprOutputFiles> getRegExprOutputFiles() {
        return this.regExprOutputFilesList;
    }

    public void setCreateBeforeExe(boolean createBeforeExe) {
        this.createBeforeExe = createBeforeExe;
    }

    public boolean isCreateBeforeExe() {
        return this.createBeforeExe;
    }

}
