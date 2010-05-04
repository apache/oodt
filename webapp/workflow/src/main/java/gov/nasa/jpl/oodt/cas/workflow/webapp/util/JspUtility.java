//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.webapp.util;

//JDK imports
import java.util.Iterator;

//OODT imports
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowInstance;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTask;

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
