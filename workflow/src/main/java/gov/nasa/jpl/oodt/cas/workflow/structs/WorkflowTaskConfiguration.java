//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.structs;

//JDK imports
import java.util.Properties;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>A specialized set of metadata properties for a {@link WorkflowTask}.</p>
 *
 */
public class WorkflowTaskConfiguration {

	/* the task configuration properties */
	private Properties taskProperties = null;
	
	/**
	 * <p>
	 * Default Constructor
	 * </p>.
	 */
	public WorkflowTaskConfiguration() {
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
	public WorkflowTaskConfiguration(Properties properties) {
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
