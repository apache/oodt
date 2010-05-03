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
 * A TermQueryExpression which allows Term comparison
 * <p>
 */
public class ComparisonQueryExpression extends TermQueryExpression {

	public static enum Operator { EQUAL_TO("=="), LESS_THAN_EQUAL_TO("<="), GREATER_THAN_EQUAL_TO(">="), LESS_THAN("<"), GREATER_THAN(">"), LIKE("LIKE"); 
	
		private String value;
		
		Operator(String value) {
			this.value = value;
		}
		
		public static Operator getOperatorBySign(String sign) {
			if (EQUAL_TO.value.equals(sign))
				return EQUAL_TO;
			else if (LESS_THAN_EQUAL_TO.value.equals(sign))
				return LESS_THAN_EQUAL_TO;
			else if (GREATER_THAN_EQUAL_TO.value.equals(sign))
				return GREATER_THAN_EQUAL_TO;
			else if (LESS_THAN.value.equals(sign))
				return LESS_THAN;
			else if (GREATER_THAN.value.equals(sign))
				return GREATER_THAN;
			else if (LIKE.value.equals(sign))
				return LIKE;
			else
				throw new IllegalArgumentException("Not matching operator for '" + sign + "'");
		}
		
		public String toString() {
			return this.value;
		}
	
	}
	protected Operator operator;
	
	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	
	public Operator getOperator() {
		return this.operator;
	}

	@Override
	public String toString() {
		return "({" + this.bucketNames + "} " + this.getTerm().getName() + " " + this.operator + " " + this.getTerm().getValues() + ")";
	}

	@Override
	public ComparisonQueryExpression clone() {
		ComparisonQueryExpression newQE = new ComparisonQueryExpression();
		newQE.operator = this.operator;
		newQE.setTerm(this.term.clone());
		newQE.setBucketNames(this.getBucketNames());
		return newQE;
	}

}
