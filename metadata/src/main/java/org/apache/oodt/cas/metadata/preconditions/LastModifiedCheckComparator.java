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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * Precondition class that checks on the last modification time of a product, 
 * requiring the product to be no older than a given time (in seconds).
 * 
 * @author Luca Cinquini
 *
 */
public class LastModifiedCheckComparator extends PreConditionComparator<Boolean> {
	
	private static final Logger LOG = Logger.getLogger(ExistanceCheckComparator.class.getName());
	
	private long maxAgeInSeconds = Long.MAX_VALUE;

	public void setMaxAgeInSeconds(long maxAgeInSeconds) {
		this.maxAgeInSeconds = maxAgeInSeconds;
	}
	

	@Override
	protected int performCheck(File product, Boolean compareItem) throws PreconditionComparatorException {
		
		// check product last modification time
		long now = System.currentTimeMillis();
		long lastModTime = product.lastModified();
		long deltaInSecs = (now-lastModTime)/1000;
		
		// reject this product
		if (deltaInSecs>maxAgeInSeconds) {
			LOG.log(Level.FINEST, "Product: "+product.getAbsolutePath()+" fails 'Last Modified' check: "+new Date(lastModTime));
			return Boolean.FALSE.compareTo(compareItem);
			
		// accept this product
		} else {
			LOG.log(Level.FINEST, "Product: "+product.getAbsolutePath()+" passes 'Last Modified' check: "+new Date(lastModTime));
			return Boolean.TRUE.compareTo(compareItem);

		}
		
	}

}
