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

//JDK imports
import java.util.List;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.cli.exception.CmdLineActionException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryFilter;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.structs.query.conv.VersionConverter;
import org.apache.oodt.cas.filemgr.structs.query.filter.FilterAlgor;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;

/**
 * Abstract query {@link CmdLineAction}.
 *
 * @author bfoster (Brian Foster)
 */
public abstract class AbstractQueryCliAction extends FileManagerCliAction {

   private String sortBy;
   private String outputFormat;
   private String delimiter;

   private FilterAlgor filterAlgor;
   private String startDateTimeMetKey;
   private String endDateTimeMetKey;
   private String priorityMetKey;
   private VersionConverter versionConverter;

   public AbstractQueryCliAction() {
      delimiter = "\n";
   }

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try (FileManagerClient client = getClient()) {
         ComplexQuery complexQuery = getQuery();
         complexQuery.setSortByMetKey(sortBy);
         complexQuery.setToStringResultFormat(outputFormat);

         if (filterAlgor != null) {
            Validate.notNull(startDateTimeMetKey,
                  "Must specify startDateTimeMetKey");
            Validate.notNull(endDateTimeMetKey,
                  "Must specify endDateTimeMetKey");
            Validate.notNull(priorityMetKey, "Must specify priorityMetKey");
            QueryFilter filter = new QueryFilter(startDateTimeMetKey,
                  endDateTimeMetKey, priorityMetKey, filterAlgor);
            if (versionConverter != null) {
               filter.setConverter(versionConverter);
            }
            complexQuery.setQueryFilter(filter);
         }

         List<QueryResult> results = client.complexQuery(complexQuery);
         StringBuilder returnString = new StringBuilder("");
         for (QueryResult qr : results) {
            returnString.append(qr.toString()).append(delimiter);
         }
         if (returnString.length() > 0) {
            printer.println(returnString.substring(0, returnString.length()
               - delimiter.length()));
         } else {
            printer.println("Query returned no results");
         }
      } catch (Exception e) {
         throw new CmdLineActionException("Failed to perform sql query : "
               + "sortBy '"
               + sortBy
               + "', "
               + "outputFormat '"
               + outputFormat
               + "', and delimiter '"
               + delimiter
               + "', filterAlgor '"
               + (filterAlgor != null ? filterAlgor.getClass()
                     .getCanonicalName() : null)
               + "', startDateTimeMetKey '"
               + startDateTimeMetKey
               + "', endDateTimeMetKey '"
               + endDateTimeMetKey
               + "', priorityMetKey '"
               + priorityMetKey
               + "', "
               + (versionConverter != null ? versionConverter.getClass()
                     .getCanonicalName() : null) + "' : " + e.getMessage(), e);
      }
   }

   public abstract ComplexQuery getQuery() throws Exception;

   public void setSortBy(String sortBy) {
      this.sortBy = sortBy;
   }

   public void setOutputFormat(String outputFormat) {
      this.outputFormat = outputFormat;
   }

   public void setDelimiter(String delimiter) {
      this.delimiter = delimiter;
   }

   public void setFilterAlgor(FilterAlgor filterAlgor) {
      this.filterAlgor = filterAlgor;
   }

   public void setStartDateTimeMetKey(String startDateTimeMetKey) {
      this.startDateTimeMetKey = startDateTimeMetKey;
   }

   public void setEndDateTimeMetKey(String endDateTimeMetKey) {
      this.endDateTimeMetKey = endDateTimeMetKey;
   }

   public void setPriorityMetKey(String priorityMetKey) {
      this.priorityMetKey = priorityMetKey;
   }

   public void setVersionConverter(VersionConverter versionConverter) {
      this.versionConverter = versionConverter;
   }
}
