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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	
	private static final Logger LOG = Logger.getLogger(LocalEngineRunner.class.getName());
	
	private int cacheSize;
	private int numOfSlots;
	private int usedSlots = 0;
	private List<TaskInstance> cache;
	private boolean running = true;
	
	public LocalEngineRunner(int numOfSlots, int cacheSize) {
		this.numOfSlots = numOfSlots;
		this.cacheSize = cacheSize;
		if (this.cacheSize > 0) {
			new Thread(new Runnable() {
				public void run() {
					while (running) {
						try {
							if (LocalEngineRunner.this.numOfSlots > LocalEngineRunner.this.usedSlots && LocalEngineRunner.this.cache.size() > 0)
								LocalEngineRunner.this.execute(LocalEngineRunner.this.cache.remove(0));
						}catch (Exception e) {
							LOG.log(Level.SEVERE, "Failed to submit job from cache : " + e.getMessage(), e);
						}
						try {
							synchronized(this) {
								this.wait(2000);
							}
						}catch (Exception e) {
							LOG.log(Level.WARNING, "Local Runner cache submitter thread wait terminated : " + e.getMessage(), e);
						}
					}
				}
			}).start();
		}
	}
	
	public void execute(final TaskInstance workflowInstance) throws Exception {
		incrSlots();
		if (this.cache.size() > 0)
			cache.add(workflowInstance);
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
	public synchronized int getOpenSlots(TaskInstance workflowInstance) throws Exception {
		return (numOfSlots - usedSlots) + this.cacheSize;
	}

	@Override
	public boolean hasOpenSlots(TaskInstance workflowInstance) throws Exception {
		return this.getOpenSlots(workflowInstance) > 0;
	}
	
	@Override
	public void shutdown() throws Exception {
		this.running = false;
	}
	
	private synchronized void incrSlots() {
		usedSlots++;
	}
	
	private synchronized void decrSlots() {
		usedSlots--;
	}
		
}
