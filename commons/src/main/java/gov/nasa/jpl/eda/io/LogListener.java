// This software was developed by the the Jet Propulsion Laboratory, an operating division
// of the California Institute of Technology, for the National Aeronautics and Space
// Administration, an independent agency of the United States Government.
// 
// This software is copyrighted (c) 2000 by the California Institute of Technology.  All
// rights reserved.  For a key to the seals in the Athenaeum dining hall, see the south
// wall.
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
// $Id: LogListener.java,v 1.1.1.1 2004-02-28 13:09:14 kelly Exp $

package jpl.eda.io;

import java.util.EventObject;

/** Listener for logging events.
 *
 * Objects of classes that implement this interface are notified when messages are logged
 * and when other logging events occur.
 *
 * @see Log
 * @see LogEvent
 * @author Kelly
 */
public interface LogListener extends java.beans.PropertyChangeListener {
	/** A message got logged.
	 *
	 * The <var>event</var> contains the details of the message, including
	 * 
	 * <ul>
	 *   <li>The timestamp of the message, from {@link LogEvent#getTimestamp}.</li>
	 *   <li>The source of the message, from {@link EventObject#getSource}, which is always
	 *     a {@link String}.</li>
	 *   <li>The category of the message, from {@link LogEvent#getCategory}.</li>
	 *   <li>The message text, from {@link LogEvent#getMessage}.</li>
	 * </ul>
	 *
	 * @param event The logging event.
	 */
	void messageLogged(LogEvent event);

	/** A stream got started.
	 *
	 * The <var>event</var> contains the details of stream start, including 
	 *
	 * <ul>
	 *   <li>The name of the stream, from {@link LogEvent#getStream}.</li>
	 *   <li>The time the stream got started, from {@link LogEvent#getTimestamp}</li>
	 *   <li>The source of the new stream start, from {@link EventObject#getSource} (always a {@link String}).</li>
	 * </ul>
	 *
	 * @param event The logging event.
	 */
	void streamStarted(LogEvent event);

	/** A stream was stopped.
	 *
	 * The <var>event</var> contains the detail of the stream stop, which is the name
	 * of the stream, from {@link LogEvent#getStream} or {@link EventObject#getSource}
	 * (always a {@link String}).
	 *
	 * @param event The logging event.
	 */
	void streamStopped(LogEvent event);
}
