package org.apache.oodt.cas.workflow.engine.runner;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobStatus;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClient;
import org.apache.oodt.cas.workflow.engine.WorkflowEngineClientFactory;
import org.apache.oodt.cas.workflow.server.channel.xmlrpc.XmlRpcCommunicationChannelClientFactory;
import org.apache.oodt.cas.workflow.state.done.FailureState;
import org.apache.oodt.cas.workflow.state.done.StoppedState;
import org.apache.oodt.cas.workflow.state.done.SuccessState;
import org.apache.oodt.cas.workflow.state.running.ExecutingState;

public class HadoopTaskOutputCommitter extends OutputCommitter {

	@Override
	public void abortTask(TaskAttemptContext taskContext) throws IOException {
		new File(taskContext.getWorkingDirectory().toString()).delete();
	}

	@Override
	public void commitTask(TaskAttemptContext taskContext) throws IOException {
		new File(taskContext.getWorkingDirectory().toString()).delete();
	}

	@Override
	public boolean needsTaskCommit(TaskAttemptContext taskContext)
			throws IOException {
		return taskContext.getWorkingDirectory() != null;
	}

	@Override
	public void abortJob(JobContext jobContext, JobStatus.State state) throws IOException {
		super.abortJob(jobContext, state);
		this.cleanupJobFiles(jobContext);
		try {
			WorkflowEngineClient wmClient = this.getWorkflowClient(jobContext);
			if (state.equals(JobStatus.State.FAILED))
				wmClient.setWorkflowState(jobContext.getConfiguration().get(HadoopTaskProperties.WORKFLOW_INSTANCE_ID), jobContext.getConfiguration().get(HadoopTaskProperties.WORKFLOW_MODEL_ID), new FailureState(""));
			else if (state.equals(JobStatus.State.KILLED))
				wmClient.setWorkflowState(jobContext.getConfiguration().get(HadoopTaskProperties.WORKFLOW_INSTANCE_ID), jobContext.getConfiguration().get(HadoopTaskProperties.WORKFLOW_MODEL_ID), new StoppedState(""));
		}catch (Exception e) {
			throw new IOException();
		}
	}
	
	@Override
	public void commitJob(JobContext jobContext) throws IOException {
		super.commitJob(jobContext);
		this.cleanupJobFiles(jobContext);
		try {
			WorkflowEngineClient wmClient = this.getWorkflowClient(jobContext);
			wmClient.setWorkflowState(jobContext.getConfiguration().get(HadoopTaskProperties.WORKFLOW_INSTANCE_ID), jobContext.getConfiguration().get(HadoopTaskProperties.WORKFLOW_MODEL_ID), new SuccessState(""));
		}catch (Exception e) {
			throw new IOException();
		}
	}
	
	private void cleanupJobFiles(JobContext jobContext) throws IOException {
		try {
			Cluster cluster = new Cluster(jobContext.getConfiguration());
			for (String inputFile : jobContext.getConfiguration().get(HadoopTaskProperties.INPUT_FILES).split(","))
				cluster.getFileSystem().delete(new Path(inputFile), false);
		    for (String dependencyFile : jobContext.getConfiguration().get(HadoopTaskProperties.DEPENDENCY_FILES).split(",")) 
			    cluster.getFileSystem().delete(new Path(dependencyFile), false);
		}catch (Exception e) {
			throw new IOException("Failed to cleanup job files : " + e.getMessage(), e);
		}
	}
	
	@Override
	public void setupJob(JobContext jobContext) throws IOException {
		try {
		    Cluster cluster = new Cluster(jobContext.getConfiguration());
		    for (String inputFile : jobContext.getConfiguration().get(HadoopTaskProperties.INPUT_FILES).split(",")) {
		    	Path inputFilePath = new Path(inputFile);
			    cluster.getFileSystem().copyFromLocalFile(inputFilePath, inputFilePath);
		    }
		    for (String dependencyFile : jobContext.getConfiguration().get(HadoopTaskProperties.DEPENDENCY_FILES).split(",")) {
		    	Path dependencyFilePath = new Path(dependencyFile);
			    cluster.getFileSystem().copyFromLocalFile(dependencyFilePath, dependencyFilePath);
		    }
		    try {
		    	WorkflowEngineClient wmClient = this.getWorkflowClient(jobContext);
		    	wmClient.setWorkflowState(jobContext.getConfiguration().get(HadoopTaskProperties.WORKFLOW_INSTANCE_ID), jobContext.getConfiguration().get(HadoopTaskProperties.WORKFLOW_MODEL_ID), new ExecutingState(""));
		    }catch (Exception e) {}
		}catch (Exception e) {
			throw new IOException("Failed to setup job '" + jobContext.getJobName() + "' : " + e.getMessage(), e);
		}
	}

	@Override
	public void setupTask(TaskAttemptContext taskContext) throws IOException {
		new File(taskContext.getWorkingDirectory().toString()).mkdirs();
	}
	
	private WorkflowEngineClient getWorkflowClient(JobContext jobContext) {
	    XmlRpcCommunicationChannelClientFactory xmlrpcFactory = new XmlRpcCommunicationChannelClientFactory();
	    xmlrpcFactory.setChunkSize(1024);
	    xmlrpcFactory.setConnectionRetries(10);
	    xmlrpcFactory.setConnectionRetryIntervalSecs(30);
	    xmlrpcFactory.setConnectionTimeout(60);
	    xmlrpcFactory.setRequestTimeout(20);
	    xmlrpcFactory.setServerUrl(jobContext.getConfiguration().get(HadoopTaskProperties.WORKFLOW_URL));
	    WorkflowEngineClientFactory wengineFactory = new WorkflowEngineClientFactory();
	    wengineFactory.setAutoPagerSize(1000);
	    wengineFactory.setCommunicationChannelClientFactory(xmlrpcFactory);
	    return wengineFactory.createEngine();
	}

}
