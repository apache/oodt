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

package org.apache.oodt.cas.catalog.query.filter.time;

//JDK imports
import java.util.Collections;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.query.filter.QueryFilter;
import org.apache.oodt.cas.catalog.query.filter.time.conv.AsciiSortableVersionConverter;
import org.apache.oodt.cas.catalog.query.filter.time.conv.VersionConverter;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A query filter that allows user to filter results in a complex query
 * <p>
 */
public class MetadataTimeEventQueryFilter extends QueryFilter<MetadataTimeEvent> {

    private String startDateTimeMetKey, endDateTimeMetKey, priorityMetKey;
    private VersionConverter converter;

    public MetadataTimeEventQueryFilter() {
    	super();
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

    public VersionConverter getConverter() {
        return converter;
    }

    public void setConverter(VersionConverter converter) {
        if (converter != null)
            this.converter = converter;
    }

	@Override
	protected List<MetadataTimeEvent> metadataToFilterType(List<TransactionalMetadata> metadataList) {
		List<MetadataTimeEvent> timeEvents = new Vector<MetadataTimeEvent>();
		for (TransactionalMetadata transactionalMetadata : metadataList) {
			double priority = 0;
			if (this.getPriorityMetKey() != null)
				priority = Double.parseDouble(transactionalMetadata.getMetadata().getMetadata(this.priorityMetKey));
			long startTime = Long.parseLong(transactionalMetadata.getMetadata().getMetadata(this.startDateTimeMetKey));
			String endTimeString = transactionalMetadata.getMetadata().getMetadata(this.endDateTimeMetKey);
			long endTime = startTime;
			if (endTimeString != null)
				endTime = Long.parseLong(endTimeString);
			timeEvents.add(new MetadataTimeEvent(startTime, endTime, priority, transactionalMetadata));
		}
		return Collections.unmodifiableList(timeEvents);
	}

	@Override
	protected List<TransactionalMetadata> filterTypeToMetadata(List<MetadataTimeEvent> filterObjects) {
		List<TransactionalMetadata> metadataList = new Vector<TransactionalMetadata>();
		for (MetadataTimeEvent timeEvent : filterObjects)
			metadataList.add(timeEvent.getTimeObject());
		return Collections.unmodifiableList(metadataList);
	}

}
