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

package org.apache.oodt.commons;

import java.io.*;

/** An executable object.
 *
 * Objects of this class are programs that can be executed asynchronously.  Their standard
 * output and standard error streams are shared with the parent process' standard output
 * and error streams.
 *
 * @author Kelly
 */
public abstract class Executable {

  public static final int INT = 1024;

  /** Construct an executable.
	 */
	public Executable() {}

	/** Get the command-line.
	 *
	 * @return Command-line.
	 */
	protected abstract String[] getCommandLine();

	/** Start executing this program.
	 *
	 * If any error occurs while launching the program, this method notes a message to
	 * stderr, but otherwise takes no further action.
	 */
	public void execute() {
		// We force program execution to be asynchronous by cutting a new thread.
		new Thread() {
			public void run() {
				try {
					process = Runtime.getRuntime().exec(getCommandLine());
					
					// Read all output (standard and error output)
					// from the process and pass it to the user.
					redirect(process.getErrorStream(), System.err);
					redirect(process.getInputStream(), System.out);
				} catch (IOException ex) {
					System.err.println("Can't execute command \"" + getCommandLine()[0] + "\": "
						+ ex.getMessage());
				}
			}

			/** Redirect the given input onto the given output.
			 *
			 * @param in The stream to read.
			 * @param out Where to write it.
			 */
			private void redirect(final InputStream in, final OutputStream out) {
				// Do this in the background, too.
				new Thread() {
					public void run() {
						try {
							byte[] buf = new byte[INT];
							for (;;) {
								int numRead = in.read(buf);
								if (numRead == -1) {
									in.close();
									break;
								}
								out.write(buf, 0, numRead);
							}
						} catch (IOException ex) {
							try {
								in.close();
							} catch (IOException ignore) {}
						}
					}
				}.start();
			}
		}.start();

		// Spin until the process field is set.
		while (process == null) {
		  Thread.yield();
		}
	}

	/** Wait for the process to terminate.
	 *
	 * @throws InterruptedException If the thread waiting for the termination is interrupted.
	 * @return The exit value of the process.
	 */
	public int waitFor() throws InterruptedException {
		return process.waitFor();
	}

	/** Terminate this process.
	 */
	public void terminate() {
		process.destroy();
	}

	/** The process backing this executable. */
	protected Process process;

}
