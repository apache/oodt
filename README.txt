
Because the Hadoop 0.21.0 jars are currently not deployed 
anywhere, you must install the hadoop source. The source
has been checked in for your convenience in the hadoop-src
directory. To install this code into your maven repo:

In common-0.21.0:
ant mvn-install

Then in mapreduce-0.21.0:
ant mvn-install-mapred

