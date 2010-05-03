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

'''Unit tests for the `xmlutils` module.
'''

__docformat__ = 'restructuredtext'

import unittest, xml.dom, oodt.xmlutils
from oodt.xmlutils import DocumentableField

class XMLTest(unittest.TestCase):
    '''Unit test the XML utilities.
    '''
    def testText(self):
        '''Test getting text from nodes.
        '''
        domImpl = xml.dom.getDOMImplementation()
        doc = domImpl.createDocument(None, None, None) # ns URI, qual name, doctype
        root = doc.createElement('root')
        root.appendChild(doc.createTextNode('Hello'))
        child = doc.createElement('child')
        root.appendChild(child)
        root.appendChild(doc.createCDATASection('world'))
        child.appendChild(doc.createTextNode('cruel'))
        empty = doc.createElement('empty')
        root.appendChild(empty)

        self.assertEquals('cruel', oodt.xmlutils.text(child))
        self.assertEquals(0, len(oodt.xmlutils.text(empty)))
        self.assertEquals('Hellocruelworld', oodt.xmlutils.text(root))
    
    def testAdds(self):
        '''Test adding things to nodes.
        '''
        domImpl = xml.dom.getDOMImplementation()
        doc = domImpl.createDocument(None, None, None) # ns URI, qual name, doctype
        root = doc.createElement('root')

        oodt.xmlutils.add(root, 'first')
        self.assertEquals(0, len(oodt.xmlutils.text(root)))
        oodt.xmlutils.add(root, 'second', 'with text')
        self.assertEquals('with text', oodt.xmlutils.text(root))
        
        oodt.xmlutils.add(root, 'number', [str(i) for i in range(1, 4)])
        self.assertEquals('with text123', oodt.xmlutils.text(root))
    

class DocumentableTest(unittest.TestCase):
    '''Unit test the `Documentable` and `DocumentableField` classes.
    '''
    def testIt(self):
        '''Test the `Documentable` and `DocumentableField` classes.
        '''
        class X(oodt.xmlutils.Documentable):
            def __init__(self, mission='UNK', target='UNK', measurements=[], node=None):
                self.mission, self.target, self.measurements = mission, target, measurements
                if node is not None:
                    self.parse(node)
            def computeValueFromDocument(self, attrName, text):
                if attrName == 'measurements':
                    return int(text)
                else:
                    return text
            def getDocumentElementName(self):
                return 'X'
            def getDocumentableFields(self):
                return (DocumentableField('mission', u'MISSION', DocumentableField.SINGLE_VALUE_KIND),
                DocumentableField('target', u'TARGET', DocumentableField.SINGLE_VALUE_KIND),
                DocumentableField('measurements', u'MZR', DocumentableField.MULTI_VALUED_KIND))
            def __eq__(self, other):
                return (self.mission == other.mission and self.target == other.target
                    and cmp(self.measurements, other.measurements) == 0)
            def __str__(self):
                return '%s,%s:%s' % (self.mission, self.target, self.measurements)
        a = X('Explorer', 'Moon', [1, 2, 3])
        doc = xml.dom.getDOMImplementation().createDocument(None, None, None)
        node = a.toXML(doc)
        b = X(node=node)
        self.assertEquals(a, b)
    

def test_suite():
    '''Create the suite of tests.
    '''
    import doctest
    suite = unittest.TestSuite()
    suite.addTest(unittest.makeSuite(XMLTest))
    suite.addTest(unittest.makeSuite(DocumentableTest))
    suite.addTest(doctest.DocTestSuite(oodt.xmlutils))
    return suite
    

if __name__ == '__main__':
    unittest.main(defaultTest='test_suite')
