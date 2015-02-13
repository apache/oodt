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

#include "Metadata.h"
#include <string>
#include <vector>
#include <iostream> 
#include <xercesc/dom/DOM.hpp>
#include <xercesc/dom/DOMImplementation.hpp>
#include <xercesc/dom/DOMImplementationLS.hpp>
#include <xercesc/framework/StdOutFormatTarget.hpp>
#include<xercesc/parsers/XercesDOMParser.hpp>

#ifdef XERCES_CPP_NAMESPACE_USE
XERCES_CPP_NAMESPACE_USE
#endif

//STD imports
using namespace std;

//CAS imports
using namespace cas;

/**********************************************************************
Pseudo-unit tests for the Metadata container.
***********************************************************************/

int main(void){
	
	Metadata metadata;
	
	metadata.addMetadata("key1", "val1");
	
	vector<string> values;
	
	values.push_back("val1");
	values.push_back("val2");
	values.push_back("val3");
	
	metadata.addMetadata("key2", values);
	
	cout << "Checking the value of key1: Value: " << metadata.getMetadata("key1") << endl;
	cout << "Checking the value of key2: Value: " << metadata.getMetadata("key2") << endl;

	vector<string> key2Vals = metadata.getAllMetadata("key2");
	cout << "Checking all values for key2: Values: " << endl;
		
	for(int i=0; i < key2Vals.size(); i++){
		 cout << "Value: " << key2Vals[i] << endl;
	}
		
    if(metadata.getAllMetadata("key3") == cas::Metadata::EMPTY_VEC){
    	   cout << "No multiple values for key3!" << endl;
    }
    
    if(metadata.getMetadata("key3") == cas::Metadata::EMPTY_STR){
    	cout << "No singular value for key3!" << endl;
    }
	
	metadata.addMetadata("key3", "val3");
	
	if(metadata.getMetadata("key3") != cas::Metadata::EMPTY_STR){
		cout << "New value for key3: " << metadata.getMetadata("key3") << endl;
	}
	
	if(!metadata.containsKey("blah")){
		cout << "Metadata doesn't contain key blah: correct!" << endl;
	}
	
	if(metadata.containsKey("key3")){
		cout << "Metadata contains key key3: correct!" << endl;
	}
	
	if(metadata.isMultiValued("key2")){
		cout << "Key2 is correctly multi-valued: correct!" << endl;
	}
	
	if(!metadata.isMultiValued("key3")){
		cout << "Key3 is correctly NOT multi-valued: correct!" << endl;
	}
	
	metadata.addMetadata("key4", "val99");
	
	if(metadata.getMetadata("key4") != cas::Metadata::EMPTY_STR){
		cout << "Metadata exists for key4, now deleting key4 and retesting" << endl;
		
		metadata.removeMetadata("key4");
		
		if(metadata.getMetadata("key4") == cas::Metadata::EMPTY_STR){
			cout << "Metadata successfully removed for key4! " << endl;
		}
	}
	
	vector<string> key5vals;
	key5vals.push_back("key5val1");
	key5vals.push_back("key5val2");
	
	metadata.addMetadata("key5", key5vals);
	
	vector<string> key5replvals;
	key5replvals.push_back("key5_replaced_val1");
	key5replvals.push_back("key5_replaced_val2");
	
	metadata.replaceMetadata("key5", key5replvals);

	for(int i=0; i < metadata.getAllMetadata("key5").size(); i++){
		 cout << "Value: " << metadata.getAllMetadata("key5")[i] << endl;
	}	
	
	metadata.addMetadata("key6", "key6val1");
	
	if(metadata.getMetadata("key6") == "key6val1"){
		cout << "key6: value = key6val1, as appropriate" << endl;
	}
	
	metadata.replaceMetadata("key6", "key6_replaced_val1");
	
	if(metadata.getMetadata("key6") == "key6_replaced_val1"){
		cout << "key6: value = key6_replaced_val1, as appropriate" << endl;
	}
	
	
	cout << "XML is: " << endl;
    DOMDocument* doc = metadata.toXML();
	XMLCh tempStr[100];
 

	XMLString::transcode("LS", tempStr, 99);
	DOMImplementation *impl          = DOMImplementationRegistry::getDOMImplementation(tempStr);
    DOMLSSerializer* theSerializer = ((DOMImplementationLS*)impl)->createLSSerializer();


	if (theSerializer->getDomConfig()->canSetParameter(XMLUni::fgDOMWRTFormatPrettyPrint, true))
	  theSerializer->getDomConfig()->setParameter(XMLUni::fgDOMWRTFormatPrettyPrint, true);

	XMLFormatTarget *myFormTarget;
        myFormTarget = new StdOutFormatTarget();
    DOMLSOutput* theOutput = ((DOMImplementationLS*)impl)->createLSOutput();
    theOutput->setByteStream(myFormTarget);

	theSerializer->write((DOMNode*)doc, theOutput);
	
	cout << "Now attempting to parse cas xml document: data/sample.met.xml" << endl;
	
	XercesDOMParser parser_;
	
	parser_.setValidationScheme( xercesc::XercesDOMParser::Val_Never ) ;
	parser_.setDoNamespaces( false ) ;
	parser_.setDoSchema( false ) ;
	parser_.setLoadExternalDTD( false ) ;
	
	 parser_.parse("./data/sample.met.xml") ;

    // there's no need to free this pointer -- it's
    // owned by the parent parser object
    DOMDocument* xmlDoc = parser_.getDocument() ;
    
    cout << "Metadata XML file parsed: constructing CAS Metadata object from it " << endl;
    
    Metadata newMetadata(xmlDoc);
    
    cout << "Outputting XML from CAS Metadata " << endl;
        
    DOMDocument *newDoc = newMetadata.toXML();
    theSerializer->write((DOMNode*)doc, theOutput);
    
	return 1;
		
}
