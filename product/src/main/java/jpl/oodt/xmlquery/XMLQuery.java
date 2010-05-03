// Copyright 1999-2004 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: XMLQuery.java,v 1.1 2004-11-30 21:28:49 kelly Exp $

package jpl.oodt.xmlquery;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.List;

/**
 * A query.
 *
 * @deprecated Replaced by {@link jpl.eda.xmlquery.XMLQuery}.
 */
public class XMLQuery implements Serializable {
	private String mode;
	private String type;
	private String levels;
	private int max;
	private String kwqString;
	private List mime;
	private QueryHeader header;
	private List select;
	private List from;
	private List where;
	public QueryResult result;

	public XMLQuery(jpl.eda.xmlquery.XMLQuery q) {
		mode = q.getResultModeID();
		type = q.getPropagationType();
		levels = q.getPropagationLevels();
		max = q.getMaxResults();
		kwqString = q.getKwdQueryString();
		mime = q.getMimeAccept();
		header = new QueryHeader(q.getQueryHeader());
		select = convert(q.getSelectElementSet());
		from = convert(q.getFromElementSet());
		where = convert(q.getWhereElementSet());
		result = new QueryResult(q.getResult());
	}

	private static List convert(List newStyle) {
		List oldStyle = new ArrayList(newStyle.size());
		for (Iterator i = newStyle.iterator(); i.hasNext();)
			oldStyle.add(new QueryElement((jpl.eda.xmlquery.QueryElement) i.next()));
		return oldStyle;
	}

	private static final ObjectStreamField[] serialPersistentFields = {
		new ObjectStreamField("resultModeId", String.class),
		new ObjectStreamField("propogationType", String.class),
		new ObjectStreamField("propogationLevels", String.class),
		new ObjectStreamField("maxResults", Integer.TYPE),
		new ObjectStreamField("kwqString", String.class),
		new ObjectStreamField("mimeAccept", List.class),
		new ObjectStreamField("numResults", Integer.TYPE),
		new ObjectStreamField("queryHeader", QueryHeader.class),
		new ObjectStreamField("selectElementSet", List.class),
		new ObjectStreamField("fromElementSet", List.class),
		new ObjectStreamField("whereElementSet", List.class),
		new ObjectStreamField("result", QueryResult.class)
	};

	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
		ObjectInputStream.GetField f = s.readFields();
		result = (QueryResult) f.get("result", /*def value*/null);
	}

	public List getResults() {
		List newStyle = new ArrayList(result.results.size());
		for (Iterator i = result.results.iterator(); i.hasNext();) {
			Result r = (Result) i.next();
			if (r instanceof LargeResult)
				newStyle.add(new jpl.eda.xmlquery.LargeResult(r.id, r.mime, r.prof, r.res, Collections.EMPTY_LIST,
						((LargeResult) r).size));
			else
				newStyle.add(new jpl.eda.xmlquery.Result(r.id, r.mime, r.prof, r.res, Collections.EMPTY_LIST,
						r.value));
		}
		return newStyle;
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		ObjectOutputStream.PutField f = s.putFields();
		f.put("resultModeId", mode);
		f.put("propogationType", type);
		f.put("propogationLevels", levels);
		f.put("maxResults", max);
		f.put("kwqString", kwqString);
		f.put("mimeAccept", mime);
		f.put("numResults", 0);
		f.put("queryHeader", header);
		f.put("selectElementSet", select);
		f.put("fromElementSet", from);
		f.put("whereElementSet", where);
		f.put("result", result);
		s.writeFields();
	}

	/** Serial version unique ID. */
	static final long serialVersionUID = -7638068782048963710L;
}
