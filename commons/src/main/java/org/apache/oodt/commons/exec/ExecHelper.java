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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * This class is taken from Stephen Ostermiller's example at:
 * http://ostermiller.org/utils/ExecHelper.java.html
 * 
 * It provides some useful methods for manipulating and reading in the output of
 * a {@link Process} resultant from a call to {@link Runtime#getRuntime()}
 * without using the {@link Process#waitFor()} method, which apparently can hang
 * on certain underlying systems.
 * </p>
 * 
 * @author mattmann
 * 
 */
public final class ExecHelper {
  public static final int INT = 1024;
  private static Logger LOG = Logger.getLogger(ExecHelper.class.getName());

    /**
     * Executes the specified command and arguments in a separate process, and
     * waits for the process to finish.
     * <p>
     * Output from the process is expected to be text in the system's default
     * character set.
     * <p>
     * No input is passed to the process on STDIN.
     * 
     * @param cmdarray
     *            array containing the command to call and its arguments.
     * @return The results of the execution in an ExecHelper object.
     * @throws SecurityException
     *             if a security manager exists and its checkExec method doesn't
     *             allow creation of a subprocess.
     * @throws IOException -
     *             if an I/O error occurs
     * @throws NullPointerException -
     *             if cmdarray is null
     * @throws IndexOutOfBoundsException -
     *             if cmdarray is an empty array (has length 0).
     * 
     * 
     */
    public static ExecHelper exec(String[] cmdarray) throws IOException {
        return new ExecHelper(Runtime.getRuntime().exec(cmdarray), null);
    }

    /**
     * Executes the specified command and arguments in a separate process, and
     * waits for the process to finish.
     * <p>
     * Output from the process is expected to be text in the system's default
     * character set.
     * <p>
     * No input is passed to the process on STDIN.
     * 
     * @param cmdarray
     *            array containing the command to call and its arguments.
     * @param envp
     *            array of strings, each element of which has environment
     *            variable settings in format name=value.
     * @return The results of the execution in an ExecHelper object.
     * @throws SecurityException
     *             if a security manager exists and its checkExec method doesn't
     *             allow creation of a subprocess.
     * @throws IOException -
     *             if an I/O error occurs
     * @throws NullPointerException -
     *             if cmdarray is null
     * @throws IndexOutOfBoundsException -
     *             if cmdarray is an empty array (has length 0).
     * 
     * 
     */
    public static ExecHelper exec(String[] cmdarray, String[] envp)
            throws IOException {
        return new ExecHelper(Runtime.getRuntime().exec(cmdarray, envp), null);
    }

    /**
     * Executes the specified command and arguments in a separate process, and
     * waits for the process to finish.
     * <p>
     * Output from the process is expected to be text in the system's default
     * character set.
     * <p>
     * No input is passed to the process on STDIN.
     * 
     * @param cmdarray
     *            array containing the command to call and its arguments.
     * @param envp
     *            array of strings, each element of which has environment
     *            variable settings in format name=value.
     * @param dir
     *            the working directory of the subprocess, or null if the
     *            subprocess should inherit the working directory of the current
     *            process.
     * @return The results of the execution in an ExecHelper object.
     * @throws SecurityException
     *             if a security manager exists and its checkExec method doesn't
     *             allow creation of a subprocess.
     * @throws IOException -
     *             if an I/O error occurs
     * @throws NullPointerException -
     *             if cmdarray is null
     * @throws IndexOutOfBoundsException -
     *             if cmdarray is an empty array (has length 0).
     * 
     * 
     */
    public static ExecHelper exec(String[] cmdarray, String[] envp, File dir)
            throws IOException {
        return new ExecHelper(Runtime.getRuntime().exec(cmdarray, envp, dir),
                null);
    }

    /**
     * Executes the specified command and arguments in a separate process, and
     * waits for the process to finish.
     * <p>
     * No input is passed to the process on STDIN.
     * 
     * @param cmdarray
     *            array containing the command to call and its arguments.
     * @param charset
     *            Output from the executed command is expected to be in this
     *            character set.
     * @return The results of the execution in an ExecHelper object.
     * @throws SecurityException
     *             if a security manager exists and its checkExec method doesn't
     *             allow creation of a subprocess.
     * @throws IOException -
     *             if an I/O error occurs
     * @throws NullPointerException -
     *             if cmdarray is null
     * @throws IndexOutOfBoundsException -
     *             if cmdarray is an empty array (has length 0).
     * 
     * 
     */
    public static ExecHelper exec(String[] cmdarray, String charset)
            throws IOException {
        return new ExecHelper(Runtime.getRuntime().exec(cmdarray), charset);
    }

    /**
     * Executes the specified command and arguments in a separate process, and
     * waits for the process to finish.
     * <p>
     * No input is passed to the process on STDIN.
     * 
     * @param cmdarray
     *            array containing the command to call and its arguments.
     * @param envp
     *            array of strings, each element of which has environment
     *            variable settings in format name=value.
     * @param charset
     *            Output from the executed command is expected to be in this
     *            character set.
     * @return The results of the execution in an ExecHelper object.
     * @throws SecurityException
     *             if a security manager exists and its checkExec method doesn't
     *             allow creation of a subprocess.
     * @throws IOException -
     *             if an I/O error occurs
     * @throws NullPointerException -
     *             if cmdarray is null
     * @throws IndexOutOfBoundsException -
     *             if cmdarray is an empty array (has length 0).
     * 
     * 
     */
    public static ExecHelper exec(String[] cmdarray, String[] envp,
            String charset) throws IOException {
        return new ExecHelper(Runtime.getRuntime().exec(cmdarray, envp),
                charset);
    }

    /**
     * Executes the specified command and arguments in a separate process, and
     * waits for the process to finish.
     * <p>
     * No input is passed to the process on STDIN.
     * 
     * @param cmdarray
     *            array containing the command to call and its arguments.
     * @param envp
     *            array of strings, each element of which has environment
     *            variable settings in format name=value.
     * @param dir
     *            the working directory of the subprocess, or null if the
     *            subprocess should inherit the working directory of the current
     *            process.
     * @param charset
     *            Output from the executed command is expected to be in this
     *            character set.
     * @return The results of the execution in an ExecHelper object.
     * @throws SecurityException
     *             if a security manager exists and its checkExec method doesn't
     *             allow creation of a subprocess.
     * @throws IOException -
     *             if an I/O error occurs
     * @throws NullPointerException -
     *             if cmdarray is null
     * @throws IndexOutOfBoundsException -
     *             if cmdarray is an empty array (has length 0).
     * 
     * 
     */
    public static ExecHelper exec(String[] cmdarray, String[] envp, File dir,
            String charset) throws IOException {
        return new ExecHelper(Runtime.getRuntime().exec(cmdarray, envp, dir),
                charset);
    }

    /**
     * Executes the specified command using a shell. On windows uses cmd.exe or
     * command.exe. On other platforms it uses /bin/sh.
     * <p>
     * A shell should be used to execute commands when features such as file
     * redirection, pipes, argument parsing are desired.
     * <p>
     * Output from the process is expected to be text in the system's default
     * character set.
     * <p>
     * No input is passed to the process on STDIN.
     * 
     * @param command
     *            String containing a command to be parsed by the shell and
     *            executed.
     * @return The results of the execution in an ExecHelper object.
     * @throws SecurityException
     *             if a security manager exists and its checkExec method doesn't
     *             allow creation of a subprocess.
     * @throws IOException -
     *             if an I/O error occurs
     * @throws NullPointerException -
     *             if command is null
     * 
     * 
     */
    public static ExecHelper execUsingShell(String command) throws IOException {
        return execUsingShell(command, null);
    }

    /**
     * Executes the specified command using a shell. On windows uses cmd.exe or
     * command.exe. On other platforms it uses /bin/sh.
     * <p>
     * A shell should be used to execute commands when features such as file
     * redirection, pipes, argument parsing are desired.
     * <p>
     * No input is passed to the process on STDIN.
     * 
     * @param command
     *            String containing a command to be parsed by the shell and
     *            executed.
     * @param charset
     *            Output from the executed command is expected to be in this
     *            character set.
     * @return The results of the execution in an ExecHelper object.
     * @throws SecurityException
     *             if a security manager exists and its checkExec method doesn't
     *             allow creation of a subprocess.
     * @throws IOException -
     *             if an I/O error occurs
     * @throws NullPointerException -
     *             if command is null
     * 
     * 
     */
    public static ExecHelper execUsingShell(String command, String charset)
            throws IOException {
        if (command == null) {
          throw new NullPointerException();
        }
        String[] cmdarray;
        String os = System.getProperty("os.name");
        if (os.equals("Windows 95") || os.equals("Windows 98")
                || os.equals("Windows ME")) {
            cmdarray = new String[] { "command.exe", "/C", command };
        } else if (os.startsWith("Windows")) {
            cmdarray = new String[] { "cmd.exe", "/C", command };
        } else {
            cmdarray = new String[] { "/bin/sh", "-c", command };
        }
        return new ExecHelper(Runtime.getRuntime().exec(cmdarray), charset);
    }

    /**
     * Take a process, record its standard error and standard out streams, wait
     * for it to finish
     * 
     * @param process
     *            process to watch
     * @throws SecurityException
     *             if a security manager exists and its checkExec method doesn't
     *             allow creation of a subprocess.
     * @throws IOException -
     *             if an I/O error occurs
     * @throws NullPointerException -
     *             if cmdarray is null
     * @throws IndexOutOfBoundsException -
     *             if cmdarray is an empty array (has length 0).
     * 
     * 
     */
    private ExecHelper(Process process, String charset) throws IOException {
    	try {
	        StringBuilder output = new StringBuilder();
	        StringBuilder error = new StringBuilder();
	
	        Reader stdout;
	        Reader stderr;
	
	        if (charset == null) {
	            // This is one time that the system charset is appropriate,
	            // don't specify a character set.
	            stdout = new InputStreamReader(process.getInputStream());
	            stderr = new InputStreamReader(process.getErrorStream());
	        } else {
	            stdout = new InputStreamReader(process.getInputStream(), charset);
	            stderr = new InputStreamReader(process.getErrorStream(), charset);
	        }
	        char[] buffer = new char[INT];
	
	        boolean done = false;
	        boolean stdoutclosed = false;
	        boolean stderrclosed = false;
	        while (!done) {
	            boolean readSomething = false;
	            // read from the process's standard output
	            if (!stdoutclosed && stdout.ready()) {
	                readSomething = true;
	                int read = stdout.read(buffer, 0, buffer.length);
	                if (read < 0) {
	                    readSomething = true;
	                    stdoutclosed = true;
	                } else if (read > 0) {
	                    readSomething = true;
		                    output.append(buffer, 0, read);
	                }
	            }
	            // read from the process's standard error
	            if (!stderrclosed && stderr.ready()) {
	                int read = stderr.read(buffer, 0, buffer.length);
	                if (read < 0) {
	                    readSomething = true;
	                    stderrclosed = true;
	                } else if (read > 0) {
	                    readSomething = true;
	                    error.append(buffer, 0, read);
	                }
	            }
	            // Check the exit status only we haven't read anything,
	            // if something has been read, the process is obviously not dead
	            // yet.
	            if (!readSomething) {
	                try {
	                    this.status = process.exitValue();
	                    done = true;
	                } catch (IllegalThreadStateException itx) {
	                    // Exit status not ready yet.
	                    // Give the process a little breathing room.
	                    try {
	                        Thread.sleep(100);
	                    } catch (InterruptedException ix) {
	                        process.destroy();
	                        throw new IOException("Interrupted - processes killed");
	                    }
	                }
	            }
	        }
	
	        this.output = output.toString();
	        this.error = error.toString();
    	}catch (Exception e) {
    		LOG.log(Level.SEVERE, e.getMessage());
    		throw new IOException("Process exec failed : " + e.getMessage());
    	}finally {
            try {
            	process.getErrorStream().close();
            } catch (Exception ignored) {}
            try {
               	process.getInputStream().close();
            } catch (Exception ignored) {}
            try {
            	process.getOutputStream().close();
            } catch (Exception ignored) {}
    	}
    }

    /**
     * The output of the job that ran.
     * 
     * 
     */
    private String output;

    /**
     * Get the output of the job that ran.
     * 
     * @return Everything the executed process wrote to its standard output as a
     *         String.
     * 
     * 
     */
    public String getOutput() {
        return output;
    }

    /**
     * The error output of the job that ran.
     * 
     * 
     */
    private String error;

    /**
     * Get the error output of the job that ran.
     * 
     * @return Everything the executed process wrote to its standard error as a
     *         String.
     * 
     * 
     */
    public String getError() {
        return error;
    }

    /**
     * The status of the job that ran.
     * 
     * 
     */
    private int status;

    /**
     * Get the status of the job that ran.
     * 
     * @return exit status of the executed process, by convention, the value 0
     *         indicates normal termination.
     * 
     * 
     */
    public int getStatus() {
        return status;
    }
}
