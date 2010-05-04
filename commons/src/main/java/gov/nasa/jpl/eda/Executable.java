// This software was developed by the Object Oriented Data Technology task of the Science
// Data Engineering group of the Engineering and Space Science Directorate of the Jet
// Propulsion Laboratory of the National Aeronautics and Space Administration, an
// independent agency of the United States Government.
// 
// This software is copyrighted (c) 2000 by the California Institute of Technology.  All
// rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification, is not
// permitted under any circumstance without prior written permission from the California
// Institute of Technology.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
// THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// $Id: Executable.java,v 1.1.1.1 2004-02-28 13:09:13 kelly Exp $

package jpl.eda;

import java.io.*;
import java.util.*;

/** An executable object.
 *
 * Objects of this class are programs that can be executed asynchronously.  Their standard
 * output and standard error streams are shared with the parent process' standard output
 * and error streams.
 *
 * @author Kelly
 */
public abstract class Executable {
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
							byte[] buf = new byte[1024];
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
		while (process == null)
			Thread.yield();
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
