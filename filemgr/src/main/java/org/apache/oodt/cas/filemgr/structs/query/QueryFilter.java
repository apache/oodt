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

package org.apache.oodt.cas.filemgr.structs.query;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.query.conv.AsciiSortableVersionConverter;
import org.apache.oodt.cas.filemgr.structs.query.conv.VersionConverter;
import org.apache.oodt.cas.filemgr.structs.query.filter.FilterAlgor;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A query filter that allows user to filter results in a complex query
 * <p>
 */
public class QueryFilter {

    private String startDateTimeMetKey, endDateTimeMetKey, priorityMetKey;
    private FilterAlgor filterAlgor;
    private VersionConverter converter;

    public QueryFilter(String startDateTimeMetKey, String endDateTimeMetKey,
            String priorityMetKey, FilterAlgor filterAlgor) {
        this.startDateTimeMetKey = startDateTimeMetKey;
        this.endDateTimeMetKey = endDateTimeMetKey;
        this.priorityMetKey = priorityMetKey;
        this.filterAlgor = filterAlgor;
        this.converter = new AsciiSortableVersionConverter();
    }

    public String getStartDateTimeMetKey() {
        return startDateTimeMetKey;
    }

    public void setStartDateTimeMetKey(String startDateTimeMetKey) {
        this.startDateTimeMetKey = startDateTimeMetKey;
    }

    public String getEndDateTimeMetKey() {
        return endDateTimeMetKey;
    }

    public void setEndDateTimeMetKey(String endDateTimeMetKey) {
        this.endDateTimeMetKey = endDateTimeMetKey;
    }

    public String getPriorityMetKey() {
        return priorityMetKey;
    }

    public void setPriorityMetKey(String priorityMetKey) {
        this.priorityMetKey = priorityMetKey;
    }

    public FilterAlgor getFilterAlgor() {
        return filterAlgor;
    }

    public void setFilterAlgor(FilterAlgor filterAlgor) {
        if (filterAlgor != null) {
            this.filterAlgor = filterAlgor;
        }
    }

    public VersionConverter getConverter() {
        return converter;
    }

    public void setConverter(VersionConverter converter) {
        if (converter != null) {
            this.converter = converter;
        }
    }

}
