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

//OODT imports
import org.apache.commons.io.FileUtils;
import org.apache.oodt.opendapps.util.ProfileChecker;
import org.apache.oodt.opendapps.util.ProfileSerializer;
import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.handlers.ProfileHandler;
import org.apache.oodt.xmlquery.XMLQuery;
import org.xml.sax.SAXException;

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
   * Optional directory to serialize the profiles to.
   */
  private File outputDir;

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
    Profiler profiler = new Profiler();
    if (args.length == 2) {
      profiler.setOutputDir( new File(args[1]) );
    }
    
    // run profiler
    profiler.makeProfiles(configFile);
    
  }
  
  /**
   * No argument constructor.
   */
  public Profiler() {}
  
  /**
   * Setter method for output directory.
   * @param outputDir
   */
  public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	/**
   * Method to generate OODT profiles according to the specifications contained in a configuration file.
   * 
   * @param configFile 
   * @return
   */
  public List<Profile> makeProfiles(final File configFile) throws Exception {

    // parse THREDDS catalogs, create OODT profiles
    ProfileHandler profileHandler = new OpendapProfileHandler();
    XMLQuery xmlQuery = Profiler.buildXMLQuery(configFile);
    @SuppressWarnings(value = "unchecked")
    final List<Profile> profiles = profileHandler.findProfiles(xmlQuery);
    
    // check profiles
    for (final Profile profile : profiles) {
    	final StringBuilder sb = new StringBuilder();
    	boolean ok = ProfileChecker.check(profile, sb);
    	// print out the profile summary for quick review by the publisher
    	System.out.println(sb.toString());
    	if (!ok) {
    		LOG.log(Level.SEVERE, "ERROR: invalid profile:"+profile.getResourceAttributes().getIdentifier());
    	} 	
    }

    // serialize profiles to XML
    String xml = ProfileSerializer.toXML(profiles);
    LOG.log(Level.FINE, xml);

    // write XML to disk
    if (outputDir != null) {
      final File file = new File(outputDir, "profiles.xml");
      FileUtils.writeStringToFile(file, xml);
    }

    return profiles;
    
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
