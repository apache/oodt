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

package org.apache.oodt.cas.filemgr.repository;

//JDK imports
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.commons.io.DirectorySelector;

/**
 * @author mattmann
 * @author bfoster
 * @author luca
 * @version $Revision$
 * 
 * <p>
 * A Factory class for creating {@link XMLRepositoryManager} objects.
 * </p>
 * 
 */
public class XMLRepositoryManagerFactory implements RepositoryManagerFactory {

    /* list of dir uris specifying file paths to productType directories */
    private List<String> productTypeDirList = null;

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(XMLRepositoryManagerFactory.class.getName());

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public XMLRepositoryManagerFactory() {
    	
    	
        String productTypeDirUris = System
                .getProperty("org.apache.oodt.cas.filemgr.repositorymgr.dirs");
        
        // only returns true if org.apache.oodt.cas.filemgr.repositorymgr.dirs.recursive=true
        boolean recursive = Boolean.parseBoolean( 
        		System.getProperty("org.apache.oodt.cas.filemgr.repositorymgr.dirs.recursive") );

        if (productTypeDirUris != null) {
            productTypeDirUris = PathUtils
                    .replaceEnvVariables(productTypeDirUris);
            String[] dirUris = productTypeDirUris.split(",");

            // recursive directory listing
            if (recursive) {
            	
            	// empty list
            	productTypeDirList = new ArrayList<String>();
            	
            	// loop over specified root directories,
            	// add directories and sub-directories that contain "product-types.xml"
            	for (String rootDir : dirUris) {
            		try {
            			
            			DirectorySelector dirsel = new DirectorySelector(
            					Arrays.asList( 
            							new String[] {"product-types.xml"} ));
            			productTypeDirList.addAll( dirsel.traverseDir(new File(new URI(rootDir))) );
            			
            		} catch (URISyntaxException e) {
            			LOG.log(Level.WARNING, "URISyntaxException when traversing directory: "+rootDir);
            		}
            	}        		

        	// non-recursive directory listing
            } else {	
                productTypeDirList = Arrays.asList(dirUris);
            }
            
            LOG.log(Level.FINE,"Collecting XML policies from the following directories:");
            for (String pdir : productTypeDirList) {
            	LOG.log(Level.FINE, pdir);
            }
            
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.cas.filemgr.repository.RepositoryManagerFactory#createRepositoryManager()
     */
    public RepositoryManager createRepositoryManager() {
        if (productTypeDirList != null) {
            RepositoryManager repositoryManager = null;
            try {
                repositoryManager = new XMLRepositoryManager(productTypeDirList);
            } catch (Exception ignore) {
            }

            return repositoryManager;
        } else {
            LOG
                    .log(
                            Level.WARNING,
                            "Cannot create XML Repository Manager: no product type dir uris specified: value: "
                                    + System
                                            .getProperty("org.apache.oodt.cas.filemgr.repositorymgr.dirs"));
            return null;
        }
    }

}
