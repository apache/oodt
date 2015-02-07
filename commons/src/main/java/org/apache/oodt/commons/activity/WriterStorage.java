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

package org.apache.oodt.commons.activity;

import java.util.List;
import java.io.Writer;
import java.io.PrintWriter;

/**
 * Storage that sends activity reports to a {@link Writer} as plain text.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public abstract class WriterStorage implements Storage {
	/**
	 * Creates a new {@link WriterStorage} instance.
	 *
	 * @param writer Where to write activity reports.
	 */
	protected WriterStorage(Writer writer) {
		this.writer = new PrintWriter(writer, /*autoflush*/true);
	}

	/**
	 * Write a line of text for a stored activity.
	 *
	 * The format is <code><var>activityID</var>[<var>incident</var>,<var>incident</var>...]</code>.
	 *
	 * @param id a {@link String} value.
	 * @param incidents a {@link List} value.
	 */
	public void store(String id, List incidents) {
		writer.println(id + incidents);
	}

	/** Where to send the text. */
	protected PrintWriter writer;
}
