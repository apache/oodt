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

package org.apache.oodt.cas.resource.monitor.ganglia.loadcalc;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rajith
 * @version $Revision$
 */
public class WeightedAverageLoadCalcFactory implements LoadCalculatorFactory {

    private static final Logger LOG = Logger.getLogger(WeightedAverageLoadCalcFactory.class.getName());

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public LoadCalculator createLoadCalculator() {
        try {
            int loadOneWeight = Integer.parseInt(
                    System.getProperty("org.apache.oodt.cas.resource.monitor.loadcalc.weight.loadone"));
            int loadFiveWeight = Integer.parseInt(
                    System.getProperty("org.apache.oodt.cas.resource.monitor.loadcalc.weight.loadfive"));
            int loadFifteenWeight = Integer.parseInt(
                    System.getProperty("org.apache.oodt.cas.resource.monitor.loadcalc.weight.loadfifteen"));
            return new WeightedAverageLoadCalc(loadOneWeight, loadFiveWeight, loadFifteenWeight);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create Load Calculator : " + e.getMessage(), e);
            return null;
        }
    }
}
