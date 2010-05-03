// Copyright 2000-2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProfileElementTestCase.java,v 1.2 2005/09/19 15:48:29 kelly Exp $

package jpl.eda.profile;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import jpl.eda.util.XML;

/**
 * Test case for profile elements.
 *
 * @author Kelly
 */ 
public abstract class ProfileElementTestCase extends TestCase {
	/** Construct the test case for the {@link ProfileElement} superclass. */
	public ProfileElementTestCase(String name) {
		super(name);
	}

	/**
	 * Create a profile element object to test.
	 *
	 * The element returned must have as its owning profile the
	 * <code>ProfileTest.TEST_PROFILE</code>, be named "name", have "id" as its ID,
	 * have "desc" as its description, must be of type "type", have "unit" units, have
	 * no synonyms, not be obligatory, may occur once, and have "comment" as its
	 * comment.
	 *
	 * @return A profile element.
	 */
	protected abstract ProfileElement createProfileElement();

	/**
	 * Check if the given enumeration flag is valid.
	 *
	 * This method merely asserts that it's valid for the profile element in question.
	 *
	 * @param text Text to check.
	 */
	protected abstract void checkEnumFlag(String text);

	/**
	 * Check that the given value is valid.
	 *
	 * This method merely asserts that it's valid for the profile element in question.
	 *
	 * @param text Text to check.
	 */
	protected abstract void checkValue(String text);

	/**
	 * Check that the given maximum value is valid.
	 *
	 * This method merely asserts that it's valid for the profile element in question.
	 *
	 * @param text Text to check.
	 */
	protected abstract void checkMaxValue(String text);

	/**
	 * Check that the given minimum value is valid.
	 *
	 * This method merely asserts that it's valid for the profile element in question.
	 *
	 * @param text Text to check.
	 */
	protected abstract void checkMinValue(String text);

	public void testCharacteristics() {
		ProfileElement element = createProfileElement();
		assertEquals(ProfileTest.TEST_PROFILE, element.getProfile());

		assertEquals("name", element.getName());
		element.setName("newName");
		assertEquals("newName", element.getName());

		assertEquals("id", element.getID());
		element.setID("newID");
		assertEquals("newID", element.getID());

		assertEquals("desc", element.getDescription());
		element.setDescription("newDesc");
		assertEquals("newDesc", element.getDescription());

		assertEquals("type", element.getType());
		element.setType("newType");
		assertEquals("newType", element.getType());

		assertEquals("unit", element.getUnit());
		element.setUnit("newUnit");
		assertEquals("newUnit", element.getUnit());

		assertEquals(0, element.getSynonyms().size());
		element.getSynonyms().add("synonym");
		assertEquals(1, element.getSynonyms().size());
		assertEquals("synonym", element.getSynonyms().get(0));

		assertTrue(!element.isObligatory());
		element.setObligation(true);
		assertTrue(element.isObligatory());

		assertEquals(1, element.getMaxOccurrence());
		element.setMaxOccurrence(2);
		assertEquals(2, element.getMaxOccurrence());

		assertEquals("comment", element.getComments());
		element.setComments("newComment");
		assertEquals("newComment", element.getComments());
	}

	public void testObjectMethods() {
		ProfileElement elem1 = createProfileElement();
		ProfileElement elem2 = createProfileElement();
		ProfileElement elem3 = createProfileElement();
		elem3.setName("newName");
		assertEquals(elem1, elem1);
		assertEquals(elem1, elem2);
		assertTrue(!elem1.equals(elem3));
		ProfileElement elem4 = (ProfileElement) elem3.clone();
		assertEquals(elem3, elem4);
		assertTrue(elem3 != elem4);
	}

	public void testXML() {
		ProfileElement element = createProfileElement();
		Document doc = XML.createDocument();
		Node root = element.toXML(doc);
		assertEquals("profElement", root.getNodeName());
		NodeList children = root.getChildNodes();
		boolean foundName = false;
		boolean foundEnumFlag = false;
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			String name = child.getNodeName();
			String text = ProfileElement.text(child);
			if ("elemId".equals(name)) {
				assertEquals("id", text);
			} else if ("elemName".equals(name)) {
				assertEquals("name", text);
				foundName = true;
			} else if ("elemDesc".equals(name)) {
				assertEquals("desc", text);
			} else if ("elemType".equals(name)) {
				assertEquals("type", text);
			} else if ("elemUnit".equals(name)) {
				assertEquals("unit", text);
			} else if ("elemEnumFlag".equals(name)) {
				checkEnumFlag(text);
				foundEnumFlag = true;
			} else if ("elemValue".equals(name)) {
				checkValue(text);
			} else if ("elemMinValue".equals(name)) {
				checkMinValue(text);
			} else if ("elemMaxValue".equals(name)) {
				checkMaxValue(text);
			} else if ("elemSynonym".equals(name)) {
				; // ignore
			} else if ("elemObligation".equals(name)) {
				assertEquals("Optional", text);
			} else if ("elemMaxOccurrence".equals(name)) {
				assertEquals("1", text);
			} else if ("elemComment".equals(name)) {
				assertEquals("comment", text);
			} else fail("Unknown node <" + name + "> under <profElement>");
		}
		assertTrue("Required <elemName> missing", foundName);
		assertTrue("Required <elemEnumFlag> missing", foundEnumFlag);
	}
}
