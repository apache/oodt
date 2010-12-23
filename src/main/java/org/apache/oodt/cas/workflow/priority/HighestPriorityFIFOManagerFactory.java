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
package org.apache.oodt.cas.workflow.priority;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Factory for creating highest priority first priority manager with FIFO boost
 * </p>.
 */
public class HighestPriorityFIFOManagerFactory implements
		PriorityManagerFactory {

	private int secondsBetweenBoosts;
	private double boostAmount;
	private double boostCap;
	
	public HighestPriorityFIFOManager createPriorityManager() {
		return new HighestPriorityFIFOManager(secondsBetweenBoosts, boostAmount, boostCap);
	}

	public void setSecondsBetweenBoosts(int secondsBetweenBoosts) {
		this.secondsBetweenBoosts = secondsBetweenBoosts;
	}

	public void setBoostAmount(double boostAmount) {
		this.boostAmount = boostAmount;
	}

	public void setBoostCap(double boostCap) {
		this.boostCap = boostCap;
	}
	
}
