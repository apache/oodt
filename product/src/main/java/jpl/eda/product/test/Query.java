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


package jpl.eda.product.test;

import java.util.Arrays;

/**
 * A test query to run.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
class Query {
	/**
	 * Creates a new <code>Query</code> instance with an MD5 digest.
	 *
	 * @param expr Query expression.
	 * @param md5 Digest of expected result from query.
	 */
	public Query(String expr, byte[] md5) {
		this.expr = expr;
		this.md5 = md5;
	}

	/**
	 * Creates a new <code>Query</code> instance with no MD5 digest.
	 *
	 * @param expr a <code>String</code> value.
	 */
	public Query(String expr) {
		this.expr = expr;
	}

	/**
	 * Get the query expression.
	 *
	 * @return a <code>String</code> value.
	 */
	public String getExpr() {
		return expr;
	}

	/**
	 * Check if the given digest matches this query's expected MD5 digest.  If this
	 * query has no specified MD5 digest, then we assume any digest is correct.
	 *
	 * @param digest a <code>byte[]</code> value.
	 * @return a <code>boolean</code> value.
	 */
	public boolean checkDigest(byte[] digest) {
		return md5 == null? true : Arrays.equals(md5, digest);
	}	      

	/**
	 * Get the digest of the result expected from the query.
	 *
	 * @return a <code>byte[]</code> value.
	 */
	public byte[] getMD5() {
		return md5;
	}

	public int hashCode() {
		return expr.hashCode() ^ md5.hashCode();
	}

	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof Query) {
			Query rhs = (Query) obj;
			return expr.equals(rhs.expr) && Arrays.equals(md5, rhs.md5);
		}
		return false;
	}

	/** Query expression. */
	private String expr;

	/** Digest of result expected from query. */
	private byte[] md5;
}
