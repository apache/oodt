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
package org.apache.oodt.cas.protocol.imaps;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.util.List;

//Apache imports
import org.apache.commons.io.FileUtils;

//OODT imports
import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.protocol.auth.BasicAuthentication;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;

//GreenMail imports
import com.icegreen.greenmail.util.DummySSLSocketFactory;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

//JUnit imports
import junit.framework.TestCase;

/**
 * Test class for {@link ImapsProtocol}.
 * 
 * @author bfoster
 */
public class TestImapsProtocol extends TestCase {

	private GreenMail gMail;
	private ImapsProtocol imapsProtocol;
	
	@Override
	public void setUp() {
  	System.setProperty("mail.imaps.socketFactory.class", DummySSLSocketFactory.class.getCanonicalName());
		System.setProperty("mail.imaps.socketFactory.fallback", "false");
		gMail = new GreenMail();
		gMail.setUser("bfoster@google.com", "password");
		gMail.start();
		ImapsProtocol.port = gMail.getImaps().getPort();
		imapsProtocol = new ImapsProtocol();
		try {
			imapsProtocol.connect("localhost", new BasicAuthentication("bfoster@google.com", "password"));
		} catch (ProtocolException e) {
			fail("Failed to connect to GreenMail IMAPS server : " + e.getMessage());
		}
		assertEquals(1, ImapsProtocol.connectCalls);
	}
	
	@Override
	public void tearDown() {
		gMail.stop();
		try {
			imapsProtocol.close();
		} catch (Exception e) {}
		assertEquals(0, ImapsProtocol.connectCalls);
	}
	
	public void testCDAndPWD() throws ProtocolException {
		assertEquals("", imapsProtocol.pwd().getPath());
		imapsProtocol.cd(new ProtocolFile("INBOX", true));
		assertEquals("/INBOX", imapsProtocol.pwd().getPath());
	}
	
	public void testLSandGET() throws ProtocolException, IOException {
		GreenMailUtil.sendTextEmailSecureTest("bfoster@google.com", "tom@bumster.org", "Test Subject", "Test Body");
		imapsProtocol.cd(new ProtocolFile("INBOX", true));
		List<ProtocolFile> emails = imapsProtocol.ls();
		assertEquals(1, emails.size());
		File bogusFile = File.createTempFile("bogus", "bogus");
		File tmpDir = new File(bogusFile.getParentFile(), "TestImapsProtocol");
		bogusFile.delete();
		tmpDir.mkdirs();
		
		File email = new File(tmpDir, "test-email");
		imapsProtocol.get(emails.get(0), email);
		String[] splitEmail = FileUtils.readFileToString(email, "UTF-8").split("\n");
		assertEquals("From: tom@bumster.org", splitEmail[0]);
		assertEquals("To: bfoster@google.com", splitEmail[1]);
		assertEquals("Subject: Test Subject", splitEmail[2]);
		// 3 is divider text (i.e. ----- ~ Message ~ -----)
		assertEquals("Test Body", splitEmail[4]);
		tmpDir.delete();
	}
	
	public void testDelete() throws ProtocolException {
		GreenMailUtil.sendTextEmailSecureTest("bfoster@google.com", "tom@bumster.org", "Test Subject", "Test Body");
		imapsProtocol.cd(new ProtocolFile("INBOX", true));
		List<ProtocolFile> emails = imapsProtocol.ls();
		assertEquals(1, emails.size());
		imapsProtocol.delete(emails.get(0));
		emails = imapsProtocol.ls();
		assertEquals(0, emails.size());
	}
}
