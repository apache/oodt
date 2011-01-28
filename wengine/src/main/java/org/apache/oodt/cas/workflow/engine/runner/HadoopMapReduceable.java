package org.apache.oodt.cas.workflow.engine.runner;

public interface HadoopMapReduceable {

	public String getMapperClass();
	
	public String getCombinerClass();
	
	public String getReducerClass();
	
	public int getNumOfReducers();
	
}
