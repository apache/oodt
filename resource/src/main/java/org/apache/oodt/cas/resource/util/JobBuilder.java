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


package org.apache.oodt.cas.resource.util;

//JDK imports

import org.apache.oodt.cas.resource.structs.JobSpec;
import org.apache.oodt.commons.xml.XMLUtils;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public final class JobBuilder {
    private static Logger LOG = Logger.getLogger(JobBuilder.class.getName());
    private JobBuilder() throws InstantiationException {
        throw new InstantiationException("Don't construct utility classes!");
    }

    public static JobSpec buildJobSpec(File jobFile) {
        return buildJobSpec(jobFile.getAbsolutePath());
    }

    public static JobSpec buildJobSpec(String jobFilePath) {
        Document doc;
        try {
            doc = XMLUtils.getDocumentRoot(new FileInputStream(new File(
                    jobFilePath)));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return null;
        }
        return XmlStructFactory.getJobSpec(doc.getDocumentElement());
    }

}
