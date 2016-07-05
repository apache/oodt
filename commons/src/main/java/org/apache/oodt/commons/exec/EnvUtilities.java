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

//JDK imports
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @author bfoster
 * @author mstarch
 * @version $Revision$
 * 
 * <p>
 * Environment Utilities for reading variables and setting them and such.
 * </p>.
 */
public final class EnvUtilities {

    private static final Logger LOG = Logger.getLogger(EnvUtilities.class
            .getName());
    public static final int INT = 4096;

    private EnvUtilities() throws InstantiationException {
        throw new InstantiationException("Don't construct utility classes!");
    }
    
    private final static Properties environment = new Properties();
    static{
     System.getenv().entrySet();
      for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
        environment.setProperty(entry.getKey(), entry.getValue());
      }
    }

    /**
     * This method returns a particular named environment variable from the user's
     * working environment using the {@link #getEnv()} method to grab that environment.
     * 
     * @param envVarName The named environment variable to return.
     * @return The environment variable value, as a String.
     */
    public static String getEnv(String envVarName) {
        return environment.getProperty(envVarName);
    }

    /**
     * This method does exactly the same thing as {@link #getEnvUsingWaitFor()},
     * except it uses System.getenv()
     * 
     * @return The user's current environment, in a {@link Properties} object.
     */
    public static Properties getEnv() {
        return environment;
    }

    /**
     * This method grabs the current environment using the Linux shell program
     * called <code>env</code>. Unfortunately, this method also uses
     * {@link Process#waitFor()}, which seems to hang when folks have weird
     * environment variables and such.
     * 
     * A more appropriate method to call is {@link #getEnv()} because it uses
     * the {@link ExecHelper} class, which seems to handle process termination a
     * bit more elegantly.
     * 
     * @deprecated
     * @return A {@link Properites} object containing the user's current
     *         environment variables.
     */
    public static Properties getEnvUsingWaitFor() {
        String commandLine = "env";

        Process p = null;
        Properties envProps = null;

        try {
            p = Runtime.getRuntime().exec(commandLine);
            int retVal = p.waitFor();
            envProps = new Properties();
            envProps.load(preProcessInputStream(p.getInputStream()));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Error executing env command: Message: "
                    + e.getMessage());
        } finally {
            try {
                if (p.getErrorStream() != null) {
                    p.getErrorStream().close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (p.getInputStream() != null) {
                    p.getInputStream().close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (p.getOutputStream() != null) {
                    p.getOutputStream().close();
                }
            } catch (Exception ignored) {
            }
        }

        return envProps;
    }

    /**
     * This method turns an {@link InputStream} into a String. Method taken
     * from:<br>
     * 
     * <a
     * href="http://snippets.dzone.com/posts/show/555">http://snippets.dzone.com/posts/show/555</a>.
     * 
     * @param in
     *            The {@link InputStream} to Stringify.
     * @return A String constructed from the given {@link InputStream}.
     * @throws IOException
     *             If any error occurs.
     */
    public static String slurp(InputStream in) throws IOException {
        StringBuilder out = new StringBuilder();
        byte[] b = new byte[INT];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    protected static InputStream preProcessInputStream(InputStream is)
        throws IOException {
        // basically read this sucker into a BufferedReader
        // line by line, and replaceAll on \ with \\
        // so \\\\ with \\\\\\\\
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder buf = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            // fix the line
            line = line.replaceAll("\\\\", "\\\\\\\\");
            buf.append(line).append("\n");
        }

        try {
            reader.close();
        } catch (Exception ignore) {
        }

        return new ByteArrayInputStream(buf.toString().getBytes());
    }

}
