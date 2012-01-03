/**
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
package org.apache.oodt.opendapps;

//JDK imports
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

//OODT imports
import org.apache.commons.io.FileUtils;
import org.apache.oodt.opendapps.util.ProfileSerializer;
import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.handlers.ProfileHandler;
import org.apache.oodt.xmlquery.XMLQuery;

/**
 * Command line class to drive the creation of OODT profiles from THREDDS
 * catalogs with OpenDAP endpoints.
 * <p/>
 * This class reads the list of THREDDS catalog URLs from the given opendapps
 * configuration file, parses the catalogs, and it writes the OODT profiles (one
 * for each THREDDS dataset) in the file "profiles.xml" in the specified
 * directory, or in the local execution directory if none is specified.
 * <p/>
 * Usage: java -classpath [path to opendapps-version-jar-with-dependencies.jar]
 * org.apache.oodt.opendapps.Profiler [config_file_location]
 * [optional_output_dir]
 * <p/>
 * Usage example: java -classpath
 * ./target/opendapps-0.4-SNAPSHOT-jar-with-dependencies.jar
 * org.apache.oodt.opendapps.Profiler /home/users/testuser/opendap.config.xml
 * /tmp
 * 
 * @author Luca Cinquini
 * 
 */
public class Profiler {

  private static Logger LOG = Logger.getLogger(Profiler.class.getName());

  /**
   * Command line invocation method.
   * 
   * @param args
   */
  public static void main(String[] args) throws Exception {

    // parse command line input
    if (args.length != 1 && args.length != 2) {
      usage();
    }
    File configFile = new File(args[0]);
    File outputDir = null;
    if (args.length == 2)
      outputDir = new File(args[1]);

    // parse THREDDS catalogs, create OODT profiles
    ProfileHandler profileHandler = new OpendapProfileHandler();
    XMLQuery xmlQuery = Profiler.buildXMLQuery(configFile);
    @SuppressWarnings(value = "unchecked")
    final List<Profile> profiles = profileHandler.findProfiles(xmlQuery);

    // serialize profiles to XML
    final Document doc = Profile.createProfileDocument();
    for (final Profile profile : profiles) {
      profile.toXML(doc);
    }
    String xml = ProfileSerializer.toXML(profiles);
    LOG.log(Level.INFO, xml);

    // write XML to disk
    if (outputDir != null) {
      final File file = new File(outputDir, "profiles.xml");
      FileUtils.writeStringToFile(file, xml);
    }

  }

  private static XMLQuery buildXMLQuery(final File file) throws SAXException {

    final String query = "<query><queryKWQString>"
        + "PFunction=findall?ConfigUrl=" + file.getAbsolutePath()
        + "</queryKWQString></query>";
    final XMLQuery xmlQuery = new XMLQuery(query);
    return xmlQuery;

  }

  private final static void usage() {
    System.out
        .println("Usage: java -classpath [path to opendapps-version-jar-with-dependencies.jar] org.apache.oodt.opendapps.Profiler <config file location> [<output_dir>]");
    System.out
        .println("Example: java -classpath ./target/opendapps-0.4-SNAPSHOT-jar-with-dependencies.jar org.apache.oodt.opendapps.Profiler /home/users/testuser/opendap.config.xml /tmp");
    System.exit(-1);
  }

}
