# encoding: utf-8
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE.txt file distributed with
# this work for additional information regarding copyright ownership.  The ASF
# licenses this file to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations
# under the License.    

'''Object Oriented Data Technology XML utilities. These are some simple utilities
that make working with rather obtuse DOM API less painful.
'''

__docformat__ = 'restructuredtext'

import string
import xml.dom
    
def text(node):
    '''Return the text under the given node.  Text and CDATA nodes simply return
    their content.  Otherwise, text is gathered in a depth-first left-to-right
    traversal of the node, building up text as we go along.
    
    >>> import xml.dom
    >>> domImpl = xml.dom.getDOMImplementation()
    >>> doc = domImpl.createDocument(None, None, None)
    >>> root = doc.createElement('root')
    >>> root.appendChild(doc.createTextNode('alpha')) # doctest: +ELLIPSIS
    <DOM Text node...>
    >>> text(root)
    'alpha'
    >>> child = root.appendChild(doc.createElement('child'))
    >>> text(child.appendChild(doc.createCDATASection('beta')))
    'beta'
    >>> text(root)
    'alphabeta'
    '''
    strings = []
    _text0(node, strings)
    return string.join(strings, '') # no separator
    

def _text0(node, strings):
    '''Add the text under the given node to the list of strings.
    '''
    if node.nodeType == xml.dom.Node.CDATA_SECTION_NODE:
        strings.append(node.nodeValue)
    elif node.nodeType == xml.dom.Node.TEXT_NODE:
        strings.append(node.nodeValue)
    for child in node.childNodes:
        _text0(child, strings)
    

def add(node, elementName, value=None):
    '''Add an element under the given node.  Optionally, the value text is added to the element.
    Returns the original node, handy for chaining calls together.  If the value is a sequence,
    then the element is added multiple times for each item in the sequence, using the item
    as the text.

    >>> from xml.dom.minidom import getDOMImplementation
    >>> doc = getDOMImplementation().createDocument(None, None, None)
    >>> root = doc.createElement('root')
    >>> root.toxml()
    '<root/>'
    >>> add(root, 'empty').toxml()
    '<root><empty/></root>'
    >>> add(root, 'full', 'of text').toxml()
    '<root><empty/><full>of text</full></root>'
    >>> add(root, 'item', [str(i) for i in range(1, 4)]).toxml()
    '<root><empty/><full>of text</full><item>1</item><item>2</item><item>3</item></root>'
    '''
    owner = node.ownerDocument
    if isinstance(value, basestring):
        elem = owner.createElement(elementName)
        node.appendChild(elem)
        elem.appendChild(owner.createTextNode(value))
    elif value is not None:
        for i in value:
            elem = owner.createElement(elementName)
            node.appendChild(elem)
            elem.appendChild(owner.createTextNode(i))
    else:
        node.appendChild(owner.createElement(elementName))
    return node
    

class DocumentableField(object):
    '''A documentable field is an attribute of an object that we can serialize into XML.
    '''
    SINGLE_VALUE_KIND = 1
    MULTI_VALUED_KIND = 2
    DOCUMENTABLE_KIND = 3

    def __init__(self, attrName, elemName, kind):
        '''Initialize a documentable field with attrName (name of attribute in object),
        elemName (name to use for XML element) and kind, which is one of the DOCUMENTABLE_*
        constants.
        '''
        self.attrName = attrName
        self.elemName = elemName
        if kind not in (DocumentableField.SINGLE_VALUE_KIND, DocumentableField.MULTI_VALUED_KIND,
            DocumentableField.DOCUMENTABLE_KIND):
            raise ValueError('Invalid kind %d' % kind)
        self.kind = kind
    
    def __cmp__(self, other):
        attrName = cmp(self.attrName, other.attrName)
        if attrName < 0:
            return -1
        elif attrName == 0:
            elemName = cmp(self.elemName, other.elemName)
            if elemName < 0:
                return -1
            elif elemName == 0:
                return cmp(self.kind, other.kind)
        return 1
    
    def __hash__(self):
        return hash(self.attrName) ^ hash(self.elemName) ^ self.kind
    

class Documentable(object):
    '''An object that can be documented into XML and parsed from an XML document.
    '''
    def getDocumentElementName(self):
        '''Get the XML tag name.  Subclasses must override this
        otherwise they get the tag name `UNKNOWN`.
        '''
        return 'UNKNOWN'
        
    def getDocumentableFields(self):
        '''Get the sequence of documentable attributes.  Subclasses should override this
        with a sequence of `DocumentableField` objects.  By default, we return an empty
        sequence.
        '''
        return []
    
    def toXML(self, owner):
        '''Convert this object into XML owned by the given `owner` document.
        '''
        root = owner.createElement(self.getDocumentElementName())
        for df in self.getDocumentableFields():
            if df.kind == DocumentableField.SINGLE_VALUE_KIND:
                add(root, df.elemName, str(getattr(self, df.attrName)))
            elif df.kind == DocumentableField.MULTI_VALUED_KIND:
                add(root, df.elemName, [str(value) for value in getattr(self, df.attrName)])
            else:
                root.appendChild(getattr(self, df.attrName).toXML(owner))
        return root
    
    def computeValueFromDocument(self, attrName, text):
        '''Compute a value for the attribute named `attrName` from the
        XML text representation `text`.  Subclasses may wish to
        override this in order to provide custom typing for certain
        attributes, such as returning an integer or datetime value by
        parsing the text.  By default, the `text` is returned
        unmodified as a string.
        '''
        return text
    
    def parse(self, node):
        '''Initialize this object from the given XML DOM `node`.
        '''
        if node.nodeName != self.getDocumentElementName():
            raise ValueError('Expected %s element but got %s' % (self.getDocumentElementName(), node.nodeName))
        fieldMap = dict(zip([i.elemName for i in self.getDocumentableFields()], self.getDocumentableFields()))
        for child in filter(lambda n: n.nodeType == xml.dom.Node.ELEMENT_NODE, node.childNodes):
            name = child.nodeName
            if name in fieldMap:
                field = fieldMap[name]
                if field.kind == DocumentableField.SINGLE_VALUE_KIND:
                    setattr(self, field.attrName, self.computeValueFromDocument(field.attrName, text(child)))
                elif field.kind == DocumentableField.MULTI_VALUED_KIND:
                    if not hasattr(self, field.attrName):
                        setattr(self, field.attrName, [])
                    getattr(self, field.attrName).append(self.computeValueFromDocument(field.attrName,
                        text(child)))
                else:
                    getattr(self, field.attrName).parse(child)
    
    def __cmp__(self, other):
        '''The documentable fields provide a bonus: we can use them to do comparisons.
        '''
        for field in self.getDocumentableFields():
            attrName = field.attrName
            mine = getattr(self, attrName)
            others = getattr(other, attrName)
            rc = cmp(mine, others)
            if rc < 0:
                return -1
            elif rc > 0:
                return 1
        return 0
    
    def __hash__(self):
        '''The documentable fields provide another bonus: we can use them
        to do hashing.
        '''
        return reduce(lambda x, y: hash(x) ^ hash(y), [getattr(self, i.attrName) for i in self.getDocumentableFields()],
            0x55555555)
    