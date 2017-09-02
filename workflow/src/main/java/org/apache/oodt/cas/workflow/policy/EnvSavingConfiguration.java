//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package org.apache.oodt.cas.workflow.policy;

//JDK imports
import java.util.Properties;

//OODT imports
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A {@link WorkflowTaskConfiguration} that preserves whether or not a task config
 * property was envReplace'd or not.
 * </p>.
 */
public class EnvSavingConfiguration extends WorkflowTaskConfiguration {

    private Properties envReplaceMap;

    public EnvSavingConfiguration() {
        super();
        envReplaceMap = new Properties();
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration#addConfigProperty(java.lang.String,
     *      java.lang.String)
     */
    public void addConfigProperty(String propName, String propVal) {
        addConfigProperty(propName, propVal, false);

    }

    /**
     * Adds a configuration property, along with its envReplace
     * information.
     * 
     * @param propName The workflow config property name.
     * @param propVal The workflow config property val.
     * @param isReplace Whether or not the property should be envReplaced.
     */
    public void addConfigProperty(String propName, String propVal,
            boolean isReplace) {
        super.addConfigProperty(propName, propVal);
        envReplaceMap.put(propName, String.valueOf(isReplace));
    }

    public boolean isReplace(String propName) {
        return Boolean.valueOf((String) this.envReplaceMap.get(propName))
                .booleanValue();
    }

}
