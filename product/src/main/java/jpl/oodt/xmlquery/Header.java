// Copyright 1999-2004 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: Header.java,v 1.1 2004-11-30 21:22:25 kelly Exp $

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
