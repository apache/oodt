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


package org.apache.oodt.cas.metadata;

//JDK imports
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

//JUnit imports
import junit.framework.TestCase;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Test class for SerializableMetadata
 * </p>.
 */
public class TestSerializableMetadata extends TestCase {

    private String[] encodings = new String[] { "UTF-8", "iso-8859-1",
            "windows-1252", "UTF-16", "US-ASCII" };

    public TestSerializableMetadata() {
        super();
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testSerialization() throws Exception {
        for (int i = 0; i < encodings.length; i++) {
            String encoding = encodings[i];
            boolean useCDATA = false;
            for (int j = 0; j < 2; j++, useCDATA = true) {
                SerializableMetadata sm = new SerializableMetadata(encoding,
                        useCDATA);
                sm.addMetadata("key1", "val1");
                sm.addMetadata("key2", "val2");

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(out);
                oos.writeObject(sm);
                oos.flush();

                ObjectInputStream ois = new ObjectInputStream(
                        new ByteArrayInputStream(out.toByteArray()));
                SerializableMetadata sm2 = (SerializableMetadata) ois
                        .readObject();
                oos.close();
                ois.close();

                assertNotNull(sm2);
                assertNotNull(sm2.getHashtable());
                assertEquals(2, sm2.getHashtable().size());
                assertNotNull(sm2.getMetadata("key1"));
                assertEquals("val1", sm2.getMetadata("key1"));
                assertNotNull(sm2.getMetadata("key2"));
                assertEquals("val2", sm2.getMetadata("key2"));
                assertNotNull(sm2.getEncoding());
                assertEquals(encoding, sm2.getEncoding());
                assertEquals(useCDATA, sm2.isUsingCDATA());
            }
        }
    }

    public void testXmlStreaming() throws Exception {
        for (int i = 0; i < encodings.length; i++) {
            String encoding = encodings[i];
            boolean useCDATA = false;
            for (int j = 0; j < 2; j++, useCDATA = true) {
                SerializableMetadata metadata1 = new SerializableMetadata(
                        encoding, useCDATA);
                metadata1.addMetadata("Name1", "Value1");
                metadata1.addMetadata("Name2", "Value/2");

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                metadata1.writeMetadataToXmlStream(out);

                SerializableMetadata metadata2 = new SerializableMetadata(
                        encoding, useCDATA);
                metadata2.loadMetadataFromXmlStream(new ByteArrayInputStream(
                        out.toByteArray()));
                out.close();

                assertNotNull(metadata2);
                assertNotNull(metadata2.getHashtable());
                assertEquals(2, metadata2.getHashtable().size());
                assertNotNull(metadata2.getMetadata("Name1"));
                assertEquals("Value1", metadata2.getMetadata("Name1"));
                assertNotNull(metadata2.getMetadata("Name2"));
                assertEquals("Value/2", metadata2.getMetadata("Name2"));
            }
        }
    }

    public void testMetadataConversion() throws Exception {
        for (int i = 0; i < encodings.length; i++) {
            String encoding = encodings[i];
            Metadata m = new Metadata();
            m.addMetadata("key1", "val1");
            m.addMetadata("key2", "val2");

            SerializableMetadata sm = new SerializableMetadata(m, encoding,
                    false);
            Metadata mConv = sm.getMetadata();

            assertNotNull(mConv);
            assertNotNull(mConv.getHashtable());
            assertEquals(2, mConv.getHashtable().size());
            assertNotNull(mConv.getMetadata("key1"));
            assertEquals("val1", mConv.getMetadata("key1"));
            assertNotNull(mConv.getMetadata("key2"));
            assertEquals("val2", mConv.getMetadata("key2"));
        }
    }
}
