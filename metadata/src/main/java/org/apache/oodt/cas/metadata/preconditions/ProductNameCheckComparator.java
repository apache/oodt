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
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//OODT imports
//Spring imports

/**
 * Precondition class that checks the product name (i.e. the directory name)
 * versus the regular expression defined by the environmental variable "PNC_REGEX".
 * 
 * @author Luca Cinquini
 *
 */
public class ProductNameCheckComparator extends PreConditionComparator<Boolean> {
	
	private static final Logger LOG = Logger.getLogger(ExistanceCheckComparator.class.getName());
	
	private final static String PNC_REGEX = "PNC_REGEX";
	
	private final Pattern pattern;
	
	public ProductNameCheckComparator() {
		
		String regex = System.getenv(PNC_REGEX);
		if (!StringUtils.hasText(regex)) {
			regex = ".*"; // default regular expression matches everything
		}
		pattern = Pattern.compile(regex);
	}
	

	@Override
	protected int performCheck(File product, Boolean compareItem) throws PreconditionComparatorException {
		
		LOG.log(Level.FINEST, "Comparing product: "+product.getName()+" versus regex: "+pattern.toString());
		
		Matcher matcher = pattern.matcher(product.getName());
		if (matcher.matches()) {
			LOG.log(Level.FINEST, "Product: "+product.getName()+" passes 'regex' check: "+pattern.toString());
			return Boolean.TRUE.compareTo(compareItem);
		} else {

			LOG.log(Level.FINEST, "Product: "+product.getName()+" failed 'regex' check: "+pattern.toString());
			return Boolean.FALSE.compareTo(compareItem);
		}

	}

}
