
Because the Hadoop 0.21.0 jars are currently not deployed anywhere you must check out the following source code:

http://svn.apache.org/repos/asf/hadoop/common/tags/release-0.21.0
http://svn.apache.org/repos/asf/hadoop/mapreduce/tags/release-0.21.0

In the checkout directory for common run:

ant
ant mvn-install

For mapreduce run:

ant
ant mvn-install-mapred