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
package org.apache.oodt.cas.workflow.server.action;

//JDK imports
import java.text.DecimalFormat;
import java.util.Date;

//OODT imports
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * Action for print out percent loaded/cached of WorkflowProcessors
 * <p>
 */
public class GetPercentLoaded extends WorkflowEngineServerAction {

	@Override
	public void performAction(WorkflowEngineClient weClient) throws Exception {
		try {
			long timePassedInMillis = new Date().getTime() - weClient.getLaunchDate().getTime();
			long loaded = weClient.getNumOfLoadedProcessors();
			long total = weClient.getNumOfWorkflows();
			long etaInMins = 0;
			if (loaded != total)
				etaInMins = (long) ((((double) (total - loaded) * (double) timePassedInMillis) / (double) loaded) / 1000.0 / 60.0);
			double factionLoaded = (double) loaded / (double) total;
			System.out
					.println("Workflows Loaded (Percent: '" + (int) (factionLoaded * 100.0) + "%', Decimal: '"
							+ new DecimalFormat("#.###").format(factionLoaded) + "', Faction: '"
							+ loaded + "/" + total + "', ETA: '" + etaInMins + " mins')");
		} catch (Exception e) {
			throw new Exception("Failed to get percent loaded workflows : "
					+ e.getMessage(), e);
		}
	}

}
