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
import java.io.Serializable;

/**
 * Header to a result.
 *
 * @deprecated Replaced by {@link jpl.eda.xmlquery.Header}.
 */
public class Header implements Serializable {
	private String name;
	private String type;
	private String unit;

	public Header(jpl.eda.xmlquery.Header newStyle) {
		this.name = newStyle.getName();
		this.type = newStyle.getType();
		this.unit = newStyle.getUnit();
	}

	private static final ObjectStreamField[] serialPersistentFields = {
		new ObjectStreamField("name", String.class),
		new ObjectStreamField("type", String.class),
		new ObjectStreamField("unit", String.class)
	};

	private void writeObject(ObjectOutputStream s) throws IOException {
		ObjectOutputStream.PutField f = s.putFields();
		f.put("name", name);
		f.put("type", type);
		f.put("unit", unit);
		s.writeFields();
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		ObjectInputStream.GetField f = s.readFields();
		name = (String) f.get("name", /*def value*/null);
		type = (String) f.get("type", /*def value*/null);
		unit = (String) f.get("unit", /*def value*/null);
	}

	/** Serial version unique ID. */
	static final long serialVersionUID = 2238567985493830805L;
}
