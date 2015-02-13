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

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A dynamic representation of a SciPgeConfigFile
 * </p>.
 */
public class DynamicConfigFile {

    private String filePath, writerClass;

    private Object[] args;

    public DynamicConfigFile(String filePath, String writerClass, Object[] args) {
        this.filePath = filePath;
        this.writerClass = writerClass;
        this.args = args;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setWriterClass(String writerClass) {
        this.writerClass = writerClass;
    }

    public String getWriterClass() {
        return this.writerClass;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object[] getArgs() {
        return this.args;
    }

}
