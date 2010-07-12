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
// $Id: LogFilter.java,v 1.1.1.1 2004-02-28 13:09:14 kelly Exp $

package jpl.eda.io;

import java.util.Hashtable;

/** A filter for log messages.
 *
 * This is a {@link LogListener} that filters out and passes through certain categories of
 * log messages to another <code>LogListener</code>.  When you construct this listener,
 * you pass a boolean flag that indicates its pass-through mode: if true, it passes log
 * messages by default and filters out specified categories; if false, it filters out
 * messages by default and passes through specified categories.
 *
 * <p>Note that this filter only affects events sent to {@link LogListener#messageLogged}.
 * Events sent to {@link LogListener#streamStarted} and {@link LogListener#streamStopped}
 * are passed through regardless.
 *
 * <p>Categories used by this filter should implement their {@link Object#hashCode} and
 * {@link Object#equals} methods.
 *
 * @see Log
 * @author Kelly
 */
public class LogFilter implements LogListener {
	/** Create a log filter.
	 *
	 * The log filter passes through and filters out messages before sending them onto
	 * the given log listener.
	 *
	 * @param listener What object will get filtered log messages.
	 * @param passThrough If true, pass messages through by default and filter out the
	 * specified <var>categories</var> of messages. If false, filter out messages by
	 * default and pass through the specified <var>categories</var>.
	 * @param categories Categories of messages to filter out (if
	 * <var>passThrough</var> is true) or to pass through (if <var>passThrough</var>
	 * is false).
	 */
	public LogFilter(LogListener listener, boolean passThrough, Object[] categories) {
		if (listener == null)
			throw new IllegalArgumentException("Can't filter messages to a null listener");
		this.listener = listener;
		this.passThrough = passThrough;
		if (categories == null) return;
		for (int i = 0; i < categories.length; ++i)
			this.categories.put(categories[i], DUMMY);
	}

	/** Create a log filter.
	 *  
	 * The log filter passes through and filters out messages before sending them onto
	 * the given log listener. The filter starts out empty (with no categories).
	 *
	 * @param listener What object will get filtered log messages.
	 * @param passThrough If true, pass messages through by default. If false, filter out messages by default.
	 */
	public LogFilter(LogListener listener, boolean passThrough) {
		this(listener, passThrough, /*categories*/null);
	}

	/** Add an additional category.
	 *
	 * If the filter is in pass-through mode, messages in this category will be
	 * filtered out. If the filter is in filter mode, messages in this category will
	 * be passed through.
	 *
	 * @param category The category to add.
	 */
	public void addCategory(Object category) {
		categories.put(category, DUMMY);
	}

	/** Remove a category.
	 *
	 * If the category isn't in the filter, nothing happens.
	 *
	 * @param category The category to remove.
	 */
	public void removeCategory(Object category) {
		categories.remove(category);
	}

	/** Pass on the event unmodified to the registered listener.
	 *
	 * @param event The event to pass.
	 */
	public void streamStarted(LogEvent event) {
		listener.streamStarted(event);
	}

	/** Pass on the event unmodified to the registered listener.
	 *
	 * @param event The event to pass.
	 */
	public void streamStopped(LogEvent event) {
		listener.streamStopped(event);
	}

	/** Filter the event, and possibly pass it onto the registered listener.
	 *
	 * @param event The event to filter.
	 */
	public void messageLogged(LogEvent event) {
		boolean found = categories.containsKey(event.getCategory());
		if ((passThrough && !found) || (!passThrough && found))
			listener.messageLogged(event);
	}

	/** Ignore this event.
	 */
	public void propertyChange(java.beans.PropertyChangeEvent ignore) {}

	/** If true, pass through by default, otherwise filter out by default.
	 */
	protected boolean passThrough;

	/** Table of categories to filter/pass.
	 *
	 * This table maps all values to {@link #DUMMY}.  In Java2, we can get rid of
	 * <code>DUMMY</code> and use a {@link java.util.HashSet} instead.
	 */
	protected Hashtable categories = new Hashtable();

	/** The DUMMY value for all mappings in the {@link #categories} table.
	 */
	protected static final Object DUMMY = new Object();

	/** The listener on whose behalf we filter.
	 */
	protected LogListener listener;
}
