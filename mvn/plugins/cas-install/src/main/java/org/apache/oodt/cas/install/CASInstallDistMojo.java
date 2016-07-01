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


package org.apache.oodt.cas.install;

//APACHE imports
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

//JDK imports
import java.io.File;
import java.io.IOException;

//OODT imports
import static org.apache.oodt.cas.install.CASInstallDirMetKeys.*;

/**
 * Goal that unpackages a .tar.gz distribution of a CAS distro and installs it,
 * overriding configuration files and installing custom policy files, as
 * specified.
 * 
 * @goal install
 */
public class CASInstallDistMojo extends AbstractMojo {

    /**
     * Location of the CAS distribution to install, e.g.,
     * /path/to/cas-filemgr-{version}.tar.gz.
     * 
     * @parameter
     * @required
     */
    private File casDistributionFile;

    /**
     * Location of policy directories each containing XML-based CAS policy files
     * (e.g., <code>product-types.xml</code>,
     * <code>product-type-element-map.xml</code>, and
     * <code>elements.xml</code> for the CAS) that will be installed into the
     * {@link #casInstallationDir}/policy directory.
     * 
     * @parameter
     * 
     */
    private File[] customPolicyDirs;

    /**
     * Location of a directory containing a custom CAS configuration, e.g., a
     * filemgr.properties and/or a logging.properties file for filemgr that will
     * be copied into the {@link #casInstallationDir}/etc directory.
     * 
     * @parameter
     */
    private File customConfigurationDir;

    /**
     * The directory to which the plugin will install the CAS. Defaults to
     * /usr/local/${project.artifactId}-${project.version}, if not specified.
     * 
     * @parameter expression='/usr/local/${project.artifactId}-${project.version}'
     */
    private File casInstallationDir;

    /**
     * Location of custom jar files that you want to include in the
     * {@link #casInstallationDir}/lib directory.
     * 
     * @parameter
     */
    private File[] customLibs;

    /**
     * Files to do dynamic {@link org.apache.oodt.cas.metadata.util.PathUtils#replaceEnvVariables(String)} on.
     * 
     * @parameter
     */
    private EnvReplacer[] envVarReplaceFiles;

    /**
     * Script files to potentially overwrite and customize functionality for the
     * CAS bin scripts.
     * 
     * @parameter
     */
    private File[] customBinScripts;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (casDistributionFile == null || (!casDistributionFile
            .exists())) {
            throw new MojoExecutionException("the CAS distribution: ["
                    + casDistributionFile + "] does not exist!");
        }

        // remove cas installation libs directory and its contents if custom libs specified.
        // this is to prevent legacy jars from polluting the cas installation libs directory
        if (customLibs != null && customLibs.length > 0) {

            File libDir = null;
            
            // get the lib dir
            try {
                libDir = new File(casInstallationDir.getCanonicalPath()
                        + File.separator + LIB_DIR_NAME);

            } catch (IOException e) {
                getLog().warn(
                        "Unable to detect lib dir: IO exception: "
                                + e.getMessage());
            }

            // delete the lib dir
            if (libDir != null) {

                getLog().warn(
                		"removing pre-existing CAS libraries directory ["
                		+libDir.getAbsolutePath()+"] since custom CAS libraries have been specified");
                
                try {
					FileUtils.deleteDirectory(libDir);
				} catch (IOException e) {
	                getLog().warn(
	                        "Unable to delete lib dir ["+libDir.getAbsolutePath()+"]: "
	                                + e.getMessage());
				}    
            }         
        }
        
