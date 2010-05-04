//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.webapp.util;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

//JDK imports
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public final class WorkflowInstanceMetMap implements WorkflowInstanceMetMapKeys{
    private Metadata map;

    public WorkflowInstanceMetMap() {
        map = new Metadata();
    }
    
    public void addDefaultField(String fld){
        map.addMetadata(DEFAULT_WORKFLOW_ID, fld);
    }
    
    public void addDefaultFields(List flds){
        addWorkflowToMap(DEFAULT_WORKFLOW_ID, flds);
    }

    public void addWorkflowToMap(String id, List fields) {
        if (fields != null && fields.size() > 0) {
            for (Iterator i = fields.iterator(); i.hasNext();) {
                String fld = (String) i.next();
                addFieldToWorkflow(id, fld);
            }
        }
    }
    
    public List getDefaultFields(){
        return getFieldsForWorkflow(DEFAULT_WORKFLOW_ID);
    }

    public List getFieldsForWorkflow(String id) {
        return map.getAllMetadata(id);
    }

    public void addFieldToWorkflow(String id, String fld) {
        map.addMetadata(id, fld);
    }

    public List getWorkflows() {
        return Arrays.asList(map.getHashtable().keySet().toArray());
    }

}
