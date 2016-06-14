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
package org.apache.oodt.cas.filemgr.cli.action;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.util.SqlParser;

/**
 * A {@link CmdLineAction} which queries the FileManager by parsing an SQL like
 * query into a FileManager {@link Query}.
 * 
 * @author bfoster (Brian Foster)
 */
public class SqlQueryCliAction extends AbstractQueryCliAction {

   private String query;

   public SqlQueryCliAction() {
      super();
   }

   @Override
   public ComplexQuery getQuery() throws QueryFormulationException {
      Validate.notNull(query, "Must specify query");

      return SqlParser.parseSqlQuery(query);
   }

   public void setQuery(String query) {
      this.query = query;
   }
}
