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

package org.apache.oodt.commons.activity;

import java.util.List;

/**
 * A Storage is a place to store for the long term record an activity and the incidents
 * that define it.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public interface Storage {
	/**
	 * Store the activity.
	 *
	 * @param id Activity ID.
	 * @param incidents a {@link List} of {@link Incident}s that defined it.
	 */
	void store(String id, List incidents);
}
