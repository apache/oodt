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


package org.apache.oodt.cas.metadata.extractors;

//OODT imports
import org.apache.oodt.cas.metadata.MetExtractorConfig;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Configuration file for the {@link ExternMetExtractor}.
 * </p>.
 */
public class ExternalMetExtractorConfig implements MetExtractorConfig,
        ExternMetExtractorMetKeys {

    private String workingDirPath;

    private String extractorBinPath;

    private String metFileExt;

    private String[] argList;

    public ExternalMetExtractorConfig() {
        this(null, null, null);
    }

    public ExternalMetExtractorConfig(String workingDirPath,
            String extractorBinPath, String[] argList) {
        this.workingDirPath = workingDirPath;
        this.extractorBinPath = extractorBinPath;
        this.argList = argList;
        this.metFileExt = DEFAULT_MET_FILE_EXTENSION;
    }

    /**
     * @return the argList
     */
    public String[] getArgList() {
        return argList;
    }

    /**
     * @param argList
     *            the argList to set
     */
    public void setArgList(String[] argList) {
        this.argList = argList;
    }

    /**
     * @return the extractorBinPath
     */
    public String getExtractorBinPath() {
        return extractorBinPath;
    }

    /**
     * @param extractorBinPath
     *            the extractorBinPath to set
     */
    public void setExtractorBinPath(String extractorBinPath) {
        this.extractorBinPath = extractorBinPath;
    }

    /**
     * @return the workingDirPath
     */
    public String getWorkingDirPath() {
        return workingDirPath;
    }

    /**
     * @param workingDirPath
     *            the workingDirPath to set
     */
    public void setWorkingDirPath(String workingDirPath) {
        this.workingDirPath = workingDirPath;
    }

    /**
     * @return the metFileExt
     */
    public String getMetFileExt() {
        return metFileExt;
    }

    /**
     * @param metFileExt
     *            the metFileExt to set
     */
    public void setMetFileExt(String metFileExt) {
        this.metFileExt = metFileExt;
    }

}
