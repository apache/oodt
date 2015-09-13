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
package org.apache.oodt.cas.catalog.query;

//JDK imports
import java.util.Set;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Standard QueryExpression
 * <p>
 */
public class StdQueryExpression extends QueryExpression {

	public StdQueryExpression() {
		super();
	}
	
	public StdQueryExpression(Set<String> bucketNames) {
		super(bucketNames);
	}
	
	@Override
	public StdQueryExpression clone() {
		return new StdQueryExpression(this.getBucketNames());
	}

	@Override
	public String toString() {
		return "({" + this.bucketNames + "})";
	}

}
