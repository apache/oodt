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

import org.apache.oodt.cas.metadata.MetExtractorConfig;
import org.apache.oodt.cas.metadata.MetExtractorConfigReader;
import org.apache.oodt.cas.metadata.exceptions.MetExtractorConfigReaderException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;

/**
 * Parses an xml config file for MetExtractors using SAX.
 *
 * @author rickdn (Ricky Nguyen)
 */
public class AbstractSAXConfigReader extends DefaultHandler implements MetExtractorConfigReader, MetExtractorConfig {

    @Override
    public AbstractSAXConfigReader parseConfigFile(File configFile) throws MetExtractorConfigReaderException {
        try {
            SAXParser p = SAXParserFactory.newInstance().newSAXParser();
            p.parse(configFile, this);
        } catch (Exception e) {
            throw new MetExtractorConfigReaderException(e.getMessage());
        }
        return this;
    }

}
