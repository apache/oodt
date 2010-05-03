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

package gov.nasa.jpl.oodt.cas.catalog.query;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A WrapperQueryExpression which signifies negation of wrapped QueryExpression
 * <p>
 */
public class NotQueryExpression extends WrapperQueryExpression {
	
	public NotQueryExpression() {
		super();
	}
	
	public NotQueryExpression(QueryExpression queryExpression) {
		super(queryExpression);
	}

	@Override
	public NotQueryExpression clone() {
		NotQueryExpression nqe = new NotQueryExpression(this.queryExpression.clone());
		nqe.setBucketNames(this.getBucketNames());
		return nqe;
	}
	
	@Override
	public String toString() {
		return "(NOT (" + this.queryExpression + "))";
	}

	@Override
	public boolean isValidWithNoSubExpression() {
		return false;
	}

}
