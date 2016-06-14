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


package org.apache.oodt.cas.metadata.preconditions;

//JDK imports
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//Spring imports


/**
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Evaluation utility for metadata extractor preconditions
 * </p>.
 */
public class PreCondEvalUtils implements PreConditionOperatorMetKeys {

    private static Logger LOG = Logger.getLogger(PreCondEvalUtils.class
            .getName());

    private ApplicationContext applicationContext;

    public PreCondEvalUtils(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Evaluates whether all preconditions pass or not
     * 
     * @param preCondComparatorIds
     *            The String identifiers of the {@link PreConditionComparator}s
     *            in question
     * @param product
     *            the {@link File} to test the preconditions against to
     *            determine whether or not the {@link org.apache.oodt.cas.metadata.MetExtractor} should be
     *            run or not.
     * @return True if all preconditions pass and false otherwise
     */
    public boolean eval(List<String> preCondComparatorIds, File product) {
        for (String preCondComparatorId : preCondComparatorIds) {
            if (!((PreConditionComparator<?>) applicationContext.getBean(
                    preCondComparatorId, PreConditionComparator.class))
                    .passes(product)) {
                LOG.log(Level.INFO, "Failed precondition comparator id "
                        + preCondComparatorId);
                return false;
            } else {
                LOG.log(Level.INFO, "Passed precondition comparator id "
                        + preCondComparatorId);
            }
        }

        return true;
    }

}
