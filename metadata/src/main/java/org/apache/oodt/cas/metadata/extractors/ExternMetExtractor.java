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

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

//APACHE imports
import org.apache.commons.lang.StringUtils;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.extractors.CmdLineMetExtractor;
import org.apache.oodt.commons.exec.ExecUtils;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Runs an external {@link MetExtractor} that is a command line program to
 * extract {@link Metadata} from {@link Product} files.
 * </p>.
 */
public class ExternMetExtractor extends CmdLineMetExtractor implements
        ExternMetExtractorMetKeys {

    private static ExternConfigReader reader = new ExternConfigReader();

    public ExternMetExtractor() {
        super(reader);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.metadata.AbstractMetExtractor#extractMetadata(java.io.File)
     */
    public Metadata extrMetadata(File file) throws MetExtractionException {

        // determine working directory
        String workingDirPath = ((ExternalMetExtractorConfig) this.config)
                .getWorkingDirPath();
        if (workingDirPath == null || workingDirPath.equals("")) {
          workingDirPath = file.getParentFile().getAbsolutePath();
        }
        File workingDir = new File(workingDirPath);

        // determine met file path
        String metFileName = file.getName() + "."
                + ((ExternalMetExtractorConfig) this.config).getMetFileExt();
        String metFilePath = workingDir.getAbsolutePath() + "/" + metFileName;
        File metFile = new File(metFilePath);

        // get exe args
        List commandLineList = new Vector();
        commandLineList.add(((ExternalMetExtractorConfig) this.config)
                .getExtractorBinPath());
        if (((ExternalMetExtractorConfig) this.config).getArgList() != null
                && ((ExternalMetExtractorConfig) this.config).getArgList().length > 0) {
          commandLineList.addAll(Arrays
              .asList(((ExternalMetExtractorConfig) this.config)
                  .getArgList()));
        }
        String[] commandLineArgs = new String[commandLineList.size()];
        for (int i = 0; i < commandLineList.size(); i++) {
          commandLineArgs[i] = StringUtils.replace(StringUtils.replace(
              (String) commandLineList.get(i), MET_FILE_PLACE_HOLDER,
              metFilePath), DATA_FILE_PLACE_HOLDER, file
              .getAbsolutePath());
        }

        // generate metadata file
        LOG.info("Generating met file for product file: ["
                + file.getAbsolutePath() + "]");
        int status;
        try {
            LOG.info("Executing command line: ["
                    + ExecUtils.printCommandLine(commandLineArgs)
                    + "] with workingDir: [" + workingDir
                    + "] to extract metadata");
            status = ExecUtils.callProgram(commandLineArgs, workingDir);
        } catch (IOException e) {
            LOG.warn("IOException running met extraction in commandLine [{}]: {}",
                    ExecUtils.printCommandLine(commandLineArgs), e.getMessage(), e);
            return null;
        }

        if (status != 0) {
            throw new MetExtractionException(
                    "Exit status for met extraction not 0");
        } else if (!metFile.exists()) {
            throw new MetExtractionException(
                    "Met extractor failed to create metadata file");
        } else {
            LOG.info("Met extraction successful for product file: ["
                    + file.getAbsolutePath() + "]");
            try {
                SerializableMetadata sm = new SerializableMetadata("UTF-8",
                        false);
                sm.loadMetadataFromXmlStream(new FileInputStream(metFile));
                return sm;
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                throw new MetExtractionException(e.getMessage(), e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        processMain(args, new ExternMetExtractor());
    }

}
