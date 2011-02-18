package org.apache.oodt.cas.workflow.engine.runner;

import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.instance.TaskInstance;

import com.thoughtworks.xstream.XStream;

public class HadoopRunner extends EngineRunner {

	public static final String DEPENDENCY_FILE_KEYS = "HadoopRunner/DependencyFileKeys";
	public static final String INPUT_FILE_KEYS = "HadoopRunner/InputFileKeys";
	
	@Override
	public void execute(TaskInstance workflowInstance) throws Exception {
		Metadata instanceMetadata = workflowInstance.getMetadata();
	    Configuration conf = new Configuration();
	    Cluster cluster = new Cluster(conf);
	    Job job = Job.getInstance(cluster);
	    job.setJobName(workflowInstance.getModelId());
	    job.setJarByClass(HadoopRunner.class);

	    //setup input files
	    List<String> inputFiles = this.getInputFiles(instanceMetadata);
	    for (String inputFile : inputFiles)
	    	FileInputFormat.addInputPath(job, new Path(inputFile));
    	conf.set(HadoopTaskProperties.INPUT_FILES, StringUtils.join(inputFiles, ","));
    	
    	//setup deps files
	    List<String> dependencyFiles = this.getDependencyFiles(instanceMetadata);
	    for (String dependencyFile : dependencyFiles) 
	    	job.addCacheFile(new Path(dependencyFile).toUri());
    	conf.set(HadoopTaskProperties.DEPENDENCY_FILES, StringUtils.join(dependencyFiles, ","));

		if (workflowInstance instanceof HadoopMapReduceable) {
			job.setMapperClass((Class<? extends Mapper>) Class.forName(((HadoopMapReduceable) workflowInstance).getMapperClass()));
			job.setCombinerClass((Class<? extends Reducer>) Class.forName(((HadoopMapReduceable) workflowInstance).getCombinerClass()));
			job.setReducerClass((Class<? extends Reducer>) Class.forName(((HadoopMapReduceable) workflowInstance).getReducerClass()));
			job.setNumReduceTasks(((HadoopMapReduceable) workflowInstance).getNumOfReducers());
			job.setOutputFormatClass(HadoopTaskOutputFormat.class);
		}else {
			job.setMapperClass(HadoopTaskInstance.class);
			job.setNumReduceTasks(0);
			XStream xstream = new XStream();
			conf.set(HadoopTaskInstance.XSTREAM_TASK, xstream.toXML(workflowInstance));
		}
		
		job.submit();
	}
	
	private List<String> getInputFiles(Metadata metadata) throws Exception {
		Vector<String> inputFiles = new Vector<String>();
		for (String inputFileKey : metadata.getMetadata(INPUT_FILE_KEYS).split(","))
			inputFiles.addAll(metadata.getAllMetadata(inputFileKey));
		return inputFiles;
	}
	
	private List<String> getDependencyFiles(Metadata metadata) throws Exception {
		Vector<String> dependencyFiles = new Vector<String>();
		for (String dependencyFileKey : metadata.getMetadata(DEPENDENCY_FILE_KEYS).split(","))
			dependencyFiles.addAll(metadata.getAllMetadata(dependencyFileKey));
		return dependencyFiles;
	}
	
	@Override
	public int getOpenSlots(TaskInstance workflowInstance) throws Exception {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean hasOpenSlots(TaskInstance workflowInstance) throws Exception {
		return true;
	}
	
	@Override
	public void shutdown() throws Exception {
		
	}

//	public static class MapperTask extends Mapper<Object, Text, Text, File> {
//		private Text outputFilesText = new Text("OutputFiles");
//
//		public void map(Object key, Text value, Context context)
//				throws IOException, InterruptedException {
//			Path[] localFiles = context.getLocalCacheFiles();
//			File outputFile = new File("/path/to/output/file");
//			int returnValue = ExecUtils.callProgram("/usr/bin/time -v -o log/runtime_" + value.toString() + ".txt  /path/to/exe config/${config_type}/l2_fp.config output/l2_" + value.toString() + ".h5) > log/l2_" + value.toString() + ".log.running 2>&1", (File) null);
//			ExecUtils.callProgram("mv -f log/l2_" + value.toString() + ".log.running log/l2_" + value.toString() + ".log", (File) null);
//			context.write(outputFilesText, outputFile);
//		}
//	}
//
//	public static class ReducerTask extends Reducer<Text, File, Text, File> {
//		private Text outputFileText = new Text("OutputFile");
//
//		public void reduce(Text key, Iterable<File> values,
//				Context context) throws IOException, InterruptedException {
//			File outputFile = new File("/path/to/output/concat/file");
//			context.write(outputFileText, outputFile);
//		}
//	}
	
}
