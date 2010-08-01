/*
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

#ifndef METADATA_CPP_
#define METADATA_CPP_

#include "Metadata.h"
#include "XStr.h"
#include <vector>
#include <map>
#include <string>
#include <iostream>
#include <xercesc/dom/DOM.hpp>
#include<xercesc/dom/DOMDocument.hpp>
#include<xercesc/dom/DOMElement.hpp>
#include<xercesc/dom/DOMNodeList.hpp>
#include <xercesc/util/PlatformUtils.hpp>
#include <xercesc/util/XMLString.hpp>
#include <xercesc/framework/StdOutFormatTarget.hpp>
#include <xercesc/util/OutOfMemoryException.hpp>
#ifdef XERCES_CPP_NAMESPACE_USE
XERCES_CPP_NAMESPACE_USE
#endif

#define X(str) XStr(str).unicodeForm()

//STD imports
using namespace std;

namespace cas
{

/**********************************************************************
Implementation of the Metadata container methods.
***********************************************************************/
	
vector<string> Metadata::EMPTY_VEC(0);
string Metadata::EMPTY_STR;

Metadata::Metadata()
{
	
}

Metadata::Metadata(DOMDocument *doc)
{
   DOMElement *metadataRootElem = doc->getDocumentElement();
   
   if(metadataRootElem == NULL){
   	return;
   }
   
   XMLCh *keyValStr = XMLString::transcode("keyval");
   DOMNodeList *keyValElems = metadataRootElem->getElementsByTagName(keyValStr);
   
   const XMLSize_t nodeCount = keyValElems->getLength();
   
   //cout << "Metadata::constructor:  Found " << nodeCount << " keyval elements" << endl;
   
   for(XMLSize_t i = 0; i < nodeCount; i++){
     DOMNode* currentNode = keyValElems->item(i) ;
        
     if(currentNode == NULL){
       	  continue;
     }
     	
     if( xercesc::DOMNode::ELEMENT_NODE != currentNode->getNodeType() ){
       // not an element node -> not of interest here
        continue ;
     }

     DOMElement* metadataElem = dynamic_cast< xercesc::DOMElement* >(currentNode);

     
     string key;
     read(metadataElem, "key", key);
     vector<string> values;
     readMany(metadataElem, "val", values);
     
     /*cout << "Processing DOMElement (key, values): (" << key << ", ";
     
     for(int j=0; j < values.size(); j++){
     	cout << values[j] << ", ";
     }
     
     cout << ")" << endl;*/
     
     elementMap[key] = values;
        	
   }	
}

Metadata::~Metadata()
{
}


void Metadata::read(DOMElement *elem, const string& key, string& value){
	XMLCh *tagName = XMLString::transcode(key.c_str());
	DOMNodeList *valueNodes = elem->getElementsByTagName(tagName);
	
	DOMNode* valElem = valueNodes->item(0);
	value = getSimpleNodeText(valElem);
}
 

void Metadata::readMany(DOMElement *elem, const string& key, vector<string>& values){
	XMLCh *tagName = XMLString::transcode(key.c_str());
	DOMNodeList *valueNodes = elem->getElementsByTagName(tagName);
	
    for(int i=0; i < valueNodes->getLength(); i++){
    	  DOMNode *valElem = valueNodes->item(i);
    	  string value = getSimpleNodeText(valElem);
    	  values.push_back(value);
    }
}

bool Metadata::containsKey(const string& key) const{
	return elementMap.find(key) != elementMap.end();
}

bool Metadata::isMultiValued(const string& key){
    if(elementMap.find(key) != elementMap.end()){
    	vector<string>& values = elementMap[key];
    	return values.size() > 1;
    }
    else return false;
}

void Metadata::addMetadata(const string& key, const string& value){
	if(elementMap.find(key) != elementMap.end()){	
		vector<string>& values = elementMap[key];
		values.push_back(value);
	}
	else{
		vector<string> values;
		values.push_back(value);
		elementMap[key] = values;
	}
}

void Metadata::addMetadata(const string& key, const vector<string>& values){
	if(elementMap.find(key) != elementMap.end()){
		vector<string>& existingValues = elementMap[key];
		
         for(int i=0; i < values.size(); i++){
         	existingValues.push_back(values[i]);
         }
	}
	else{
		elementMap[key] = values;
	}
}

