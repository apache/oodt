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
package org.apache.oodt.cas.catalog.term;

//JDK imports
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Bucket which has Terms attached to it
 * <p>
 */
public class TermBucket extends Bucket {

	private HashMap<String, Term> terms;
	
	public TermBucket() {
		super();
		this.terms = new HashMap<String, Term>();
	}
	
	public TermBucket(String name) {
		super(name);
		this.terms = new HashMap<String, Term>();
	}
	
	public TermBucket(String name, Set<Term> terms) {
		this(name);
		this.setTerms(terms);
	}
	
	public void setTerms(Set<Term> terms) {
		if (terms != null) {
			this.terms = new HashMap<String, Term>();
			for (Term term : terms) {
			  this.terms.put(term.name, term);
			}
		}
	}
	
	public void addTerms(Set<Term> terms) {
		this.addTerms(terms, false);
	}
	
	public void addTerms(Set<Term> terms, boolean replace) {
		if (replace) {
			for (Term term : terms) {
			  this.terms.put(term.name, term);
			}
		}else {
			for (Term term : terms) {
				Term found = this.terms.get(term.name);
				if (found != null) {
					List<String> newTermValues = new Vector<String>();
					newTermValues.addAll(found.getValues());
					newTermValues.addAll(term.getValues());
					found.setValues(newTermValues);
				}else {
					this.terms.put(term.name, term);
				}
			}
		}
	}
	
	public void addTerm(Term term) {
		this.addTerms(Collections.singleton(term));
	}
	
	public void addTerm(Term term, boolean replace) {
		this.addTerms(Collections.singleton(term), replace);
	}
	
	public Set<Term> getTerms() {
		return Collections.unmodifiableSet(new HashSet<Term>(this.terms.values()));
	}
	
	public Term getTermByName(String termName) {
		return this.terms.get(termName);
	}
	
}
