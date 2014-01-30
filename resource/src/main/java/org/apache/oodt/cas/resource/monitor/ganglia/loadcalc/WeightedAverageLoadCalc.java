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

import org.apache.oodt.cas.resource.monitor.ganglia.GangliaMetKeys;

import java.text.NumberFormat;
import java.util.HashMap;

/**
 * @author rajith
 * @version $Revision$
 */
public class WeightedAverageLoadCalc implements LoadCalculator {

    private int loadOneWeight;
    private int loadFiveWeight;
    private int loadFifteenWeight;

    /* to format the load value*/
    private NumberFormat numberFormat;

    /**
     * Make a new WeightedAverageLoadCalc {@link LoadCalculator}
     * @param loadOneWeight weight for the load_one
     * @param loadFiveWeight weight for the load_five
     * @param loadFifteenWeight weight for the load_fifteen
     */
    public WeightedAverageLoadCalc (int loadOneWeight, int loadFiveWeight, int loadFifteenWeight){
        this.loadOneWeight = loadOneWeight;
        this.loadFiveWeight = loadFiveWeight;
        this.loadFifteenWeight = loadFifteenWeight;

        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(MAXIMUM_FRACTION_DIGITS);
    }
    /**
     * {@inheritDoc}
     *
     * load is calculated as follows
     * weightedLoadOne = loadOneWeight * minimum of (nodeCapacity, ((loadOne/numOfCPUs) * nodeCapacity))
     *
     * load = (weightedLoadOne + weightedLoadFive + weightedLoadFifteen) /
     *           (loadOneWeight + loadFiveWeight + loadFifteenWeight)
     */
    @Override
    public float calculateLoad(float nodeCapacity, HashMap<String, String> nodeMetrics) {
        int tn = Integer.valueOf(nodeMetrics.get(GangliaMetKeys.TN));
        int tmax = Integer.valueOf(nodeMetrics.get(GangliaMetKeys.TMAX));

        if(tn > (4 * tmax)){
           return nodeCapacity; //if the node is offline assign the node's capacity as the load
        }
        else {
            float weightedLoadOne = loadOneWeight * Math.min(nodeCapacity,
                    ((Float.valueOf(nodeMetrics.get(LOAD_ONE)) /
                            Float.valueOf(nodeMetrics.get(CPU_NUM))) * nodeCapacity));
            float weightedLoadFive = loadFiveWeight * Math.min(nodeCapacity,
                    ((Float.valueOf(nodeMetrics.get(LOAD_FIVE)) /
                            Float.valueOf(nodeMetrics.get(CPU_NUM)))* nodeCapacity));
            float weightedLoadFifteen = loadFifteenWeight * Math.min(nodeCapacity,
                    ((Float.valueOf(nodeMetrics.get(LOAD_FIFTEEN)) /
                            Float.valueOf(nodeMetrics.get(CPU_NUM)))* nodeCapacity));

            float weightedLoadAverage = (weightedLoadOne + weightedLoadFive + weightedLoadFifteen) /
                    (loadOneWeight + loadFiveWeight + loadFifteenWeight);

            return Float.valueOf(numberFormat.format(weightedLoadAverage));
        }
    }
}
