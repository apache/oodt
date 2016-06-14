// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.
package org.apache.oodt.commons.io;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that traverses a directory tree 
 * and selects those directories that contain ALL of the requested files.
 * If no requested files are specified, an empty list will be returned.
 * 
 * @author Luca Cinquini
 */
public class DirectorySelector {
	
	private List<String> files;
	
	private FileFilter directoryFilter;
	
	/**
	 * Creates a new directory selector for the specified files
	 * 
	 * @param files the list of files that this class will look for
	 */
	public DirectorySelector(List<String> files) {
		
		// list of requested files
		this.files = files;
		
		// File filter that selects directories
		this.directoryFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		
	}
	
    /**
     * Looks for files in all sub-directories starting from rootDir.
     * 
     * @param rootDir starting root directory
     * @return list of matching sub-directories as 'file:///path/to/dir' URIs
     */
	public List<String> traverseDir(File rootDir) {
				
		List<String> subDirs = new ArrayList<String>();
		
		if (rootDir.exists() && files!=null && files.size()>0) {
			this.traverseDir(rootDir, subDirs);
		}
		
		return subDirs;
		
	}
	
	/**
	 * Internal recursion method.
	 * 
	 * @param dir
	 * @param subDirs
	 */
    private void traverseDir(File dir, List<String> subDirs) {
    	
    	// loop over required files,
    	// include only if all files are found
		boolean include = true;
     	for (String file : files) {
     		File requiredFile = new File(dir, file);
     		if (!requiredFile.exists()) {
     			include = false;
     		}
     	}
     	
     	// include this directory
     	if (include) {
    		subDirs.add("file://"+dir.getAbsolutePath());
    	}
    	
    	// recursion over sub-directories
    	File[] subdirs = dir.listFiles( directoryFilter );
	  if(subdirs!=null) {
		for (File subdir : subdirs) {
		  traverseDir(subdir, subDirs);
		}
	  }
    	
    }

}
