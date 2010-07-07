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


package org.apache.oodt.cas.install;

//OODT imports
import org.apache.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.io.File;
import java.io.IOException;

//APACHE imports
import org.apache.commons.io.FileUtils;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Does {@link PathUtils#replaceEnvVariables(String)} on the given
 * {@link #filepath} by reading the {@link File} into a String using
 * {@link FileUtils#readFileToString(File)} and then doing
 * {@link PathUtils#replaceEnvVariables(String)} on that String.
 * </p>.
 */
public class EnvReplacer {

    private File filepath;

    public void doEnvReplace() throws IOException {
        if (this.filepath != null && this.filepath.exists()
                && this.filepath.canWrite()) {

            String existingFileStr = FileUtils.readFileToString(this.filepath);
            String envReplacedFileStr = PathUtils
                    .replaceEnvVariables(existingFileStr);
            FileUtils.writeStringToFile(this.filepath, envReplacedFileStr);

        }

    }

    /**
     * @return the filepath
     */
    public File getFilepath() {
        return filepath;
    }

    /**
     * @param filepath
     *            the filepath to set
     */
    public void setFilepath(File filepath) {
        this.filepath = filepath;
    }
}
