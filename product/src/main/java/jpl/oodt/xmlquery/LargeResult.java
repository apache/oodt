// Copyright 1999-2004 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: LargeResult.java,v 1.1 2004-11-30 21:24:10 kelly Exp $

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
