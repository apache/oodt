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
package org.apache.oodt.cas.pushpull.filerestrictions.parsers;

// JUnit static imports
import static org.junit.Assert.assertThat;


// JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;


import org.apache.oodt.cas.metadata.Metadata;
// OODT imports
import org.apache.oodt.cas.pushpull.exceptions.ParserException;
import org.apache.oodt.cas.pushpull.filerestrictions.FileRestrictions;
import org.apache.oodt.cas.pushpull.filerestrictions.VirtualFileStructure;

// JUnit imports
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test class for {@link GenericEmailParser}.
 *
 * @author bfoster@apache.org (Brian Foster)
 */
@RunWith(JUnit4.class)
public class GenericEmailParserTest {

  private File emailFile;

  @Before
  public void setUp() throws URISyntaxException {
    emailFile = new File(ClassLoader.getSystemResource("TestEmail.txt").toURI());
  }

  @Test
  public void testGenericEmailParser() throws ParserException, FileNotFoundException {
    GenericEmailParser parser = new GenericEmailParser(
        "Wav File: ([^\\s]+)", "Dear Lousy Customer,", null);
    VirtualFileStructure vfs = parser.parse(new FileInputStream(emailFile), new Metadata());
    List<String> filePaths = FileRestrictions.toStringList(vfs.getRootVirtualFile());
    assertThat(filePaths.size(), Matchers.is(1));
    assertThat(filePaths.get(0), Matchers.is("/some/path/to/a/wav/file.wav"));
  }

  @Test (expected = ParserException.class)
  public void testFailedValidEmailCheck() throws ParserException, FileNotFoundException {
    GenericEmailParser parser = new GenericEmailParser(
        "Wav File: ([^\\s]+)", "Phrase Not Found", null);
    parser.parse(new FileInputStream(emailFile), new Metadata());
  }
}
