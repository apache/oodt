package org.apache.oodt.cas.resource.system;

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.resource.structs.exceptions.JobRepositoryException;
import org.apache.oodt.cas.resource.structs.exceptions.MonitorException;
import org.apache.oodt.cas.resource.structs.exceptions.QueueManagerException;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Created by bugg on 05/11/15.
 */
public class TestXmlRpcResourceManagerClient {

  private static final int RM_PORT = 50001;

  private static XmlRpcResourceManager rm;
  private static File thetmpPolicyDir;

  @BeforeClass
  public static void setUp() throws Exception {
    generateTestConfiguration();
    rm = new XmlRpcResourceManager(RM_PORT);
  }

  private static void generateTestConfiguration() throws IOException {
    Properties config = new Properties();

    String propertiesFile = "." + File.separator + "src" + File.separator +
                            "test" + File.separator + "resources" + File.separator + "test.resource.properties";
    System.getProperties().load(new FileInputStream(new File(propertiesFile)));

    // stage policy
    File tmpPolicyDir = null;
    try {
      tmpPolicyDir = File.createTempFile("test", "ignore").getParentFile();
    } catch (Exception e) {
      fail(e.getMessage());
    }
    for (File policyFile : new File("./src/test/resources/policy")
        .listFiles(new FileFilter() {

          @Override
          public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().endsWith(".xml");
          }
        })) {
      try {
        FileUtils.copyFileToDirectory(policyFile, tmpPolicyDir);
      } catch (Exception e) {
        fail(e.getMessage());
      }
    }

    config.setProperty("org.apache.oodt.cas.resource.nodes.dirs", tmpPolicyDir
        .toURI().toString());
    config.setProperty("org.apache.oodt.cas.resource.nodetoqueues.dirs",
        tmpPolicyDir.toURI().toString());

    System.getProperties().putAll(config);
    thetmpPolicyDir = tmpPolicyDir;
  }

  @Test
  public void testGetNodes() throws MonitorException {
    List<Hashtable> nodes = rm.getNodes();

    assertThat(nodes, is(not(nullValue())));
    assertThat(nodes, hasSize(1));

  }

  @Test
  public void testGetExecutionReport() throws JobRepositoryException {

    String execreport = rm.getExecutionReport();


    assertThat(execreport, is(not(nullValue())));
    //TODO make it return more than an empty string;
  }


  @Test
  public void testJobQueueCapacity() throws JobRepositoryException {
    int capacity = rm.getJobQueueCapacity();

    assertThat(capacity, equalTo(1000));

  }

  @Test
  public void testGetJobQueueSize() throws JobRepositoryException {
    int size = rm.getJobQueueSize();

    assertThat(size, equalTo(0));

    //TODO Make it change queue size

  }

  @Test
  public void testGetNodeById() throws MonitorException {
    List<Hashtable> nodelist = rm.getNodes();

    Map node = rm.getNodeById((String) nodelist.get(0).get("node.id"));

    assertThat(node, is(not(nullValue())));

    assertThat((String)node.get("node.id"), equalTo("localhost"));
  }


  @Test
  public void testGetNodeLoad() throws MonitorException {

    List<Hashtable> nodelist = rm.getNodes();

    String node = rm.getNodeLoad((String) nodelist.get(0).get("node.id"));

    assertNotNull(node);

    assertThat(node, equalTo("0/8"));

  }

  @Test
  public void testNodeReport() throws MonitorException {
    String report = rm.getNodeReport();

    assertThat(report, is(not(nullValue())));
  }

  @Test
  public void testGetNodesInQueue() throws QueueManagerException {
    List<String> nodes = rm.getNodesInQueue("long");

    assertThat(nodes, is(not(nullValue())));

    assertThat(nodes, hasSize(1));

  }


  @Test
  public void testQueuedJobs(){
    List jobs = rm.getQueuedJobs();

    assertThat(jobs, is(not(nullValue())));

    //TODO queue a job
  }

  @Test
  public void testQueuesWithNode() throws MonitorException {
    List<Hashtable> nodelist = rm.getNodes();


    List<String> queues = rm.getQueuesWithNode((String) nodelist.get(0).get("node.id"));
    assertThat(queues, hasSize(3));

    assertThat(queues, containsInAnyOrder("high", "quick", "long"));
  }

  @Test
  public void testQueues(){
    List<String> queues = rm.getQueues();

    assertThat(queues, hasSize(3));

    assertThat(queues, containsInAnyOrder("high", "quick", "long"));
  }

}
