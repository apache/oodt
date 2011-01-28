package org.apache.oodt.cas.workflow.engine.runner;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.oodt.cas.workflow.instance.TaskInstance;

import com.thoughtworks.xstream.XStream;

public class HadoopTaskInstance extends Mapper<Text, Text, Text, File> {

	public static final String XSTREAM_TASK = "hadoop.cas.xstream.task";
	
	public void map(Text key, Text value, Context context)
		throws IOException, InterruptedException {
		XStream xstream = new XStream();
		TaskInstance taskInstance = (TaskInstance) xstream.fromXML(context.getConfiguration().get(XSTREAM_TASK));
		taskInstance.execute();
	}
	
}
