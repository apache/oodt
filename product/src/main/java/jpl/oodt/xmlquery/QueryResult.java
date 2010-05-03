// Copyright 1999-2004 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: QueryResult.java,v 1.1 2004-11-30 21:26:09 kelly Exp $

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
