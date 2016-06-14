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


package org.apache.oodt.cas.crawl.typedetection;

// OODT imports
import org.apache.oodt.cas.metadata.MetExtractor;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

import java.util.LinkedList;
import java.util.List;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A specification for instantiating {@link MetExtractor}s
 * </p>.
 */
public class MetExtractorSpec {

    private MetExtractor metExtractor;

    private List<String> preCondComparatorIds;

    private String configFile;

    /**
     * Default Constructor.
     */
    public MetExtractorSpec() {
    }

    /**
     * Constructs a new spec with the given parameters.
     * 
     * @param className
     *            The name of the {@link MetExtractor} impl class.
     * @param configFile
     *            The name of the configuration file used to configure This
     *            {@link MetExtractor} described by this class.
     * @throws InstantiationException
     */
    public MetExtractorSpec(String className, String configFile,
            List<String> preCondComparatorIds) throws InstantiationException {
        try {
            this.setMetExtractor(className);
            this.setExtractorConfigFile(configFile);
            this.preCondComparatorIds = preCondComparatorIds;
        } catch (Exception e) {
            throw new InstantiationException(
                  "Failed to create MetExtractorSpec object : "
                            + e.getMessage());
        }
    }

    /**
     * @return the extractorClassName
     */
    public MetExtractor getMetExtractor() {
        return this.metExtractor;
    }

    /**
     * @param extractorClassName
     *            the extractorClassName to set
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws MetExtractionException
     */
    public void setMetExtractor(String extractorClassName)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, MetExtractionException {
        this.metExtractor = (MetExtractor) Class.forName(extractorClassName)
                .newInstance();
        if (this.configFile != null) {
            this.metExtractor.setConfigFile(this.configFile);
        }
    }

    /**
     * @param extractorConfigFile
     *            the extractorConfigFile to set
     * @throws MetExtractionException
     */
    public void setExtractorConfigFile(String extractorConfigFile)
            throws MetExtractionException {
        this.configFile = extractorConfigFile;
        if (this.configFile != null && this.metExtractor != null) {
            this.metExtractor.setConfigFile(this.configFile);
        }
    }

    /**
     * @return List<Preconiditions> specified in the extractor preconditions
     *         file
     */
    public List<String> getPreCondComparatorIds() {
        return this.preCondComparatorIds != null ? this.preCondComparatorIds
                : new LinkedList<String>();
    }

    /**
     * @param preCondComparatorIds
     *            The extractor preconditions file
     */
    public void setPreConditionComparatorIds(List<String> preCondComparatorIds) {
        this.preCondComparatorIds = preCondComparatorIds;
    }

}
