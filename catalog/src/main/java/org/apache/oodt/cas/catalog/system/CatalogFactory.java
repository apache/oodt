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
package org.apache.oodt.cas.catalog.system;

//JDK imports
import java.util.List;
import java.util.Vector;

//OOD imports
import org.apache.oodt.cas.catalog.struct.Dictionary;
import org.apache.oodt.cas.catalog.struct.DictionaryFactory;
import org.apache.oodt.cas.catalog.struct.IndexFactory;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

/**
 * @author bfoster
 * @version $Revision$
 *
 */
public class CatalogFactory {

	protected String catalogId;
	protected IndexFactory indexFactory;
	protected List<DictionaryFactory> dictionaryFactories;
	protected boolean restrictQueryPermissions;
	protected boolean restrictIngestPermissions;
	
	public Catalog createCatalog() {
		Vector<Dictionary> dictionaries = null;
		if (this.dictionaryFactories != null) {
			dictionaries = new Vector<Dictionary>();
			for (DictionaryFactory dictionaryFactory : this.dictionaryFactories) {
			  dictionaries.add(dictionaryFactory.createDictionary());
			}
		}
		return new Catalog(this.catalogId, this.indexFactory.createIndex(), dictionaries, this.restrictQueryPermissions, this.restrictIngestPermissions);
	}

	@Required
	public void setCatalogId(String catalogId) {
		this.catalogId = catalogId;
	}

	@Required
	public void setIndexFactory(IndexFactory indexFactory) {
		this.indexFactory = indexFactory;
	}

	@Required
	public void setDictionaryFactories(List<DictionaryFactory> dictionaryFactories) {
		this.dictionaryFactories = dictionaryFactories;
	}

	@Required
	public void setRestrictQueryPermissions(boolean restrictQueryPermissions) {
		this.restrictQueryPermissions = restrictQueryPermissions;
	}

	@Required
	public void setRestrictIngestPermissions(boolean restrictIngestPermissions) {
		this.restrictIngestPermissions = restrictIngestPermissions;
	}
	
}
