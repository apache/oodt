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

package org.apache.oodt.cas.filemgr.structs;

//JDK imports
import java.io.Serializable;
import java.util.Properties;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A specification class showing how to constract
 * a {@link FilemgrMetExtractor}.
 * </p>.
 */
public class ExtractorSpec implements Serializable {

    private String className;

    private Properties configuration;

    public ExtractorSpec() {

    }

    /**
     * @param className
     * @param configuration
     */
    public ExtractorSpec(String className, Properties configuration) {
        super();
        this.className = className;
        this.configuration = configuration;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className
     *            the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the configuration
     */
    public Properties getConfiguration() {
        return configuration;
    }

    /**
     * @param configuration
     *            the configuration to set
     */
    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }

}
