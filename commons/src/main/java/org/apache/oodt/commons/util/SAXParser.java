// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.commons.util;

import java.io.IOException;
import java.util.*;
import org.xml.sax.*;

/** An XML Simple API for XML (SAX) parser.
 *
 * Objects of this class are SAX parsers.
 *
 * @author Kelly
 */
public class SAXParser {
	/** Construct a SAX Parser.
	 */
	SAXParser(javax.xml.parsers.SAXParser parser) {
		this.parser = parser;
	}

	/** Returns a list of features that this parser recognizes.
	 *
	 * This method will never return null; if no features are recognized, this
	 * method will return a zero length array.
	 *
	 * @return Recognized features.
	 */
	public String[] getFeaturesRecognized() {
		return EMPTY_STRING_ARRAY;
	}

	/**
	 * Returns a list of properties that this parser recognizes.
	 * This method will never return null; if no properties are
	 * recognized, this method will return a zero length array.
	 *
	 * @return Recognized properties.
	 */
	public String[] getPropertiesRecognized() {
		return EMPTY_STRING_ARRAY;
	}

	/** Resets the parser. */
	public void reset() {}


	/**
	 * Set the state of any feature in a SAX2 parser.  The parser
	 * might not recognize the feature, and if it does recognize
	 * it, it might not be able to fulfill the request.
	 *
	 * @param featureId The unique identifier (URI) of the feature.
	 * @param state The requested state of the feature (true or false).
	 *
	 * @exception SAXNotRecognizedException If the requested feature is
	 *                                      not known.
	 * @exception SAXNotSupportedException If the requested feature is
	 *                                     known, but the requested state
	 *                                     is not supported.
	 */
	public void setFeature(String featureId, boolean state) throws SAXNotSupportedException {
		throw new SAXNotSupportedException("This parser supports no features");
	}

	/**
	 * Query the current state of any feature in a SAX2 parser.  The
	 * parser might not recognize the feature.
	 *
	 * @param featureId The unique identifier (URI) of the feature
	 *                  being set.
	 *
	 * @return The current state of the feature.
	 *
	 * @exception SAXNotRecognizedException If the requested feature is
	 *                                      not known.
	 */
	public boolean getFeature(String featureId) throws SAXNotSupportedException {
		throw new SAXNotSupportedException("This parser supports no features");
	}

	/**
	 * Set the value of any property in a SAX2 parser.  The parser
	 * might not recognize the property, and if it does recognize
	 * it, it might not support the requested value.
	 *
	 * @param propertyId The unique identifier (URI) of the property
	 *                   being set.
	 * @param value The value to which the property is being set.
	 *
	 * @exception SAXNotRecognizedException If the requested property is
	 *                                      not known.
	 * @exception SAXNotSupportedException If the requested property is
	 *                                     known, but the requested
	 *                                     value is not supported.
	 */
	public void setProperty(String propertyId, Object value) throws SAXNotSupportedException {
		throw new SAXNotSupportedException("This parser supports no properties");
	}

	/**
	 * Return the current value of a property in a SAX2 parser.
	 * The parser might not recognize the property.
	 *
	 * @param propertyId The unique identifier (URI) of the property
	 *                   being set.
	 *
	 * @return The current value of the property.
	 *
	 * @exception SAXNotRecognizedException If the requested property is
	 *                                      not known.
	 *
	 */
	public Object getProperty(String propertyId) throws SAXNotSupportedException {
		throw new SAXNotSupportedException("This parser supports no properties");
	}

	/**
	 * Returns true if the specified feature is recognized.
	 *
	 */
	public boolean isFeatureRecognized(String featureId) {
		return false;
	}


	/**
	 * Returns true if the specified property is recognized.
	 *
	 */
	public boolean isPropertyRecognized(String propertyId) {
		return false;
	}

	/**
	 * return the locator being used by the parser
	 *
	 * @return the parser's active locator
	 */
	public final Locator getLocator() {
		throw new IllegalStateException("This parser doesn't support locators");
	}

	/**
	 * Sets the resolver used to resolve external entities. The EntityResolver
	 * interface supports resolution of public and system identifiers.
	 *
	 * @param resolver The new entity resolver. Passing a null value will
	 *                 uninstall the currently installed resolver.
	 */
	public void setEntityResolver(EntityResolver resolver) {
		try {
			parser.getXMLReader().setEntityResolver(resolver);
		} catch (SAXException ignore) {}
	}

