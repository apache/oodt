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

package org.apache.oodt.cas.resource.monitor.utils;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author rajith
 * @author mattmann
 * @version $Revision$
 *
 * Ganglia meta daemon mock server
 */
public class MockGmetad implements Runnable {

    private int socket;
    private File fakeXMLDump;
    private boolean testFinished;

    public MockGmetad(int socket, String filePath){
        this.socket = socket;
        this.fakeXMLDump = new File(filePath);
        this.testFinished = false;
    }

    public void stop(){
        testFinished = true;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(socket);
            FileInputStream fis = null;
            OutputStream os = null;


            while (!testFinished) {
                Socket sock = serverSocket.accept();
                try {
                    byte[] xmlByteArray = new byte[1024];
                    fis = new FileInputStream(fakeXMLDump);
                    os = sock.getOutputStream();

                    int count;
                    while ((count = fis.read(xmlByteArray)) >= 0) {
                        os.write(xmlByteArray, 0, count);
                    }
                    os.flush();
                } finally {
                    assert fis != null;
                    fis.close();
                    assert os != null;
                    os.close();
                    sock.close();
                }
            }
        } catch (FileNotFoundException ignored) {
            //Exception ignored
        } catch (IOException ignored) {
            //Exception ignored
        }

    }
    
    public static void main(String [] args){
    	String xmlPath;
    	int serverPort;
    	final String usage = "java MockGmetad <xml path> <port>\n";
    	
    	if (args.length != 2){
    		System.err.println(usage);
    		System.exit(1);
    	}
    	
    	xmlPath = args[0];
    	serverPort = Integer.valueOf(args[1]);
    	
    	MockGmetad gmetad = new MockGmetad(serverPort, xmlPath);
    	ThreadLocal<MockGmetad> mockGmetad = new ThreadLocal<MockGmetad>();
    	mockGmetad.set(gmetad);
    	Thread mockGmetadServer = new Thread(mockGmetad.get());
        mockGmetadServer.start();
    }
}
