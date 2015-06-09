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

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.pushpull.filerestrictions.Parser;
import org.apache.oodt.cas.pushpull.filerestrictions.VirtualFile;
import org.apache.oodt.cas.pushpull.filerestrictions.VirtualFileStructure;
import org.apache.oodt.cas.pushpull.exceptions.ParserException;

//Google imports
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

//JDK imports
import java.io.FileInputStream;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A generic email parser which generates file paths to be downloaded by using a defined java
 * Pattern. The pattern should specify pattern groups for file paths in the matching pattern.
 * These groups will then be extracted and added to the file structure.
 *
 * @author bfoster@apache.org (Brian Foster)
 */
public class GenericEmailParser implements Parser {

  private static final Logger log = Logger.getLogger(GenericEmailParser.class.getCanonicalName());

  public static final String FILE_PATTERNS_PROPERTY_NAME =
      "org.apache.oodt.cas.pushpull.generic.email.parser.file.pattern";
  public static final String CHECK_FOR_PATTERN_PROPERTY_NAME =
      "org.apache.oodt.cas.pushpull.generic.email.parser.check.for.pattern";
  public static final String PATH_TO_ROOT_PROPERTY_NAME =
      "org.apache.oodt.cas.pushpull.generic.email.parser.path.to.root";
  public static final String METADATA_KEYS =
      "org.apache.oodt.cas.pushpull.generic.email.parser.metadata.keys";
  public static final String METADATA_KEY_PREFIX =
      "org.apache.oodt.cas.pushpull.generic.email.parser.metadata.";      
  
  private final String filePattern;
  private final String checkForPattern;
  private final String pathToRoot;

  public GenericEmailParser() {
    filePattern = loadFilePattern();
    checkForPattern = loadCheckForPattern();
    pathToRoot = loadPathToRoot();
  }
 
  public GenericEmailParser(String filePattern, String checkForPattern, String pathToRoot) {
    this.filePattern = filePattern;
    this.checkForPattern = checkForPattern;
    this.pathToRoot = Strings.nullToEmpty(pathToRoot);
  }

  @Override
  public VirtualFileStructure parse(FileInputStream emailFile, Metadata metadata)
      throws ParserException {
    log.info("GenericEmailParser is parsing email: " + emailFile);

    VirtualFile root = VirtualFile.createRootDir();

    String emailText = readEmail(emailFile);
    if (!isValidEmail(emailText)) {
      throw new ParserException("Failed to find check for pattern in email: " + checkForPattern);
    }
    List<String> filePaths = generateFilePaths(emailText);
    readMetadata(emailText, metadata);

    for (String filePath : filePaths) {
      new VirtualFile(root, pathToRoot + filePath, false);
    }

    return new VirtualFileStructure("/", root);
  }

  private String readEmail(FileInputStream emailFile) {
    StringBuilder emailText = new StringBuilder("");
    Scanner scanner = new Scanner(emailFile);
    while (scanner.hasNextLine()) {
      emailText.append(scanner.nextLine()).append("\n");
    }
    scanner.close();
    return emailText.toString();
  }

  private List<String> generateFilePaths(String emailText) throws ParserException {
    List<String> filePaths = Lists.newArrayList();
    Pattern pattern = Pattern.compile(filePattern);
    Matcher m = pattern.matcher(emailText);
    if (m.find()) {
      // Ignore index 0, as that is the matching string for pattern.
      for (int i = 1; i <= m.groupCount(); i++) {
        filePaths.add(m.group(i));          
      }
    }
    return filePaths;
  }

  private void readMetadata(String emailText, Metadata metadata) {
    Set<String> metadataKeys = loadMetadataKeys();
    for (String metadataKey : metadataKeys) {
      String metadataPattern = loadMetadataKey(metadataKey);
      if (metadataPattern == null) {
        log.log(Level.SEVERE, "Failed to load metadata pattern for key: " + metadataKey);
      } else {
        Pattern pattern = Pattern.compile(metadataPattern);
        Matcher m = pattern.matcher(emailText);
        if (m.find()) {
          // Ignore index 0, as that is the matching string for pattern.
          String metadatValue = m.group(1);
          metadata.replaceMetadata(metadataKey, metadatValue);
        }
      }
    }
  }

  private boolean isValidEmail(String emailText) {
    Pattern pattern = Pattern.compile(checkForPattern);
    Matcher m = pattern.matcher(emailText.replaceAll("\n", " "));
    return m.find();
  }
  
  private String loadFilePattern() {
    return System.getProperty(FILE_PATTERNS_PROPERTY_NAME);
  }
  
  private String loadCheckForPattern() {
    return System.getProperty(CHECK_FOR_PATTERN_PROPERTY_NAME);    
  }
  
  private String loadPathToRoot() {
    return Strings.nullToEmpty(System.getProperty(PATH_TO_ROOT_PROPERTY_NAME));
  }

  private Set<String> loadMetadataKeys() {
    return Sets.newHashSet(Splitter.on(",").omitEmptyStrings().split(
        Strings.nullToEmpty(System.getProperty(METADATA_KEYS))));
  }

  private String loadMetadataKey(String key) {
    return System.getProperty(METADATA_KEY_PREFIX + key);
  }
}
