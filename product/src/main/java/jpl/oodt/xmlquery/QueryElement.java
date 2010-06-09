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
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;

/**
 * Element of a query.
 *
 * @deprecated Replaced by {@link jpl.eda.xmlquery.QueryElement}.
 */
public class QueryElement implements Serializable {
	private String role;
	private String value;

	public QueryElement(jpl.eda.xmlquery.QueryElement newStyle) {
		role = newStyle.getRole();
		value = newStyle.getValue();
	}

	private static final ObjectStreamField[] serialPersistentFields = {
		new ObjectStreamField("role", String.class),
		new ObjectStreamField("value", String.class)
	};

	private void writeObject(ObjectOutputStream s) throws IOException {
		ObjectOutputStream.PutField f = s.putFields();
		f.put("role", role);
		f.put("value", value);
		s.writeFields();
	}

	/** Serial version unique ID. */
	static final long serialVersionUID = -5578423727059888898L;
}
