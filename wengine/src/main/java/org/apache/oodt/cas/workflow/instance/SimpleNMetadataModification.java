package org.apache.oodt.cas.workflow.instance;

import org.apache.oodt.cas.metadata.Metadata;

public class SimpleNMetadataModification implements WorkflowConnectTaskInstance.NMetadataModification {

	public static final String I = "SimpleNMetadataModification/I";
	public static final String N = "SimpleNMetadataModification/N";
	
	public void prepare(int i, int n, Metadata metadata) {
		metadata.replaceMetadata(I, Integer.toString(i));
		metadata.replaceMetadata(N, Integer.toString(n));
	}

}
