package gov.nasa.jpl.oodt.cas.workflow.structs;

import java.util.Properties;

public class WorkflowConditionConfiguration {
   
    /* the task configuration properties */
    private Properties taskProperties = null;
    
    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public WorkflowConditionConfiguration() {
        taskProperties = new Properties();
    }

    /**
     * <p>
     * Construct a new WorkflowTaskConfiguration from a java Properties object.
     * </p>
     * 
     * @param properties
     *            The task configuration properties.
     */
    public WorkflowConditionConfiguration(Properties properties) {
        taskProperties = properties;
    }

    /**
     * <p>
     * Adds the property denoted by the given <code>name></code> and
     * <code>value</code>.
     * </p>
     * 
     * @param name
     *            The property name.
     * @param value
     *            The property value.
     */
    public void addConfigProperty(String name, String value) {
        taskProperties.setProperty(name, value);
    }

    /**
     * 
     * @param propName
     *            The property to get the value for.
     * @return The String property value for the specified propName.
     */
    public String getProperty(String propName) {
        return taskProperties.getProperty(propName);
    }

    /**
     * 
     * @return The {@link Properties} for configuring this WorkflowTask.
     */
    public Properties getProperties() {
        return taskProperties;
    }

}
