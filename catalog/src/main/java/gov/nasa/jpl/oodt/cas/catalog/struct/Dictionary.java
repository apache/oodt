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

package gov.nasa.jpl.oodt.cas.catalog.struct;

//OODT imports
import gov.nasa.jpl.oodt.cas.catalog.exception.CatalogDictionaryException;
import gov.nasa.jpl.oodt.cas.catalog.query.QueryExpression;
import gov.nasa.jpl.oodt.cas.catalog.term.TermBucket;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Dictionary for create Catalog understandable Terms from Metadata
 * <p>
 */
public interface Dictionary {

	/**
	 * Given a Metadata object a lookup call to a given dictionary will
	 * always return the same TermBucket.  However, calls to several
	 * Dictionary's lookup method may return different TermBuckets for
	 * a given Metadata object.
	 * @param metadata The Metadata for which a TermBucket will be created
	 * @return TermBucket representing the given Metadata for this Dictionary or
	 * null if Metadata is not recognized by this Dictionary
	 */
	public TermBucket lookup(Metadata metadata) throws CatalogDictionaryException;

	/**
	 * Generates Metadata for the given TermBucket.  A call to lookup(Metadata) and
	 * then reverseLookup(TermBucket) may not give you back the original Metadata
	 * because lookup may ignore key/values in Metadata for which it does not
	 * understand.
	 * @param termBucket The TermBucket for which Metadata will be created
	 * @return Metadata for the given TermBucket.  If the TermBucket is not understood,
	 * then an empty Metadata object should be returned.
	 */
	public Metadata reverseLookup(TermBucket termBucket) throws CatalogDictionaryException;
			
	/**
	 * 
	 * @param queryExpression
	 * @return
	 * @throws CatalogDictionaryException
	 */
	public boolean understands(QueryExpression queryExpression) throws CatalogDictionaryException;
	
}