	/**
	 * Return the current entity resolver.
	 *
	 * @return The current entity resolver, or null if none
	 *         has been registered.
	 * @see #setEntityResolver
	 */
	public EntityResolver getEntityResolver() {
		throw new IllegalStateException("This parser supports only setting of the entity resolver, not querying");
	}

	/**
	 * Sets the error handler.
	 *
	 * @param handler The new error handler.
	 */
	public void setErrorHandler(ErrorHandler handler) {
		try {
			parser.getXMLReader().setErrorHandler(handler);
		} catch (SAXException ignore) {}
	}

	/**
	 * Return the current error handler.
	 *
	 * @return The current error handler, or null if none
	 *         has been registered.
	 * @see #setErrorHandler
	 */
	public ErrorHandler getErrorHandler() {
		throw new IllegalStateException("This parser supports only setting of the error handler, not querying");
	}

	/**
	 * Parses the specified input source.
	 *
	 * @param source The input source.
	 *
	 * @exception org.xml.sax.SAXException Throws exception on SAX error.
	 * @exception java.io.IOException Throws exception on i/o error.
	 */
	public void parse(InputSource source) throws SAXException, IOException {
		parser.getXMLReader().parse(source);
	}

	/**
	 * Parses the input source specified by the given system identifier.
	 * <p>
	 * This method is equivalent to the following:
	 * <pre>
	 *     parse(new InputSource(systemId));
	 * </pre>
	 *
	 * @param systemID The input source.
	 *
	 * @exception org.xml.sax.SAXException Throws exception on SAX error.
	 * @exception java.io.IOException Throws exception on i/o error.
	 */
	public void parse(String systemID) throws SAXException, IOException {
		parser.getXMLReader().parse(systemID);
	}


	/**
	 * Set the locale to use for messages.
	 *
	 * @param locale The locale object to use for localization of messages.
	 *
	 * @exception SAXException An exception thrown if the parser does not
	 *                         support the specified locale.
	 */
	public void setLocale(Locale locale) {
		throw new IllegalStateException("This parser doesn't support localized error messages");
	}

	/**
	 * Allow an application to register a DTD event handler.
	 *
	 * <p>If the application does not register a DTD handler, all DTD
	 * events reported by the SAX parser will be silently ignored.</p>
	 *
	 * <p>Applications may register a new or different handler in the
	 * middle of a parse, and the SAX parser must begin using the new
	 * handler immediately.</p>
	 *
	 * @param handler The DTD handler.
	 * @exception java.lang.NullPointerException If the handler 
	 *            argument is null.
	 * @see #getDTDHandler
	 */
	public void setDTDHandler(DTDHandler handler) {
		try {
			parser.getXMLReader().setDTDHandler(handler);
		} catch (SAXException ignore) {}
	}

	/**
	 * Return the current DTD handler.
	 *
	 * @return The current DTD handler, or null if none
	 *         has been registered.
	 * @see #setDTDHandler
	 */
	public DTDHandler getDTDHandler() {
		try {
			return parser.getXMLReader().getDTDHandler();
		} catch (SAXException ex) {
			throw new IllegalStateException("Unexpected SAXException: " + ex.getMessage());
		}
	}

	/**
	 * Allow an application to register a content event handler.
	 *
	 * <p>If the application does not register a content handler, all
	 * content events reported by the SAX parser will be silently
	 * ignored.</p>
	 *
	 * <p>Applications may register a new or different handler in the
	 * middle of a parse, and the SAX parser must begin using the new
	 * handler immediately.</p>
	 *
	 * @param handler The content handler.
	 * @exception java.lang.NullPointerException If the handler 
	 *            argument is null.
	 * @see #getContentHandler
	 */
	public void setContentHandler(ContentHandler handler) {
		try {
			parser.getXMLReader().setContentHandler(handler);
		} catch (SAXException ignore) {}
	}

	/**
	 * Return the current content handler.
	 *
	 * @return The current content handler, or null if none
	 *         has been registered.
	 * @see #setContentHandler
	 */
	public ContentHandler getContentHandler() {
		try {
			return parser.getXMLReader().getContentHandler();
		} catch (SAXException ex) {
			throw new IllegalStateException("Unexpected SAXException: " + ex.getMessage());
		}

	}

	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private javax.xml.parsers.SAXParser parser;
}