void Metadata::removeMetadata(const string& key){
	if(elementMap.find(key) != elementMap.end()){
		elementMap.erase(key);
	}
}

void Metadata::replaceMetadata(const string& key, const string& value){
	removeMetadata(key);
	vector<string> values;
	values.push_back(value);
	elementMap[key] = values;
}
		
void Metadata::replaceMetadata(const string& key, const vector<string>& values){
	removeMetadata(key);
	elementMap[key] = values;
}
	
const string& Metadata::getMetadata(const string& key){
	if(elementMap.find(key) != elementMap.end()){
		return (elementMap[key])[0];
	}
	else return EMPTY_STR;
}

const vector<string>& Metadata::getAllMetadata(const string& key){
    if(elementMap.find(key) != elementMap.end()){
    	    return elementMap[key];
    }
    else return EMPTY_VEC;
    		
}

string Metadata::getSimpleNodeText(DOMNode* node){
    string nodeTxt = "";
    DOMNodeList *children = node->getChildNodes();
    for(int i=0; i < children->getLength(); i++){
      DOMNode *n = children->item(i);
      if(n->getNodeType() == DOMNode::TEXT_NODE){
         const XMLCh *xmlText = n->getNodeValue();
         nodeTxt+=XMLString::transcode(xmlText);
      }    
    }

   return nodeTxt;
}

DOMDocument* Metadata::toXML(void){
    // Initialize the XML4C2 system.
    try
    {
        XMLPlatformUtils::Initialize();
    }

    catch(const XMLException& toCatch)
    {
        char *pMsg = XMLString::transcode(toCatch.getMessage());
        XERCES_STD_QUALIFIER cerr << "Error during Xerces-c Initialization.\n"
             << "  Exception message:"
             << pMsg;
        XMLString::release(&pMsg);
        return NULL;
    }


   {
       DOMImplementation* impl =  DOMImplementationRegistry::getDOMImplementation(X("Core"));

       if (impl != NULL)
       {
           try
           {
               DOMDocument* doc = impl->createDocument(
                           // FIXME: change namespace URI?
                           X("http://oodt.jpl.nasa.gov/1.0/cas"),                    // root element namespace URI.
                           X("cas:metadata"),         // root element name
                           NULL/*pDoctype*/);                   // document type object (DTD).

               DOMElement* rootElem = doc->getDocumentElement();

              for(map<string, vector<string> >::iterator i = elementMap.begin(); i != elementMap.end(); i++){
              	DOMElement* metaElem = doc->createElement(X("keyval"));
              	rootElem->appendChild(metaElem);
              	
              	DOMElement* keyElem = doc->createElement(X("key"));
              	metaElem->appendChild(keyElem);
              	
              	DOMText* keyTextElem = doc->createTextNode(X(i->first));
              	keyElem->appendChild(keyTextElem);
              	
              	if(i->second.size() > 1){
              		metaElem->setAttribute(X("type"), X("vector"));
              	}
              	else{
              		metaElem->setAttribute(X("type"), X("scalar"));
              	}
              	
              	for(int j=0; j < i->second.size(); j++){
              		DOMElement* valueElem = doc->createElement(X("val"));
              	    DOMText* valTextElem = doc->createTextNode(X(i->second[j]));
              	    valueElem->appendChild(valTextElem);
                    metaElem->appendChild(valueElem);
              	}
              	
              }

               return doc;
           }
           catch (const OutOfMemoryException&)
           {
               XERCES_STD_QUALIFIER cerr << "OutOfMemoryException" << XERCES_STD_QUALIFIER endl;
           }
           catch (const DOMException& e)
           {
               XERCES_STD_QUALIFIER cerr << "DOMException code is:  " << e.code << XERCES_STD_QUALIFIER endl;
           }
           catch (...)
           {
               XERCES_STD_QUALIFIER cerr << "An error occurred creating the document" << XERCES_STD_QUALIFIER endl;
           }
       }
       else
       {
           XERCES_STD_QUALIFIER cerr << "Requested implementation is not supported" << XERCES_STD_QUALIFIER endl;
       }
   }

   XMLPlatformUtils::Terminate();
   return NULL;
}

}

#endif /*METADATA_CPP_*/
