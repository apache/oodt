package gov.nasa.jpl.oodt.cas.workflow.examples;

import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowConditionInstance;

public class CheckForMetadataKeys implements WorkflowConditionInstance {

    public boolean evaluate(Metadata metadata,
            WorkflowConditionConfiguration config) {
        String[] reqMetKeys = (config.getProperty("reqMetKeys") + ",")
                .split(",");
        for (String reqMetKey : reqMetKeys) {
            if (!metadata.containsKey(reqMetKey))
                return false;
        }
        return true;
    }

}
