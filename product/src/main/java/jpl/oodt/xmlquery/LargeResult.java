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


package jpl.oodt.xmlquery;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;

/**
 * Large result.
 *
 * @deprecated Replaced by {@link jpl.eda.xmlquery.LargeResult}.
 */
public class LargeResult extends Result {
	long size;

	public LargeResult(jpl.eda.xmlquery.LargeResult newStyle) {
		super (newStyle);
		this.size = newStyle.getSize();
	}

	private static final ObjectStreamField[] serialPersistentFields = {
		new ObjectStreamField("size", Long.TYPE)
	};

	private void writeObject(ObjectOutputStream s) throws IOException {
		ObjectOutputStream.PutField f = s.putFields();
		f.put("size", size);
		s.writeFields();
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		ObjectInputStream.GetField f = s.readFields();
		size = f.get("size", /*def value*/0L);
	}

	/** Serial version unique ID. */
	static final long serialVersionUID = -969838775595705444L;
}
