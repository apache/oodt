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
 * Header to a query.
 *
 * @deprecated Replaced by {@link jpl.eda.xmlquery.QueryHeader}.
 */
public class QueryHeader implements Serializable {
	private String id;
	private String title;
	private String desc;
	private String type;
	private String status;
	private String sec;
	private String rev;
	private String ddID;

	public QueryHeader(jpl.eda.xmlquery.QueryHeader newStyle) {
		id = newStyle.getID();
		title = newStyle.getTitle();
		desc = newStyle.getDescription();
		type = newStyle.getType();
		status = newStyle.getStatusID();
		sec = newStyle.getSecurityType();
		rev = newStyle.getRevisionNote();
		ddID = newStyle.getDataDictID();
	}

	private static final ObjectStreamField[] serialPersistentFields = {
		new ObjectStreamField("id", String.class),
		new ObjectStreamField("title", String.class),
		new ObjectStreamField("description", String.class),
		new ObjectStreamField("type", String.class),
		new ObjectStreamField("statusID", String.class),
		new ObjectStreamField("securityType", String.class),
		new ObjectStreamField("revisionNote", String.class),
		new ObjectStreamField("dataDictID", String.class)
	};		

	private void writeObject(ObjectOutputStream s) throws IOException {
		ObjectOutputStream.PutField f = s.putFields();
		f.put("id", id);
		f.put("title", title);
		f.put("description", desc);
		f.put("type", type);
		f.put("statusID", status);
		f.put("securityType", sec);
		f.put("revisionNote", rev);
		f.put("dataDictID", ddID);
		s.writeFields();
	}

	/** Serial version unique ID. */
	static final long serialVersionUID = -8601229234696670816L;
}
