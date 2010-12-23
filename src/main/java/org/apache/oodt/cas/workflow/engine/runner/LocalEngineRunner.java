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
package org.apache.oodt.cas.workflow.engine.runner;

//OODT imports
import org.apache.oodt.cas.workflow.instance.TaskInstance;

/**
 * 
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 *
 *	Mutli-Threaded Runner which executes TaskInstances in local JVM
 *
 */
public class LocalEngineRunner extends EngineRunner {
	
	private static final int NUM_OF_SLOTS = 6;
	private int usedSlots = 0;
	
	public void execute(final TaskInstance workflowInstance) throws Exception {
		incrSlots();
		new Thread(new Runnable() {
			public void run() {
				try {
					workflowInstance.execute();
				}finally {
					decrSlots();
				}
			}
		}).start();
	}

	@Override
	public synchronized int getOpenSlots() throws Exception {
		return NUM_OF_SLOTS - usedSlots;
	}

	@Override
	public boolean hasOpenSlots() throws Exception {
		return this.getOpenSlots() > 0;
	}
	
	private synchronized void incrSlots() {
		usedSlots++;
	}
	
	private synchronized void decrSlots() {
		usedSlots--;
	}
		
}
