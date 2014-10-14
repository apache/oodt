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

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.metadata.Metadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class TestProdTypePatternMetExtractor {

    private ProdTypePatternMetExtractor extractor;
    private File configFile;
    private File configFile2;

    private File tmpDir;
    private File book1;
    private File book2;
    private File page1;
    private File page2;
    private File page1a;
    private File page2a;

    @Before
    public void setup() throws Exception {
        URL url = getClass().getResource("/product-type-patterns.xml");
        configFile = new File(url.toURI());
        extractor = new ProdTypePatternMetExtractor();
        extractor.setConfigFile(configFile);

        tmpDir = Files.createTempDir();
        book1 = new File(tmpDir, "book-1234567890.txt");
        book2 = new File(tmpDir, "book-0987654321.txt");
        page1 = new File(tmpDir, "page-1234567890-111.txt");
        page2 = new File(tmpDir, "page-0987654321-222.txt");
        Files.touch(book1);
        Files.touch(book2);
        Files.touch(page1);
        Files.touch(page2);

        url = getClass().getResource("/product-type-patterns-2.xml");
        configFile2 = new File(url.toURI());
        page1a = new File(tmpDir, "page-111-1234567890.txt");
        page2a = new File(tmpDir, "page-222-0987654321.txt");
        Files.touch(page1a);
        Files.touch(page2a);
    }

    @After
    public void teardown() {
        FileUtils.deleteQuietly(tmpDir);
    }

    @Test
    public void testExtractMetadata() throws Exception {
        Metadata met = extractor.extractMetadata(book1);
        assertEquals(2, met.getAllKeys().size());
        assertEquals("Book", met.getMetadata("ProductType"));
        assertEquals("1234567890", met.getMetadata("ISBN"));

        met = extractor.extractMetadata(book2);
        assertEquals(2, met.getAllKeys().size());
        assertEquals("Book", met.getMetadata("ProductType"));
        assertEquals("0987654321", met.getMetadata("ISBN"));

        met = extractor.extractMetadata(page1);
        assertEquals(3, met.getAllKeys().size());
        assertEquals("BookPage", met.getMetadata("ProductType"));
        assertEquals("1234567890", met.getMetadata("ISBN"));
        assertEquals("111", met.getMetadata("Page"));

        met = extractor.extractMetadata(page2);
        assertEquals(3, met.getAllKeys().size());
        assertEquals("BookPage", met.getMetadata("ProductType"));
        assertEquals("0987654321", met.getMetadata("ISBN"));
        assertEquals("222", met.getMetadata("Page"));
    }

    @Test
    public void testNewConfigFile() throws Exception {
        // make sure that duplicate met entries do not exist when re-parsing a new config file
        Metadata met = extractor.extractMetadata(book1, configFile);
        assertEquals(2, met.getAllKeys().size());
        assertEquals(1, met.getAllMetadata("ProductType").size());
        assertEquals(1, met.getAllMetadata("ISBN").size());

        met = extractor.extractMetadata(book2, configFile);
        assertEquals(2, met.getAllKeys().size());
        assertEquals(1, met.getAllMetadata("ProductType").size());
        assertEquals(1, met.getAllMetadata("ISBN").size());

        met = extractor.extractMetadata(page1, configFile);
        assertEquals(3, met.getAllKeys().size());
        assertEquals(1, met.getAllMetadata("ProductType").size());
        assertEquals(1, met.getAllMetadata("ISBN").size());
        assertEquals(1, met.getAllMetadata("Page").size());

        met = extractor.extractMetadata(page2, configFile);
        assertEquals(3, met.getAllKeys().size());
        assertEquals(1, met.getAllMetadata("ProductType").size());
        assertEquals(1, met.getAllMetadata("ISBN").size());
        assertEquals(1, met.getAllMetadata("Page").size());
    }

    @Test
    public void testElementDeclarationOrder() throws Exception {
        // the relative order of element declarations shouldn't matter
        extractor.setConfigFile(configFile2);

        Metadata met = extractor.extractMetadata(book1);
        assertEquals(2, met.getAllKeys().size());
        assertEquals("Book", met.getMetadata("ProductType"));
        assertEquals("1234567890", met.getMetadata("ISBN"));

        met = extractor.extractMetadata(book2);
        assertEquals(2, met.getAllKeys().size());
        assertEquals("Book", met.getMetadata("ProductType"));
        assertEquals("0987654321", met.getMetadata("ISBN"));

        met = extractor.extractMetadata(page1a);
        assertEquals(3, met.getAllKeys().size());
        assertEquals("BookPage", met.getMetadata("ProductType"));
        assertEquals("1234567890", met.getMetadata("ISBN"));
        assertEquals("111", met.getMetadata("Page"));

        met = extractor.extractMetadata(page2a);
        assertEquals(3, met.getAllKeys().size());
        assertEquals("BookPage", met.getMetadata("ProductType"));
        assertEquals("0987654321", met.getMetadata("ISBN"));
        assertEquals("222", met.getMetadata("Page"));
    }
}
