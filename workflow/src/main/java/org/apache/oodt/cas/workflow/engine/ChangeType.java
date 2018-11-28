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
package org.apache.oodt.cas.workflow.engine;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Different Listener Message Categories
 * <p>
 */
public class ChangeType {

	public static final ChangeType STATE = new ChangeType("State");
	public static final ChangeType DYN_MET = new ChangeType("DynamicMetadata");
	public static final ChangeType STATIC_MET = new ChangeType("StaticMetadata");
	public static final ChangeType LISTENERS = new ChangeType("Listeners");
	public static final ChangeType PRIORITY = new ChangeType("Priority");
	public static final ChangeType EXCUSED_WPS = new ChangeType("ExcusedWorkflowProcessors");

	private String name;
	
	public ChangeType(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof ChangeType) {
			return this.name.equals(((ChangeType) obj).name);
		}else {
			return false;
		}
	}
	
	public int hashCode() {
		return this.getName().hashCode();
	}
	
}
