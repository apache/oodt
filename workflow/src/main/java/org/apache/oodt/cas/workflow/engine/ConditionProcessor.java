/**
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

package org.apache.oodt.cas.workflow.engine;

//JDK imports
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowCondition;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;
import org.apache.oodt.cas.workflow.util.GenericWorkflowObjectFactory;
import org.apache.oodt.commons.util.DateConvert;

/**
 * 
 * The Strategy for evaluating {@link WorkflowCondition}s. Maintains an internal
 * cache of created {@link WorkflowConditionInstance}s, and then leverages those
 * to perform evaluations based on optional, timeout, and other control flow
 * rules. Also maintains an internal cache of {@link WorkflowCondition} start
 * times, so that timeouts can be computed.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class ConditionProcessor {

  protected Map<String, String> COND_TIMEOUTS;

  protected Map<String, HashMap<String, WorkflowConditionInstance>> CONDITION_CACHE;

  private static final Logger LOG = Logger.getLogger(ConditionProcessor.class
      .getName());

  public ConditionProcessor() {
    this.COND_TIMEOUTS = new HashMap<String, String>();
    this.CONDITION_CACHE = new HashMap<String, HashMap<String, WorkflowConditionInstance>>();
  }

  public boolean satisfied(List<WorkflowCondition> conditionList, String id,
      Metadata context) {
    for (WorkflowCondition c : conditionList) {
      WorkflowConditionInstance cInst = null;
      if (!COND_TIMEOUTS.containsKey(c.getConditionId())) {
        COND_TIMEOUTS.put(c.getConditionId(), generateISO8601());
      }

      // see if we've already cached this condition instance
      if (CONDITION_CACHE.get(id) != null) {
        HashMap<String, WorkflowConditionInstance> conditionMap = CONDITION_CACHE
            .get(id);

        /*
         * okay we have some conditions cached for this task, see if we have the
         * one we need
         */
        if (conditionMap.get(c.getConditionId()) != null) {
          cInst = (WorkflowConditionInstance) conditionMap.get(c
              .getConditionId());
        }
        /* if not, then go ahead and create it and cache it */
        else {
          cInst = GenericWorkflowObjectFactory
              .getConditionObjectFromClassName(c
                  .getConditionInstanceClassName());
          conditionMap.put(c.getConditionId(), cInst);
        }
      }
      /* no conditions cached yet, so set everything up */
      else {
        HashMap<String, WorkflowConditionInstance> conditionMap = new HashMap<String, WorkflowConditionInstance>();
        cInst = GenericWorkflowObjectFactory.getConditionObjectFromClassName(c
            .getConditionInstanceClassName());
        conditionMap.put(c.getConditionId(), cInst);
        CONDITION_CACHE.put(id, conditionMap);
      }

      // actually perform the evaluation
      boolean result = false;
      if (!(result = cInst.evaluate(context, c.getCondConfig()))
          && !isOptional(c, result) && !timedOut(c)) {
        return false;
      }
    }

    return true;
  }

  public boolean isOptional(WorkflowCondition condition, boolean result) {
    if (condition.isOptional()) {
      LOG.log(Level.WARNING, "Condition: [" + condition.getConditionId()
          + "] is optional: evaluation results: [" + result + "] ignored");
      return true;
    } else {
      LOG.log(Level.INFO, "Condition: [" + condition.getConditionId()
          + "] is required: evaluation results: [" + result + "] included.");
      return result;
    }
  }

  public boolean timedOut(WorkflowCondition condition) {
    if (condition.getTimeoutSeconds() == -1)
      return false;
    String isoStartDateTimeStr = COND_TIMEOUTS.get(condition.getConditionId());
    Date isoStartDateTime = null;
    try {
      isoStartDateTime = DateConvert.isoParse(isoStartDateTimeStr);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.log(Level.WARNING, "Unable to parse start date time for condition: ["
          + condition.getConditionId() + "]: start date time: ["
          + isoStartDateTimeStr + "]: Reason: " + e.getMessage());
      return false;
    }
    Date now = new Date();
    long numSecondsElapsed = (now.getTime() - isoStartDateTime.getTime()) / (1000);
    if (numSecondsElapsed >= condition.getTimeoutSeconds()) {
      LOG.log(
          Level.INFO,
          "Condition: [" + condition.getConditionName()
              + "]: exceeded timeout threshold of: ["
              + condition.getTimeoutSeconds() + "] seconds: elapsed time: ["
              + numSecondsElapsed + "]");
      return true;
    } else {
      LOG.log(
          Level.FINEST,
          "Condition: [" + condition.getConditionName()
              + "]: has not exceeded timeout threshold of: ["
              + condition.getTimeoutSeconds() + "] seconds: elapsed time: ["
              + numSecondsElapsed + "]");
      return false;
    }
  }

  private String generateISO8601() {
    return DateConvert.isoFormat(new Date());
  }

}
