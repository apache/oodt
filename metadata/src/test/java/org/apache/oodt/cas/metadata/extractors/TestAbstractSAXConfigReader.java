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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestAbstractSAXConfigReader {

    private AbstractSAXConfigReader configReader;
    private File configFile;

    private List<String> uris = new ArrayList<String>();
    private List<String> localNames = new ArrayList<String>();
    private List<String> qNames = new ArrayList<String>();
    private List<Integer> attributes = new ArrayList<Integer>();

    @Before
    public void setup() throws Exception {
        URL url = getClass().getResource("/product-type-patterns.xml");
        configFile = new File(url.toURI());
        configReader = new AbstractSAXConfigReader() {
            @Override
            public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
                uris.add(uri);
                localNames.add(localName);
                qNames.add(qName);
                attributes.add(attrs.getLength());
            }
        };
    }

    @After
    public void teardown() {
        uris.clear();
        localNames.clear();
        qNames.clear();
        attributes.clear();
    }

    @Test
    public void testParseConfigFile() throws Exception {
        configReader.parseConfigFile(configFile);

        assertEquals(5, uris.size());
        for (String uri : uris)
            assertEquals("", uri);

        assertEquals(5, localNames.size());
        for (String local : localNames)
            assertEquals("", local);

        assertEquals(5, qNames.size());
        assertEquals("config", qNames.get(0));
        assertEquals("element", qNames.get(1));
        assertEquals("element", qNames.get(2));
        assertEquals("product-type", qNames.get(3));
        assertEquals("product-type", qNames.get(4));

        assertEquals(5, attributes.size());

        assertEquals(0, (int)attributes.get(0));
        assertEquals(2, (int)attributes.get(1));
        assertEquals(2, (int)attributes.get(2));
        assertEquals(2, (int)attributes.get(3));
        assertEquals(2, (int)attributes.get(4));
    }

}
