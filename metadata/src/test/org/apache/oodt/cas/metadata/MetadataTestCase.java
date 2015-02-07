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

// JDK Input/output
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

// Junit Testing framework
import junit.framework.TestCase;

/**
 * Base test case for metadata tests.  Provides access to test data files.
 *
 * @author Kelly
 */
public class MetadataTestCase extends TestCase {
    /**
     * Construct a metadata test case.
     *
     * @param name Case name.
     */
    public MetadataTestCase(String name) {
        super(name);
    }

    /**
     * Augment set up of a test case by creating a play directory where we can temporarily
     * keep our test data files.
     *
     * @throws Exception If any errors occur in directory setup, or if superclass setUp throws it.
     */
    public void setUp() throws Exception {
        super.setUp();                                                                      // Set up the framework test harness
        tmpDir = File.createTempFile("metadata", ".tests");                                 // Get a temporary file
        if (!tmpDir.delete())                                                               // File?! We don't want no stinkin' file
            throw new IOException("Cannot delete temporary file " + tmpDir);                
        if (!tmpDir.mkdirs())                                                               // Directory is what we want
            throw new IOException("Cannot create temporary directory " + tmpDir);
        //tmpDir.deleteOnExit();
    }

    /**
     * Augment tear down of a test case by cleaning up our play directory.
     *
     * @throws Exception if any errors occur in directory deletion, or if superclass tearDown throws it.
     */
    public void tearDown() throws Exception {
        String[] entries = tmpDir.list();                                                   // Get contents of our play area
        for (int i = 0; i < entries.length; ++i) {                                          // Step through each one
            File entry = new File(tmpDir, entries[i]);                                      // Make a file out of it
            if (!entry.delete())                                                            // Nuke it if possible ...
                throw new IOException("Cannot delete temporary file " + entry);             // Or if not ...
        }
        if (!tmpDir.delete())                                                               // Nuke the play area
            throw new IOException("Cannot delete temporary directory " + tmpDir);           // Or if not ...
        super.tearDown();                                                                   // Tear down the test harness
    }
    
    /**
     * Get a named test data file.  This will yield a test data file using the standard Java resource mechanism
     * (ie, fetching out of a jar, from the class path, etc.) and stick it in a temporary file, since the
     * metadata API works with files it can both name and use, not just streams of file data.
     *
     * @param name Name of the test data file to retrieve.
     * @return A {@link java.io.File} containing the named test dat.
     * @throws IOException If an I/O error occurs.
     */
    public File getTestDataFile(String name) throws IOException {
        InputStream in = MetadataTestCase.class.getResourceAsStream(name);                  // Not found? Try resource stream
        if (in == null)                                                                     // Still not found?  Bummer.
            throw new IllegalArgumentException("Unknown test data file `" + name + "`; not found in resource path");
        File fn = new File(tmpDir, name);                                                   // What the tests want: Files.
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fn));      // Copy data to it
        in = new BufferedInputStream(in);                                                   // Buffer for efficiency
        byte[] buf = new byte[512];                                                         // Classic disk page size
        for (;;) {                                                                          // For ever
            int c = in.read(buf);                                                           // Read into our buffer
            if (c == -1) break;                                                             // EOF? Done.
            out.write(buf, 0, c);                                                           // Not EOF? Copy out.
        }
        in.close();
        out.close();
        return fn;
    }

    /** Play area */
    private File tmpDir;
}
