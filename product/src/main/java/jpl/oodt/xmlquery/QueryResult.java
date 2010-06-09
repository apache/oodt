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
 * Result of a query.
 *
 * @deprecated Replaced by {@link jpl.eda.xmlquery.QueryResult}.
 */
public class QueryResult implements Serializable {
	public List results;

	public QueryResult(jpl.eda.xmlquery.QueryResult newStyle) {
		results = new ArrayList(newStyle.getList().size());
		for (Iterator i = newStyle.getList().iterator(); i.hasNext();) {
			jpl.eda.xmlquery.Result newResult = (jpl.eda.xmlquery.Result) i.next();
			if (newResult instanceof jpl.eda.xmlquery.LargeResult)
				results.add(new LargeResult((jpl.eda.xmlquery.LargeResult) newResult));
			else
				results.add(new Result(newResult));
		}
	}

	private static final ObjectStreamField[] serialPersistentFields = {
		new ObjectStreamField("list", List.class)
	};

	private void writeObject(ObjectOutputStream s) throws IOException {
		ObjectOutputStream.PutField f = s.putFields();
		f.put("list", results);
		s.writeFields();
	}

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		ObjectInputStream.GetField f = s.readFields();
		results = (List) f.get("list", /*def value*/null);
	}

	/** Serial version unique ID. */
	static final long serialVersionUID = 9156030927051226848L;
}
