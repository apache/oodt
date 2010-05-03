/*
 * Copyright (C) 2005 Dell Inc.
 *  by Michael Brown <Michael_E_Brown@dell.com>
 * Licensed under the Open Software License version 2.1
 *
 * Alternatively, you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
 
 #ifndef XMLUTILS_H
 #define XMLUTILS_H
 
 #include <string>
 #include <xercesc/util/PlatformUtils.hpp>
 #include <xercesc/dom/DOM.hpp>
 #include <xercesc/dom/DOMImplementationLS.hpp>
 #include <xercesc/framework/StdOutFormatTarget.hpp>
 #include <xercesc/framework/LocalFileFormatTarget.hpp>
 #include <xercesc/parsers/XercesDOMParser.hpp>
 #include <xercesc/util/XMLUni.hpp>
 #include <xercesc/framework/MemBufInputSource.hpp>
 #include <xercesc/framework/Wrapper4InputSource.hpp>
 #define X_(x)  XMLString::transcode(x)
 
 using namespace std;

 namespace xmlutils
 {
 
     XERCES_CPP_NAMESPACE_QUALIFIER DOMElement *castNode2Element(       XERCES_CPP_NAMESPACE_QUALIFIER DOMNode *node );
     const XERCES_CPP_NAMESPACE_QUALIFIER DOMElement *castNode2Element( const XERCES_CPP_NAMESPACE_QUALIFIER DOMNode *node );
 
     string safeXMLChToString( const XMLCh *src );
 
     string safeGetAttribute( const XERCES_CPP_NAMESPACE_QUALIFIER DOMNode *node, const string &attr );
 
     XERCES_CPP_NAMESPACE_QUALIFIER DOMBuilder *getParser( );
 
     XERCES_CPP_NAMESPACE_QUALIFIER DOMElement *findElement( XERCES_CPP_NAMESPACE_QUALIFIER DOMElement *root, const string elementName, const std::string &attribute, const string &value );
     XERCES_CPP_NAMESPACE_QUALIFIER DOMElement *findElementWithNumericAttr( XERCES_CPP_NAMESPACE_QUALIFIER DOMElement *root, const string elementName, const string &attribute, long value);
 
     std::string getNodeText( XERCES_CPP_NAMESPACE_QUALIFIER DOMNode *elem );
     int getNumberFromXmlAttr( XERCES_CPP_NAMESPACE_QUALIFIER DOMElement *element, const string field, int base );
 }
 
 #endif