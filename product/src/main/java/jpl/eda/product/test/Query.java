// Copyright 2002-2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Query.java,v 1.1 2005-08-03 16:59:18 kelly Exp $

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
