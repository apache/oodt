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
package org.apache.oodt.cas.catalog.struct.impl.dictionary;

//OODT imports
import org.apache.oodt.cas.catalog.exception.CatalogDictionaryException;
import org.apache.oodt.cas.catalog.query.*;
import org.apache.oodt.cas.catalog.struct.Dictionary;
import org.apache.oodt.cas.catalog.term.Term;
import org.apache.oodt.cas.catalog.term.TermBucket;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.util.Set;


/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 */
public class WorkflowManagerDictionary implements Dictionary {

	public TermBucket lookup(Metadata metadata)
			throws CatalogDictionaryException {
		if (metadata.getMetadata("ProductType") != null && metadata.getAllMetadata("ProductType").contains("Workflows")) {
			TermBucket workflowBucket = new TermBucket("Workflows");
			for (Object key : metadata.getHashtable().keySet()) 
				workflowBucket.addTerm(new Term((String) key, metadata.getAllMetadata((String) key)));
			return workflowBucket;
		}else {
			return null;
		}
	}

	public Metadata reverseLookup(TermBucket termBucket)
			throws CatalogDictionaryException {
		Metadata metadata = new Metadata();
		if (termBucket.getName().equals("Workflows")) {
			for (Term term : termBucket.getTerms())
				metadata.addMetadata(term.getName(), term.getValues());
		}
		return metadata;
	}

	public boolean understands(QueryExpression queryExpression)
			throws CatalogDictionaryException {
		Set<String> bucketNames = queryExpression.getBucketNames();
		if (bucketNames == null || bucketNames.contains("Workflows")) {
			if (queryExpression instanceof NotQueryExpression 
					|| queryExpression instanceof ComparisonQueryExpression 
					|| queryExpression instanceof StdQueryExpression 
					|| queryExpression instanceof QueryLogicalGroup) {
				return true;
			} else
				return false;	
		}
		return false;
	}

}
