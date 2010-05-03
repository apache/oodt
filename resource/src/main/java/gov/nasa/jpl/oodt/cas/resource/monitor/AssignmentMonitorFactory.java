//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.monitor;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;

//JDK imports
import java.util.Arrays;
import java.util.List;

/**
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * Creates implementations of {@link AssignmentMonitor}s.
 * </p>
 * 
 */
public class AssignmentMonitorFactory implements MonitorFactory {

	private List nodesDirList;

	public AssignmentMonitorFactory() {
		String nodesDirUris = System
				.getProperty("gov.nasa.jpl.oodt.cas.resource.monitor.nodes.dirs");

		if (nodesDirUris != null) {
			/* do env var replacement */
			nodesDirUris = PathUtils.replaceEnvVariables(nodesDirUris);
			String[] dirUris = nodesDirUris.split(",");
			nodesDirList = Arrays.asList(dirUris);
		}
	}

	/* (non-Javadoc)
	 * @see gov.nasa.jpl.oodt.cas.resource.monitor.MonitorFactory#createMonitor()
	 */
	public Monitor createMonitor() {
		if (nodesDirList != null) {
			return new AssignmentMonitor(nodesDirList);
		} else {
			return null;
		}
	}

}