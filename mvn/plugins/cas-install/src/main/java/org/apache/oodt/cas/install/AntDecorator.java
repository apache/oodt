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

//JDK imports
import java.io.File;
import java.io.IOException;

//ANT imports
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Chmod;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Untar;
import org.apache.tools.ant.taskdefs.Untar.UntarCompressionMethod;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A decorator wrapping <a href="http://ant.apache.org/">Apache Ant</a> core
 * tasks
 * </p>.
 */
public final class AntDecorator {
    
    public static void chmodFile(File file, String perms){
        Chmoder chmod = new Chmoder();
        chmod.setFile(file);
        chmod.setPerm(perms);
        chmod.execute();
    }

    public static void deleteAllFilesAndDir(File startDir) {
        Deleter delete = new Deleter();
        delete.setDir(startDir);
        delete.setIncludeEmptyDirs(true);
        delete.setVerbose(true);
        delete.setFollowSymlinks(true);
        delete.execute();
    }

    public static void untarFile(File tarFile, File destDir) throws IOException {
        Untarer untar = new Untarer();
        Untar.UntarCompressionMethod compMethod = new UntarCompressionMethod();
        compMethod.setValue("gzip");
        untar.setCompression(compMethod);
        untar.setDest(destDir);
        untar.setSrc(tarFile);
        untar.execute();

        // now that the work is done, use file utils to
        // move everything out of destDir/tarFileNoExt
        // into destDir
        // then delete destDir/tarFileNoExt
        String wrongInstallDir = destDir.getCanonicalPath() + File.separator
                + getFileNameNoExt(tarFile.getName());
        FileUtils.copyDirectory(new File(wrongInstallDir), destDir, true);

        // grrr java IO, love it
        // because it sucks, we have to CHMOD everything in destDir/bin
        File binDir = new File(destDir.getCanonicalPath() + File.separator
                + "bin");
        Chmoder chmod = new Chmoder();
        chmod.setDir(binDir);
        chmod.setPerm("ugo+rx");
        chmod.setIncludes("*");
        chmod.execute();

        deleteAllFilesAndDir(new File(wrongInstallDir));

    }

    final static class Untarer extends Untar {
        public Untarer() {
            project = new Project();
            project.init();
            taskType = "untar";
            taskName = "untar";
            target = new Target();
        }

    }

    final static class Deleter extends Delete {
        public Deleter() {
            project = new Project();
            project.init();
            taskType = "delete";
            taskName = "delete";
            target = new Target();
        }

    }

    final static class Chmoder extends Chmod {
        public Chmoder() {
            project = new Project();
            project.init();
            taskType = "chmod";
            taskName = "chmod";
            target = new Target();
        }
    }

    private static String getFileNameNoExt(String filename) {
        return filename.substring(0, filename.indexOf(".tar"));
    }

}
