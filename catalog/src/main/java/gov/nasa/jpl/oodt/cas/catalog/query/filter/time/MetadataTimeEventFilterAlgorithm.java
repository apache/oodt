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

package gov.nasa.jpl.oodt.cas.catalog.query.filter.time;

//JDK imports
import java.util.Collections;
import java.util.List;

//OODT imports
import gov.nasa.jpl.oodt.cas.catalog.query.filter.FilterAlgorithm;
import gov.nasa.jpl.oodt.cas.commons.filter.TimeEventWeightedHash;

/**
 * @author bfoster
 * @version $Revision$
 */
public class MetadataTimeEventFilterAlgorithm extends FilterAlgorithm<MetadataTimeEvent> {
	
    protected long epsilon;
    
    public MetadataTimeEventFilterAlgorithm() {
        this.epsilon = 0;
    }
    
    public MetadataTimeEventFilterAlgorithm(long epsilon) {
        this.epsilon = epsilon;
    }
    
    public void setEpsilon(long epsilon) {
        this.epsilon = epsilon;
    }
    
    public long getEpsilon() {
        return this.epsilon;
    }

	@Override
	public List<MetadataTimeEvent> filter(List<MetadataTimeEvent> events) {
		TimeEventWeightedHash timeEventHash = TimeEventWeightedHash.buildHash(events, this.epsilon);
		return Collections.unmodifiableList((List<MetadataTimeEvent>) timeEventHash.getGreatestWeightedPathAsOrderedList());
	}
    
}
