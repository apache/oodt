// Copyright 1999-2004 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: QueryElement.java,v 1.1 2004-11-30 21:24:47 kelly Exp $

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
