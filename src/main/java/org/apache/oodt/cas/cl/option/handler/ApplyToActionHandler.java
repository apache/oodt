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
package org.apache.oodt.cas.cl.option.handler;

//OODT static imports
import static org.apache.oodt.cas.cl.util.CmdLineUtils.convertToType;

//JDK imports
import java.util.Arrays;
import java.util.List;

//Apache imports
import org.apache.commons.lang.StringUtils;

//OODT imports
import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;

//Google imports
import com.google.common.annotations.VisibleForTesting;

/**
 * {@link CmdLineOptionHandler} which applies {@link CmdLineOption} values to
 * given {@link CmdLineAction}.
 * 
 * @author bfoster (Brian Foster)
 */
public class ApplyToActionHandler implements CmdLineOptionHandler {

   private List<ApplyToAction> applyToActions;

   public void setApplyToActions(List<ApplyToAction> applyToActions) {
      this.applyToActions = applyToActions;
   }

   public List<ApplyToAction> getApplyToActions() {
      return applyToActions;
   }

   public void handleOption(CmdLineAction action,
         CmdLineOptionInstance optionInstance) {
      try {
         Class<?> type = optionInstance.getOption().getType();
         List<?> vals = (optionInstance.getValues().isEmpty()) ? convertToType(
               Arrays.asList(new String[] { "true" }), type = Boolean.TYPE)
               : convertToType(optionInstance.getValues(), type);
         String methodName = getMethodName(action.getName());
         if (methodName != null) {
            action.getClass().getMethod(methodName, type)
                  .invoke(action, vals.toArray(new Object[vals.size()]));
         } else {
            action.getClass()
                  .getMethod(
                        "set"
                              + StringUtils.capitalize(optionInstance
                                    .getOption().getLongOption()), type)
                  .invoke(action, vals.toArray(new Object[vals.size()]));
         }
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @VisibleForTesting
   protected String getMethodName(String actionName) {
      if (applyToActions != null) {
         for (ApplyToAction applyToAction : applyToActions) {
            if (applyToAction.getActionName().equals(actionName)) {
               return applyToAction.getMethodName();
            }
         }
      }
      return null;
   }

   public String getHelp(CmdLineOption option) {
      return "Will invoke 'set" + option.getLongOption()
            + "' on action selected";
   }
}
