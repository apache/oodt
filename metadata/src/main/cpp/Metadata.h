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

#ifndef METADATA_H_
#define METADATA_H_

#include <vector>
#include <map>
#include <string>
#include <xercesc/dom/DOM.hpp>
#ifdef XERCES_CPP_NAMESPACE_USE
XERCES_CPP_NAMESPACE_USE
#endif

//STD imports
using namespace std;

namespace cas
{

/**********************************************************************
A Multi-valued, generic Metadata container class. The class uses an internal
STL map of STL string keys pointing to STL vectors of STL strings. The data 
structure looks like the following: 

[std:string key]=><std:vector of std:strings>

The multi-valued nature of the class is handled transparently by this Metadata
container. Since all values are stored internally as string vectors, the difference
between a scalar value and a non-scalar is handled by determing whether the list of
values for a particular key is greater than 1. 

The class relies on <a href="http://xml.apache.org/xerces-c/">Apache's xerces-c 
implementation</a> for outputting and reading in a Metadata container.
***********************************************************************/
class Metadata
{
public:
    /**
     * Constructs a new Metadata container object
     */
	Metadata();
	
	/**
	 * Construct a new Metadata container object from an existing DOM stream.
	 * This method is helpful when you have an external Metadata XML file that needs
	 * to be read into a manipulateable object in memory.
	 * 
	 * @param doc The DOM document to parse and turn into a Metadata container object.
	 */
	Metadata(DOMDocument *doc);
	
	/**
	 * Default Destructor, deletes the internal map.
	 */
	virtual ~Metadata();
	
	/**
	 * Adds a single string value for the specified <code>key</code>.
	 * 
	 * @param key The key to add the string value for.
	 * @param value The string value to add to the specified key.
	 */
	void addMetadata(const string& key, const string& value);
	
	/**
	 * Adds a vector of string values (in order) for the specified <code>key</code>.
	 * 
	 * @param key The key to add the string value for.
	 * @param values The vector of string values to append to the key's value list.
	 */
	void addMetadata(const string& key, const vector<string>& values);
	
	/**
	 * Gets the first metadata value for the given <code>key</code>.
	 * 
	 * @param key The key to obtain the first string metadata value for.
	 * @return The first metadata value for the specified key.
	 */
	const string& getMetadata(const string& key);
	
	/**
	 * Gets All the metadata values for the given <code>key</code>, in order.
	 * 
	 * @param key The key to obtain all metadata values for.
	 * @return an STL vector of STL strings, corresponding to all the values for a particular
	 * metadata key.
	 */
	const vector<string>& getAllMetadata(const string& key);
	
	/**
	 * Returns true if the specified <code>key</code> exists in the Metadata container, 
	 * false otherwise.
	 * 
	 * @param key The key to check for existance of.
	 * @return true if the key exists, false otherwise.
	 */
	bool containsKey(const string& key) const;
	
	/**
	 * Returns true if the specified <code>key</code> has > 1 values in the Metadata
	 * container, false otherwise.
	 * 
	 * @param key The key to check for > 1 values for.
	 * @return True if there are > 1 values, false otherwise.
	 */
	bool isMultiValued(const string& key);
	
	/**
	 * Removes the values, along with the existance of the specified
	 * <code>key</code> within the Metadata container.
	 * 
	 * @param key The key to remove the values and the existance of.
	 */
	void removeMetadata(const string& key);
	
	/**
	 * Replaces the values for the specified <code>key</code>, with the
	 * scalar string <code>value</code> parameter.
	 * 
	 * @param key The key to replace the values for.
	 * @param value The scalar string value to attach to the specified key.
	 */
	void replaceMetadata(const string& key, const string& value);
	
	/**
	 * Replaces the values for the specified <code>key</code>, with the
	 * vector of string <code>values</code> parameter.
	 * 
	 * @param key The key to replace the values for.
	 * @param value The vector of string values to attach to the specified key.
	 */
	void replaceMetadata(const string& key, const vector<string>& values);
	
	/**
	 * Serializes the Metadata container into a Xerces DOM document. Returns
	 * a pointer to the DOMDocument, which can be used for serialization into
	 * a file, or STDOUT/etc.
	 * 
	 * @return a Xerces DOM document (XML) representation of the Metadata container.
	 */
	DOMDocument* toXML(void); 
	
	// the default return value for methods that must return a vector: this
	// vector is size 0, with no elements
    static vector<string> EMPTY_VEC;
    
    // the default return value for methods that must return an stl string
    static string EMPTY_STR;
	

private:
    map<string, vector<string> > elementMap;
    
    void read(DOMElement *elem, const string& key, string& value);
    void readMany(DOMElement *elem, const string& key, vector<string>& values);
    string getSimpleNodeText(DOMNode *node);
    
};

}

#endif /*METADATA_H_*/

