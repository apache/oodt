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


package org.apache.oodt.cas.workflow.webapp.util;

//JDK imports
import java.util.Iterator;

//OODT imports
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowTask;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A collection of Utilities for the JSP pages in the Workflow webapp.
 * </p>
 * 
 */
public final class JspUtility {

    private JspUtility() throws InstantiationException {
        throw new InstantiationException(
                "Don't instantiate Utility components!");
    }

    /**
     * <p>
     * Summarizes a given String of words (the <code>orig</code> parameter),
     * and limits the size of the individual words in the string by the given
     * <code>wordThreshold</code>, and limits the final size of the final
     * summarized word string by the given <code>maxLengthTotal</code>.
     * </p>
     * 
     * @param orig
     *            The original String to summarize.
     * @param wordThreshhold
     *            The maximum amount of characters for any given word in the
     *            string.
     * @param maxLengthTotal
     *            The maximum final size of the summarized set of words.
     * @return A summarized string.
     */
    public static String summarizeWords(String orig, int wordThreshhold,
            int maxLengthTotal) {
        String[] words = orig.split(" ");
        StringBuffer summarizedString = new StringBuffer();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            summarizedString.append(word.substring(0, Math.min(wordThreshhold,
                    word.length())));
            summarizedString.append(" ");
        }

        return summarizedString.substring(0,
                Math.min(maxLengthTotal, summarizedString.length())).toString();
    }

    public static String getTaskNameFromTaskId(WorkflowInstance w, String taskId) {
        if (w.getWorkflow() != null && w.getWorkflow().getTasks() != null
                && w.getWorkflow().getTasks().size() > 0) {
            for (Iterator i = w.getWorkflow().getTasks().iterator(); i
                    .hasNext();) {
                WorkflowTask task = (WorkflowTask) i.next();
                if (task.getTaskId().equals(taskId)) {
                    return task.getTaskName();
                }
            }

            return null;
        } else
            return null;
    }

}
