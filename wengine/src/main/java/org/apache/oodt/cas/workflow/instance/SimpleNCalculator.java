package org.apache.oodt.cas.workflow.instance;

import org.apache.oodt.cas.workflow.metadata.ControlMetadata;

public class SimpleNCalculator implements WorkflowConnectTaskInstance.NCalculator {

	public static final String N = "SimpleNCalculator/N";
	
	public int determineN(ControlMetadata ctrlMetadata) {
		return Integer.parseInt(ctrlMetadata.getMetadata(N));
	}

}
