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

package org.apache.oodt.cas.curation.util;


import org.apache.oodt.cas.curation.structs.ExtractorConfig;
import org.apache.oodt.cas.metadata.util.PathUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
/**
 * A class to read extractor config
 * 
 * @author starchmd - cleanup only
 */
public class ExtractorConfigReader {
    public static final String EXTRACTOR_PROPS_FILENAME = "config.properties";
    /**
     * Read extractor configuration from directory
     * @param directory - top-level directory to read config from
     * @param identifier - id of this configuration (and subdirectory config is in)
     * @return extractor config
     * @throws FileNotFoundException - error when file is not found
     * @throws IOException - io exception
     */
    public static ExtractorConfig readFromDirectory(File directory,String identifier) throws FileNotFoundException, IOException {
        File propsFileDir = new File(directory, identifier);
        Properties props = new Properties();
        props.load(new FileInputStream(new File(propsFileDir,EXTRACTOR_PROPS_FILENAME)));

        List<File> files = new ArrayList<File>();
        String[] fileList = props.getProperty(ExtractorConfig.PROP_CONFIG_FILES).split(",");
        for (int i = 0; i < fileList.length; i++) {
            files.add(new File(PathUtils.replaceEnvVariables(fileList[i])));
        }
        return new ExtractorConfig(identifier, props.getProperty(ExtractorConfig.PROP_CLASS_NAME),files,ExtractorConfig.PROP_FILLER);
    }
}
