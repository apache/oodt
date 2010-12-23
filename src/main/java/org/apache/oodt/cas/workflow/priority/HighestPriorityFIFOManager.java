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

//JDK imports
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.workflow.processor.ProcessorStub;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Highest priority first priority manager with FIFO boost
 * </p>.
 */
public class HighestPriorityFIFOManager implements PriorityManager {

	private int secondsBetweenBoosts;
	private double boostAmount;
	private double boostCap;
	
	public HighestPriorityFIFOManager(int secondsBetweenBoosts, double boostAmount, double boostCap) {
		this.secondsBetweenBoosts = secondsBetweenBoosts;
		this.boostAmount = boostAmount;
		this.boostCap = boostCap;
	}
	
	public void sort(List<ProcessorStub> canadates) {
		
		Collections.sort(canadates, new Comparator<ProcessorStub>() {
			public int compare(ProcessorStub o1, ProcessorStub o2) {
				return calculatePriority(o2).compareTo(calculatePriority(o1));
			}
		});		
	}
	
	private Double calculatePriority(ProcessorStub processorStub) {
		double aliveTime = (double) (System.currentTimeMillis() - processorStub.getProcessorInfo().getCreationDate().getTime());
		double boostPercentage = aliveTime / 1000.0 / (double) this.secondsBetweenBoosts;
		return Math.max(processorStub.getPriority().getValue(), Math.min(this.boostCap, Double.valueOf(processorStub.getPriority().getValue() + (boostPercentage * this.boostAmount))));
	}

}
