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

package gov.nasa.jpl.oodt.cas.filemgr.validation;

//JDk imports
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Factory class for creating XMLValidationLayer objects.
 * </p>
 * 
 */
public class XMLValidationLayerFactory implements ValidationLayerFactory {

    /*
     * list of dir uris specifying file paths to elements and product type map
     * directories
     */
    private List<String> dirList = null;

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public XMLValidationLayerFactory() {
        String dirUris = System
                .getProperty("gov.nasa.jpl.oodt.cas.filemgr.validation.dirs");

        if (dirUris != null) {
            dirUris = PathUtils.replaceEnvVariables(dirUris);
            String[] dirUriList = dirUris.split(",");
            dirList = Arrays.asList(dirUriList);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.filemgr.validation.ValidationLayerFactory#createValidationLayer()
     */
    public ValidationLayer createValidationLayer() {
        return new XMLValidationLayer(dirList);
    }

}
