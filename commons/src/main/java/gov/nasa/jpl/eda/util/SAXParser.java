// This software was developed by the the Jet Propulsion Laboratory, an operating division
// of the California Institute of Technology, for the National Aeronautics and Space
// Administration, an independent agency of the United States Government.
// 
// This software is copyrighted (c) 2000 by the California Institute of Technology.  All
// rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification, is not
// permitted under any circumstance without prior written permission from the California
// Institute of Technology.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
// THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// $Id: SAXParser.java,v 1.3 2005-08-03 17:25:35 kelly Exp $

package jpl.eda.util;

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
	public void setFeature(String featureId, boolean state) throws SAXNotRecognizedException, SAXNotSupportedException {
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
	public boolean getFeature(String featureId) throws SAXNotRecognizedException, SAXNotSupportedException {
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
	public void setProperty(String propertyId, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
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
	public Object getProperty(String propertyId) throws SAXNotRecognizedException, SAXNotSupportedException {
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
	public void setLocale(Locale locale) throws SAXException {
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
