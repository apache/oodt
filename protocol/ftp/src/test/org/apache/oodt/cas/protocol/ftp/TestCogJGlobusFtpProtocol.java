/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oodt.cas.protocol.ftp;

//JUnit imports
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

//APACHE imports
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;

//OODT imports
import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.protocol.auth.BasicAuthentication;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;

//JUnit imports
import junit.framework.TestCase;
import org.globus.ftp.FileInfo;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.spy;

/**
 * Test class for {@link CogJGlobusFtpProtocol}.
 *
 * @author bfoster
 */
public class TestCogJGlobusFtpProtocol extends TestCase {

	private static final int PORT = 9000;
	private FtpServer server;
  private static final File USERS_FILE = new File("src/testdata/users.properties");
  
	@Override
	public void setUp() throws Exception {
		assertTrue(USERS_FILE.getAbsolutePath() + " must exist",
				USERS_FILE.exists());

		FtpServerFactory serverFactory = new FtpServerFactory();

		serverFactory.setConnectionConfig(new ConnectionConfigFactory()
				.createConnectionConfig());

		ListenerFactory listenerFactory = new ListenerFactory();

		listenerFactory.setPort(PORT);

		listenerFactory
				.setDataConnectionConfiguration(new DataConnectionConfigurationFactory()
						.createDataConnectionConfiguration());

		serverFactory.addListener("default", listenerFactory.createListener());

		PropertiesUserManagerFactory umFactory = new PropertiesUserManagerFactory();
		umFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
		umFactory.setFile(USERS_FILE);

		serverFactory.setUserManager(umFactory.createUserManager());

		server = serverFactory.createServer();
		//server.start();
	}
	
	@Override
	public void tearDown() {
		server.stop();
	}
	
	public void testLSandCDandPWD() throws ProtocolException {

        CogJGlobusFtpProtocol ftpProtocol = spy(new CogJGlobusFtpProtocol(PORT));
        BasicAuthentication auth = new BasicAuthentication("anonymous", "password");

        /** Mocking server responses to prevent server failure **/

        Mockito.doReturn("testdata").when(ftpProtocol).getCurentDir();

        Vector<FileInfo> vector = new Vector<FileInfo>();
        FileInfo file = new FileInfo();
        file.setName("users.properties");
        byte b = 1;
        file.setFileType(b);
        vector.add(file);

        Mockito.doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                return null;
            }}).when(ftpProtocol).setActive();

        Mockito.doReturn(vector).when(ftpProtocol).ftpList("*", null);

        Mockito.doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                return null;
            }}).when(ftpProtocol).connect("localhost", auth);

        Mockito.doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                return null;
            }}).when(ftpProtocol).cd(new ProtocolFile("testdata", true));



        ftpProtocol.connect("localhost", auth);


		ftpProtocol.cd(new ProtocolFile("testdata", true));

		List<ProtocolFile> lsResults = ftpProtocol.ls();
		assertTrue(lsResults.contains(new ProtocolFile(ftpProtocol.pwd(), "users.properties", false)));
	}
	
}
