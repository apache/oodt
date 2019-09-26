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
package org.apache.oodt.cas.pge;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.apache.oodt.cas.pge.util.GenericPgeObjectFactory.createPGETaskInstance;


/**
 * Runs a {@link PGETaskInstance} given {@link Metadata} and a
 * {@link WorkflowTaskConfiguration}.
 *
 * @author mattmann (Chris Mattmann)
 * @author bfoster (Brian Foster)
 */
public class PGETask {

    private static final Logger logger = LoggerFactory.getLogger(PGETask.class);

    private Metadata metadata;

    private WorkflowTaskConfiguration wftConfig;

    public PGETask(Metadata metadata, WorkflowTaskConfiguration wftConfig) {
        this.metadata = metadata;
        this.wftConfig = wftConfig;
    }

    public void run(String pgeTaskInstanceClasspath)
            throws
        WorkflowTaskInstanceException {
        PGETaskInstance pgeTaskInst = createPGETaskInstance(pgeTaskInstanceClasspath, logger);
        pgeTaskInst.run(this.metadata, this.wftConfig);
    }

    public static void main(String[] args) throws InstantiationException, IOException, WorkflowTaskInstanceException {
        String metadataFilePath = null, configPropertiesPath = null;
        String pgeTaskInstanceClasspath = null;
        String usage = "PGETask --instanceClass <PGETaskInstance> "
                + "--metadata </path/to/metadata/file> "
                + "--config </path/to/task/config/file>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--metadata")) {
                metadataFilePath = args[++i];
            } else if (args[i].equals("--config")) {
                configPropertiesPath = args[++i];
            } else if (args[i].equals("--instanceClass")) {
                pgeTaskInstanceClasspath = args[++i];
            }
        }

        if (metadataFilePath == null || configPropertiesPath == null) {
            System.err.println(usage);
            System.exit(1);
        }

        SerializableMetadata sm = new SerializableMetadata("UTF-8", false);
        sm.loadMetadataFromXmlStream(new File(metadataFilePath).toURI().toURL()
                .openStream());
        WorkflowTaskConfiguration config = new WorkflowTaskConfiguration();
        config.getProperties().load(
                new File(configPropertiesPath).toURI().toURL().openStream());

        PGETask task = new PGETask(sm, config);
        task.run(pgeTaskInstanceClasspath);
    }

}