        getLog().info(
                "unpackaging distro: [" + casDistributionFile + "] to: ["
                        + casInstallationDir + "]");
        try {
            AntDecorator.untarFile(casDistributionFile, casInstallationDir);
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "an IO exception occured while untarring the CAS distribution: Message: "
                            + e.getMessage());
        }

        if (customPolicyDirs != null && customPolicyDirs.length > 0) {
            getLog().info(
                    "installing [" + customPolicyDirs.length
                            + "] custom policy dirs");

            // remove the default policy
            File policyDir = null;
            try {
                policyDir = new File(casInstallationDir.getCanonicalPath()
                        + File.separator + POLICY_DIR_NAME);
                AntDecorator.deleteAllFilesAndDir(policyDir);
            } catch (IOException e) {
                getLog().warn(
                        "IO exception when removing default policy from null policy dir: "
                        + "Message: " + e.getMessage());
            }

          for (File customPolicyDir : customPolicyDirs) {
            getLog().info(
                "Installing: [" + customPolicyDir + "] to: ["
                + policyDir + "]");

            if (customPolicyDir.exists()) {
              try {
                FileUtils.copyDirectoryToDirectory(customPolicyDir,
                    policyDir);
              } catch (IOException e) {
                getLog().warn(
                    "error copying custom policy dir: ["
                    + customPolicyDir
                    + "] to policy dir: [" + policyDir
                    + "]");
              }
            }

          }

        }

        if (customConfigurationDir != null && customConfigurationDir.exists()) {

            try {
                File configDir = new File(casInstallationDir.getCanonicalPath()
                        + File.separator + CONFIG_DIR_NAME);

                // remove default config
                AntDecorator.deleteAllFilesAndDir(configDir);

                configDir.mkdir();

                // install custom config
                FileUtils
                        .copyDirectory(customConfigurationDir, configDir, true);

            } catch (IOException e) {
                getLog().warn(
                        "Unable to detect configuration dir: IO exception: "
                                + e.getMessage());
            }

        }

        if (customLibs != null && customLibs.length > 0) {
            getLog().info(
                    "installing [" + customLibs.length
                            + "] custom CAS libraries");

            File libDir = null;
            // get the lib dir
            try {
                libDir = new File(casInstallationDir.getCanonicalPath()
                        + File.separator + LIB_DIR_NAME);

            } catch (IOException e) {
                getLog().warn(
                        "Unable to detect lib dir: IO exception: "
                                + e.getMessage());
            }

          for (File customLib : customLibs) {
            getLog().info(
                "installing [" + customLib + "] to "
                + libDir.getAbsolutePath() + "]");
            try {
              FileUtils.copyFileToDirectory(customLib, libDir);
            } catch (IOException e) {
              getLog().warn(
                  "IOException installing [" + customLib
                  + "] to " + libDir.getAbsolutePath()
                  + "]: Message: " + e.getMessage());
            }
          }
        }

        if (envVarReplaceFiles != null && envVarReplaceFiles.length > 0) {
            getLog().info(
                    "Replacing env vars on [" + envVarReplaceFiles.length
                            + "] files");

          for (EnvReplacer envVarReplaceFile : envVarReplaceFiles) {
            try {
              envVarReplaceFile.doEnvReplace();
            } catch (IOException e) {
              getLog().warn(
                  "IOException while doing env replacement on: ["
                  + envVarReplaceFile.getFilepath()
                  + "]: Message: " + e.getMessage());
            }
          }
        }

        if (customBinScripts != null && customBinScripts.length > 0) {
            getLog().info(
                    "installing [" + customBinScripts.length
                            + "] custom bin scripts");

            File binDir = null;

            try {
                binDir = new File(casInstallationDir.getCanonicalPath()
                        + File.separator + BIN_DIR_NAME);
            } catch (IOException e) {
                getLog().warn(
                        "Unable to detect bin dir: IO exception: Message: "
                                + e.getMessage());
            }

          for (File customBinScript : customBinScripts) {
            getLog().info(
                "installing [" + customBinScript + "] to ["
                + binDir + "]");
            try {
              FileUtils.copyFileToDirectory(customBinScript, binDir);
              // now chmod it with exec perms
              String custBinScriptFullPath = binDir + File.separator
                                             + customBinScript.getName();

              getLog().info("fixing perms on [" + custBinScriptFullPath + "]");
              AntDecorator.chmodFile(new File(custBinScriptFullPath),
                  "ugo+rx");
            } catch (IOException e) {
              getLog().warn(
                  "unable to install [" + customBinScript
                  + "] to [" + binDir
                  + "]: IO exception: Message: "
                  + e.getMessage());
            }
          }
        }

    }

}
