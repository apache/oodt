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
import java.util.List;

//Google imports
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/** 
 * Configuration file for CAS-PGE.
 *
 * @author bfoster (Brian Foster)
 */
public class PgeConfig {

    private List<DynamicConfigFile> dynamicConfigFiles;
    private List<OutputDir> outputDirs;
    private Object[] propertyAdderCustomArgs;
    private String exeDir;
    private String shellType;
    private List<String> exeCmds;
    private FileStagingInfo fileStagingInfo;

    public PgeConfig() {
        shellType = "sh";
        outputDirs = Lists.newArrayList();
        dynamicConfigFiles = Lists.newArrayList();
        exeCmds = Lists.newArrayList();
    }

    public void addDynamicConfigFile(DynamicConfigFile dynamicConfigFile) {
        dynamicConfigFiles.add(dynamicConfigFile);
    }

    public List<DynamicConfigFile> getDynamicConfigFiles() {
        return dynamicConfigFiles;
    }

    public void addOuputDirAndExpressions(OutputDir outputDir) {
        outputDirs.add(outputDir);
    }

    public List<OutputDir> getOuputDirs() {
        return outputDirs;
    }

    public void setExeDir(String exeDir) {
        this.exeDir = exeDir;
    }

    public String getExeDir() {
        return exeDir;
    }

    public void setShellType(String shellType) {
        if (!Strings.isNullOrEmpty(shellType)) {
            this.shellType = shellType;
        }
    }

    public String getShellType() {
        return shellType;
    }

    public void setExeCmds(List<String> exeCmds) {
        this.exeCmds = exeCmds;
    }

    public List<String> getExeCmds() {
        return exeCmds;
    }

    public void setPropertyAdderCustomArgs(Object[] args) {
        propertyAdderCustomArgs = args;
    }

    public Object[] getPropertyAdderCustomArgs() {
        return propertyAdderCustomArgs != null ? propertyAdderCustomArgs
                : new Object[0];
    }

    public void setFileStagingInfo(FileStagingInfo fileStagingInfo) {
       this.fileStagingInfo = fileStagingInfo;
    }

    public FileStagingInfo getFileStagingInfo() {
       return fileStagingInfo;
    }
}
