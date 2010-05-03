// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: RangedProfileElementTest.java,v 1.2 2006/06/16 17:13:43 kelly Exp $

package jpl.eda.profile;

import java.util.ArrayList;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import jpl.eda.io.NullOutputStream;
import jpl.eda.util.XML;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Unit test the {@link RangedProfileElement} class.
 *
 * @author Kelly
 */ 
public class RangedProfileElementTest extends ProfileElementTestCase {
	/** Construct the test case for the {@link RangedProfileElement} class. */
	public RangedProfileElementTest(String name) {
		super(name);
	}

	protected ProfileElement createProfileElement() {
		return new RangedProfileElement(ProfileTest.TEST_PROFILE, "name", "id", "desc", "type", "unit",
			/*synonyms*/ new ArrayList(), /*obligation*/false, /*maxOccurrence*/1, "comment", /*min*/"-100.0",
			/*max*/"100.0");
	}

	public void testIt() {
		ProfileElement element = createProfileElement();
		assertEquals("100.0", element.getMaxValue());
		assertEquals("-100.0", element.getMinValue());
		assertEquals(0, element.getValues().size());
	}
	
	public void testXMLSerialization() throws Exception {
		Profile p = new Profile();
		RangedProfileElement e = new RangedProfileElement(p);
		Document doc = XML.createDocument();
		Node root = e.toXML(doc);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		DOMSource s = new DOMSource(root);
		StreamResult r = new StreamResult(new NullOutputStream());
		t.transform(s, r);
	}

	protected void checkEnumFlag(String text) {
		assertEquals("F", text);
	}

	protected void checkValue(String text) {
		fail("Ranged profile element shouldn't have an enumerated value");
	}

	protected void checkMaxValue(String text) {
		assertEquals("100.0", text);
	}

	protected void checkMinValue(String text) {
		assertEquals("-100.0", text);
	}
}
