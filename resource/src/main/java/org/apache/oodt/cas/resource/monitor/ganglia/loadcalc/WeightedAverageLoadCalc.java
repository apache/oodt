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

//JDK imports
import java.text.NumberFormat;
import java.util.Map;

//OODT imports
import static org.apache.oodt.cas.resource.monitor.ganglia.GangliaMetKeys.MAXIMUM_FRACTION_DIGITS;
import static org.apache.oodt.cas.resource.monitor.ganglia.GangliaMetKeys.TN;
import static org.apache.oodt.cas.resource.monitor.ganglia.GangliaMetKeys.TMAX;
import static org.apache.oodt.cas.resource.monitor.ganglia.GangliaMetKeys.CPU_NUM;
import static org.apache.oodt.cas.resource.monitor.ganglia.GangliaMetKeys.LOAD_ONE;
import static org.apache.oodt.cas.resource.monitor.ganglia.GangliaMetKeys.LOAD_FIVE;
import static org.apache.oodt.cas.resource.monitor.ganglia.GangliaMetKeys.LOAD_FIFTEEN;

/**
 * @author rajith
 * @author mattmann
 * @version $Revision$
 */
public class WeightedAverageLoadCalc implements LoadCalculator {

    private double loadOneWeight;
    private double loadFiveWeight;
    private double loadFifteenWeight;

    /* to format the load value*/
    private NumberFormat numberFormat;

    /**
     * Make a new WeightedAverageLoadCalc {@link LoadCalculator}
     * @param loadOneWeight weight for the load_one
     * @param loadFiveWeight weight for the load_five
     * @param loadFifteenWeight weight for the load_fifteen
     */
    public WeightedAverageLoadCalc (double loadOneWeight, double loadFiveWeight, double loadFifteenWeight){
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
     * weightedLoadOne = loadOneWeight * minimum of (numOfCPUs, ((loadOne/numOfCPUs) * numOfCPUs))
     *
     * load = (weightedLoadOne + weightedLoadFive + weightedLoadFifteen) /
     *           (loadOneWeight + loadFiveWeight + loadFifteenWeight)
     */
    @Override
    public double calculateLoad(Map<String, String> nodeMetrics) {
        double tn = Double.valueOf(nodeMetrics.get(TN));
        double tmax = Double.valueOf(nodeMetrics.get(TMAX));
        double numCpus = Double.valueOf(nodeMetrics.get(CPU_NUM));

        if(tn > (4 * tmax)){
           return numCpus; //if the node is offline assign the node's capacity as the load
        }
        else {
            double weightedLoadOne = loadOneWeight * Math.min(numCpus,
                    ((Double.valueOf(nodeMetrics.get(LOAD_ONE)) /
                            Double.valueOf(nodeMetrics.get(CPU_NUM))) * numCpus));
            double weightedLoadFive = loadFiveWeight * Math.min(numCpus,
                    ((Double.valueOf(nodeMetrics.get(LOAD_FIVE)) /
                            Double.valueOf(nodeMetrics.get(CPU_NUM)))* numCpus));
            double weightedLoadFifteen = loadFifteenWeight * Math.min(numCpus,
                    ((Double.valueOf(nodeMetrics.get(LOAD_FIFTEEN)) /
                            Double.valueOf(nodeMetrics.get(CPU_NUM)))* numCpus));

            double weightedLoadAverage = (weightedLoadOne + weightedLoadFive + weightedLoadFifteen) /
                    (loadOneWeight + loadFiveWeight + loadFifteenWeight);
            
            System.out.println("Weighted load one: ["+weightedLoadOne+"]: weighted load five: ["+weightedLoadFive+"] weighted load fifteen: ["+weightedLoadFifteen+"]");
            return Double.valueOf(numberFormat.format(weightedLoadAverage));
        }
    }
}
