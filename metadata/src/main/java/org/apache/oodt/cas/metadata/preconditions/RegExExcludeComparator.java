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

package org.apache.oodt.cas.metadata.preconditions;

//JDK imports
import java.io.File;
import java.util.regex.Pattern;

//OODT imports
import org.apache.oodt.cas.metadata.exceptions.PreconditionComparatorException;
import org.apache.oodt.cas.metadata.preconditions.PreConditionComparator;

/**
 * 
 * A {@link PreConditionComparator} that checks a file's absolute path and then
 * skips if it matches with the Regular Expression provided.
 * 
 * @author karanjeets
 * @version 1.0
 * 
 */
public class RegExExcludeComparator extends PreConditionComparator<String> {
	
	protected int performCheck(File file, String compareItem)
			throws PreconditionComparatorException {
		if (compareItem != null
				&& !compareItem.trim().equals("")
				&& Pattern.matches(compareItem.toLowerCase(), file
						.getAbsolutePath().toLowerCase())) {
		  return 0;
		}
		return 1;
  }

}