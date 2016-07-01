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


package org.apache.oodt.commons.exec;

/**
 * @author mattmann
 * @version $Revision$
 *
 * <p>This class is a Utility class for dealing with java <code>Process</code> streams. It was
 * taken from the site: 
 * <a href="http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html">http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html</a>, 
 * and was written by Michael Daconta.</p>
 *
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StreamGobbler extends Thread {
	
	private static final Logger LOG = Logger.getLogger(StreamGobbler.class.getName());
	
    InputStream is;

    String type;

    OutputStream os;

    private boolean running = true;

    public StreamGobbler(InputStream is, String type) {
        this(is, type, null);
    }

    public StreamGobbler(InputStream is, String type, OutputStream redirect) {
        this.is = is;
        this.type = type;
        this.os = redirect;
    }

    public void run() {
        try {
            PrintWriter pw = null;
            if (os != null) {
                pw = new PrintWriter(os);
            }

            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null && this.running) {
                if (pw != null) {
                    pw.println(this.type + ": " + line);
                }
            }
            if (pw != null) {
                pw.flush();
            }
        } catch (IOException ioe) {
        	LOG.log(Level.FINEST, "StreamGobbler failed while gobbling : " + ioe.getMessage(), ioe);
        }
    }

    public void stopGobblingAndDie() {
        this.running = false;
    }
}
