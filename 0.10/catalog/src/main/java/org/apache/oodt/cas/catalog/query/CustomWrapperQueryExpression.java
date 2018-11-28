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

/**
 * @author bfoster
 * @version $Revision$
 *
 */
public class CustomWrapperQueryExpression extends WrapperQueryExpression {

	protected String meaning;
	
	public CustomWrapperQueryExpression(String meaning) {
		this.meaning = meaning;
	}
	
	public CustomWrapperQueryExpression(String meaning, QueryExpression queryExpression) {
		super(queryExpression);
		this.meaning = meaning;
	}
	
	public String getMeaning() {
		return this.meaning;
	}
	
	@Override
	public CustomWrapperQueryExpression clone() {
		CustomWrapperQueryExpression cwqe = new CustomWrapperQueryExpression(this.meaning, this.queryExpression.clone());
		cwqe.setBucketNames(this.bucketNames);
		return cwqe;
	}

	@Override
	public String toString() {
		return "({" + this.bucketNames + "} " + this.meaning + "(" + this.queryExpression + "))";
	}

	@Override
	public boolean isValidWithNoSubExpression() {
		return false;
	}

}
