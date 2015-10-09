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

/**
 * A {@link EngineRunnerFactory} which creates
 * {@link AsynchronousLocalEngineRunner}s.
 * 
 * @author bfoster (Brian Foster)
 * @author mattmann (Chris Mattmann)
 */
public class AsynchronousLocalEngineRunnerFactory implements
    EngineRunnerFactory {

  private static final String NUM_THREADS_PROPERTY = "org.apache.oodt.cas.workflow.wengine.asynchronous.runner.num.threads";

  private int numThreads;

  public AsynchronousLocalEngineRunnerFactory() {
    numThreads = Integer.getInteger(NUM_THREADS_PROPERTY,
        AsynchronousLocalEngineRunner.DEFAULT_NUM_THREADS);
  }

  @Override
  public AsynchronousLocalEngineRunner createEngineRunner() {
    return new AsynchronousLocalEngineRunner(numThreads);
  }

  public void setNumThreads(int numThreads) {
    this.numThreads = numThreads;
  }

}
