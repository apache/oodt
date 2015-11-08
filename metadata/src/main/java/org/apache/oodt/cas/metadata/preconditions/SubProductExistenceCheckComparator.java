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

package org.apache.oodt.cas.metadata.preconditions;

//JDK imports
import org.apache.oodt.cas.metadata.exceptions.PreconditionComparatorException;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link PreConditionComparator} that checks for the existence of an arbitrary file (a "sub-product") 
 * within the product directory.
 * If the product is not a directory, or the filePath field is not specified, this class behaves
 * exactly like the superclass {@link ExistanceCheckComparator}.
 * 
 * @author luca (Luca Cinquini)
 *
 */
public class SubProductExistenceCheckComparator extends
		ExistanceCheckComparator {
	
	private static final Logger LOG = Logger.getLogger(ExistanceCheckComparator.class.getName());
	
    private String filePath;

	public String getFilePath() {
		return filePath;
	}

	/**
	 * Sets the sub-product file path (must be relative to the product absolute path).
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
    @Override
    protected int performCheck(File product, Boolean compareItem)
            throws PreconditionComparatorException {
    	
    	if (product.isDirectory() && filePath!=null) {
    		File file = new File(product.getAbsolutePath()+"/"+filePath);
    		LOG.log(Level.INFO, "Checking existence of file="+file.getAbsolutePath());
    		return Boolean.valueOf(file.exists()).compareTo(compareItem);
    	} else {
    		return Boolean.valueOf(product.exists()).compareTo(compareItem);
    	}
    }
	
}
