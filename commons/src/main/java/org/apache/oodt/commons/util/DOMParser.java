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

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.io.IOException;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;

/** An XML Document Object Model parser.
 *
 * Objects of this class are DOM parsers.
 *
 * @author Kelly
 */
public class DOMParser {
	/** Construct the DOM Parser.
	 */
	DOMParser(DocumentBuilder builder) {
		this.builder = builder;
	}

	/** Get the document.
	 *
	 * @return The document.
	 */
	public Document getDocument() {
		if (document == null) {
		  throw new IllegalStateException("Must parse something first");
		}
		return document;
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
	public void reset() {
		document = null;
	}

	/** Resets or copies the parser. */
	public void resetOrCopy() {
		reset();
	}

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
		throw new IllegalStateException("Locators ar enot supported");
	}

	/**
	 * Sets the resolver used to resolve external entities. The EntityResolver
	 * interface supports resolution of public and system identifiers.
	 *
	 * @param resolver The new entity resolver. Passing a null value will
	 *                 uninstall the currently installed resolver.
	 */
	public void setEntityResolver(EntityResolver resolver) {
		builder.setEntityResolver(resolver);
	}

	/**
	 * Return the current entity resolver.
	 *
	 * @return The current entity resolver, or null if none
	 *         has been registered.
	 * @see #setEntityResolver
	 */
	public EntityResolver getEntityResolver() {
		throw new IllegalStateException("The resolver can only be set, not queried");
	}

	/**
	 * Sets the error handler.
	 *
	 * @param handler The new error handler.
	 */
	public void setErrorHandler(ErrorHandler handler) {
		builder.setErrorHandler(handler);
	}

	/**
	 * Return the current error handler.
	 *
	 * @return The current error handler, or null if none
	 *         has been registered.
	 * @see #setErrorHandler
	 */
	public ErrorHandler getErrorHandler() {
		throw new IllegalStateException("The error handler can only be set, not queried");
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
		document = builder.parse(source);
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
		document = builder.parse(systemID);
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
		throw new IllegalStateException("This parser does not support localized error messages");
	}

	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private Document document;
	private DocumentBuilder builder;
}
