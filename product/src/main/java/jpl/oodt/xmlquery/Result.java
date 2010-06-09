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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A result.
 *
 * @deprecated Replaced by {@link jpl.eda.xmlquery.Result}.
 */
public class Result implements Serializable {
	String id;
	String mime;
	String prof;
	String res;
	List headers;
	Object value;
	boolean classified;
	long validity;

	public Result(jpl.eda.xmlquery.Result newStyle) {
		id = newStyle.getID();
		mime = newStyle.getMimeType();
		prof = newStyle.getProfileID();
		res = newStyle.getResourceID();
		headers = convert(newStyle.getHeaders());
		value = newStyle.getValue();
		classified = newStyle.isClassified();
		validity = newStyle.getValidity();
	}

	private static List convert(List newStyle) {
		List oldStyle = new ArrayList(newStyle.size());
		for (Iterator i = newStyle.iterator(); i.hasNext();)
			oldStyle.add(new Header((jpl.eda.xmlquery.Header) i.next()));
		return oldStyle;
	}

	private static final ObjectStreamField[] serialPersistentFields = {
		new ObjectStreamField("id", String.class),
		new ObjectStreamField("mimeType", String.class),
		new ObjectStreamField("profileID", String.class),
		new ObjectStreamField("resourceID", String.class),
		new ObjectStreamField("headers", List.class),
		new ObjectStreamField("value", Object.class),
		new ObjectStreamField("classified", Boolean.TYPE),
		new ObjectStreamField("validity", Long.TYPE)
	};

	private void writeObject(ObjectOutputStream s) throws IOException {
		ObjectOutputStream.PutField f = s.putFields();
		f.put("id", id);
		f.put("mimeType", mime);
		f.put("profileID", prof);
		f.put("resourceID", res);
		f.put("headers", headers);
		f.put("value", value);
		f.put("classified", classified);
		f.put("validity", validity);
		s.writeFields();
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		ObjectInputStream.GetField f = s.readFields();
		id = (String) f.get("id", /*def value*/null);
		mime = (String) f.get("mimeType", /*def value*/null);
		prof = (String) f.get("profileID", /*def value*/null);
		res = (String) f.get("resourceID", /*def value*/null);
		headers = (List) f.get("headers", /*def value*/null);
		value = f.get("value", /*def value*/null);
		classified = f.get("classified", /*def value*/false);
		validity = f.get("validity", /*def value*/0L);
	}

	/** Serial version unique ID. */
	static final long serialVersionUID = 9169143944191239575L;
}
