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

package org.apache.oodt.cas.resource.monitor.ganglia;

import org.apache.oodt.cas.resource.monitor.ResourceMonitor;
import org.apache.oodt.cas.resource.monitor.ResourceMonitorFactory;
import org.apache.oodt.cas.resource.monitor.ganglia.loadcalc.LoadCalculator;
import org.apache.oodt.cas.resource.structs.ResourceNode;
import org.apache.oodt.cas.resource.util.GenericResourceManagerObjectFactory;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rajith
 * @version $Revision$
 */
public class GangliaResourceMonitorFactory implements ResourceMonitorFactory {

    private static final Logger LOG = Logger.getLogger(GangliaResourceMonitorFactory.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public ResourceMonitor createResourceMonitor() {
        try {
            String loadCalculatorFactoryStr =  System
                    .getProperty("org.apache.oodt.cas.resource.monitor.loadcalc.factory");
            String nodeRepoFactoryStr = System
                    .getProperty("org.apache.oodt.cas.resource.nodes.repo.factory");

            List<ResourceNode> resourceNodes = GenericResourceManagerObjectFactory
                    .getNodeRepositoryFromFactory(nodeRepoFactoryStr).loadNodes();
            LoadCalculator loadCalculator = GenericResourceManagerObjectFactory
                    .getLoadCalculatorFromServiceFactory(loadCalculatorFactoryStr);

            return new GangliaResourceMonitor(loadCalculator, resourceNodes);
        } catch (Exception e){
            LOG.log(Level.SEVERE, "Failed to create Resource Monitor : " + e.getMessage(), e);
            return null;
        }
    }
}
