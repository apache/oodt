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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;

//Apache imports
//OODT imports

/**
 * @author rverma
 * @author arni
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Met Extractor that invokes Apache Tika to automatically detect
 * relevant metadata for a given product.
 * </p>
 * .
 * <p>
 * To use this extractor, a met extractor config file must be referenced. 
 * This can take the form of a Java properties file that includes, 
 * at a minimum, the 'ProductType=...' metadata key specified.
 * </p>
 */
public class TikaCmdLineMetExtractor extends CmdLineMetExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(TikaCmdLineMetExtractor.class);

    protected static MetReaderConfigReader reader = 
            new MetReaderConfigReader();

    public TikaCmdLineMetExtractor() {
        super(reader);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.oodt.cas.metadata.AbstractMetExtractor#extractMetadata(java
     * .io.File)
     */
    @Override
    public Metadata extrMetadata(File file) throws MetExtractionException {

        try {
            org.apache.tika.metadata.Metadata tikaMet = 
                    new org.apache.tika.metadata.Metadata();
            Metadata met = new Metadata();
            InputStream is = new FileInputStream(file);

            // extract met from prod using tika
            LOG.info("Invoking tika extractor on file ["
                    + file.getAbsolutePath() + "]");
            Tika tika = new Tika();
            tika.parse(is, tikaMet); // extract metadata
            tikaMet.add("content", tika.parseToString(file)); // extract content

            LOG.info("Number of captured tika metadata keys: ["
                    + tikaMet.names().length + "]");

            // copy tika met into oodt met
            for (String key : tikaMet.names()) {
                met.addMetadata(key, StringEscapeUtils.escapeXml(tikaMet.get(key)));
                LOG.info("Added tika met key [" + key + "] with value ["
                        + met.getMetadata(key) + "]");
            }

            MetReaderConfig myConfig = (MetReaderConfig) this.config;

            // add config file met
            Enumeration<Object> configMetKeys = myConfig.keys();
            while (configMetKeys.hasMoreElements()) {
                String configMetKey = (String) configMetKeys.nextElement();
                String configMetKeyVal = (String) myConfig.get(configMetKey);

                met.addMetadata(configMetKey, StringEscapeUtils.escapeXml(configMetKeyVal));
                LOG.info("Added config file met key [" + configMetKey +
                        "] with value [" + met.getMetadata(configMetKey) + "]");
            }
            
            // add standard OODT met
            met.addMetadata("Filename", StringEscapeUtils.escapeXml(file.getName()));
            met.addMetadata("FileLocation", StringEscapeUtils.escapeXml(file.getParentFile().getAbsolutePath()));

            return met;

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new MetExtractionException(e.getMessage(), e);
        }
    }

    public static void main(String[] args) throws Exception {
        processMain(args, new TikaCmdLineMetExtractor());
    }

}
