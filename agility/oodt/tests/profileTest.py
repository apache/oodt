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

'''Unit tests for the `profile` module.
'''

__docformat__ = 'restructuredtext'

import unittest, xml.dom.minidom
from oodt.profile import ProfileAttributes, ResourceAttributes, Profile, UnspecifiedProfileElement, RangedProfileElement,\
    EnumeratedProfileElement

class ProfileAttributesTest(unittest.TestCase):
    '''Unit test for the ProfileAttributes class.
    '''
    def testDefaults(self):
        '''Test to see if default values are reasonable.
        '''
        pa = ProfileAttributes()
        self.assertEquals('UNKNOWN', pa.id)
        self.assertEquals('1.0.0', pa.version)
        self.assertEquals('profile', pa.type)
        self.assertEquals('active', pa.statusID)
        self.assertEquals('unclassified', pa.securityType)
        self.assertEquals('UNKNOWN', pa.parentID)
        self.assertEquals(0, len(pa.childIDs))
        self.assertEquals('UNKNOWN', pa.regAuthority)
        self.assertEquals(0, len(pa.revNotes))

    def testCmp(self):
        '''Test comparison operators.
        '''
        a = ProfileAttributes('1')
        b = ProfileAttributes('1')
        c = ProfileAttributes('2')
        self.assertEquals(a, a)
        self.assertEquals(a, b)
        self.assertNotEquals(a, c)
        self.assert_(a <= a)
        self.assert_(a <= b)
        self.assert_(a <= c)
        self.assert_(a < c)
        
    def testXML(self):
        '''Test XML serialization and re-composition from XML.
        '''
        a = ProfileAttributes('1.3.1.9', '2.0.0', 'profile', 'inactive', 'classified', '1.3.1', ['1.3.1.9.1', '1.3.1.9.2'],
            'NASA', ['Updated', 'Created'])
        doc = xml.dom.minidom.getDOMImplementation().createDocument(None, None, None)
        node = a.toXML(doc)
        b = ProfileAttributes(node=node)
        self.assertEquals(a, b)
        
    def testXMLValidity(self):
        '''Test to see if all required XML elements are in there.
        '''
        a = ProfileAttributes('1.3.1.9', '2.0.0', 'profile', 'inactive', 'classified', '1.3.1', ['1.3.1.9.1', '1.3.1.9.2'],
            'NASA', ['Updated', 'Created'])
        doc = xml.dom.minidom.getDOMImplementation().createDocument(None, None, None)
        node = a.toXML(doc)
        self.assertEquals('profAttributes', node.nodeName)
        childElements = [n.nodeName for n in node.childNodes]
        self.assertEquals([u'profId', u'profVersion', u'profType', u'profStatusId', u'profSecurityType', u'profParentId',
            u'profChildId', u'profChildId', u'profRegAuthority', u'profRevisionNote', u'profRevisionNote'],
            childElements)
    
    def testInstances(self):
        '''Test to ensure instances don't share instance data.
        '''
        a = ProfileAttributes(id='1')
        b = ProfileAttributes(id='2')
        self.assertNotEquals(a, b)
        a.childIDs.append('3')
        self.assertNotEquals(a.childIDs, b.childIDs)
        a.revNotes.append('Uhhhhh, spam?')
        self.assertNotEquals(a.revNotes, b.revNotes)
    
    
class ResourceAttributesTest(unittest.TestCase):
    '''Unit test for the ResourceAttributes class.
    '''
    def testDefaults(self):
        '''Test if default values are reasonable.
        '''
        ra = ResourceAttributes()
        self.assertEquals('UNKNOWN', ra.identifier)
        self.assertEquals('UNKNOWN', ra.title)
        self.assertEquals(0, len(ra.formats))
        self.assertEquals('UNKNOWN', ra.description)
        self.assertEquals(0, len(ra.creators))
        self.assertEquals(0, len(ra.subjects))
        self.assertEquals(0, len(ra.publishers))
        self.assertEquals(0, len(ra.contributors))
        self.assertEquals(0, len(ra.dates))
        self.assertEquals(0, len(ra.types))
        self.assertEquals(0, len(ra.sources))
        self.assertEquals(0, len(ra.languages))
        self.assertEquals(0, len(ra.relations))
        self.assertEquals(0, len(ra.coverages))
        self.assertEquals(0, len(ra.rights))
        self.assertEquals(0, len(ra.contexts))
        self.assertEquals('UNKNOWN', ra.aggregation)
        self.assertEquals('UNKNOWN', ra.resClass)
        self.assertEquals(0, len(ra.locations))
    
    def testCmp(self):
        '''Test comparison operations.
        '''
        a = ResourceAttributes('uri:fish', 'Fish', ['text/html'], 'A book about fish.')
        b = ResourceAttributes('uri:fish', 'Fish', ['text/html'], 'A book about fish.')
        c = ResourceAttributes('uri:clams', 'Clams', ['text/html'], 'A book about clams.')
        self.assertEquals(a, a)
        self.assertEquals(a, b)
        self.assertNotEquals(a, c)
        self.assert_(a <= b)
        self.assert_(a >= c)
        self.assert_(a > c)
        self.assert_(a <= a)
    
    def testXML(self):
        '''Test XML serialization and recomposition from XML.
        '''
        a = ResourceAttributes('uri:fish', 'Fish', ['text/html'], 'A book about fish.')
        doc = xml.dom.minidom.getDOMImplementation().createDocument(None, None, None)
        node = a.toXML(doc)
        b = ResourceAttributes(node=node)
        self.assertEquals(a, b)
    
    def testXMLValidity(self):
        '''Test to see if all required XML elements are in there.
        '''
        a = ResourceAttributes('uri:anus', 'Anus', ['text/html'], 'The anus, rectum, and other parts of the bum.',
            ['Buttman'], ['butts', 'henies', 'booties'], ['Butts and Co Publishing'], ['Dr Eugene Bottomman, III'],
            [], ['reference'], ['The Big Book of Booty'], ['en'], ['Buttholes and other oddities'],
            ['anatomy'], ['Cannot touch this'], ['system.buttServer'], 'granule', 'system.buttServer',
            ['http://butt.info/butt'])
        doc = xml.dom.minidom.getDOMImplementation().createDocument(None, None, None)
        node = a.toXML(doc)
        childElements = [n.nodeName for n in node.childNodes]       
        self.assertEquals([u'Identifier', u'Title', u'Format', u'Description', u'Creator', u'Subject', u'Subject',
            u'Subject', u'Publisher', u'Contributor', u'Type', u'Source', u'Language', u'Relation', u'Coverage',
            u'Rights', u'resContext', u'resAggregation', u'resClass', u'resLocation'], childElements)
    
    def testInstances(self):
        '''Test to ensure instances don't share instance data.
        '''
        a = ResourceAttributes()
        b = ResourceAttributes()
        a.formats.append('text/xml')
        a.creators.append('Dennis Moore')
        a.subjects.append('Silliness')
        a.publishers.append('BBC')
        a.contributors.append('Nigel')
        a.types.append('Video')
        a.languages.append('en')
        a.relations.append('Fawlty Towers')
        a.coverages.append('1970')
        a.rights.append('Abused')
        a.contexts.append('humor')
        a.locations.append('http://bbc.co.uk/')
        self.assertNotEquals(a.formats, b.formats)
        self.assertNotEquals(a.creators, b.creators)
        self.assertNotEquals(a.subjects, b.subjects)
        self.assertNotEquals(a.publishers, b.publishers)
        self.assertNotEquals(a.contributors, b.contributors)
        self.assertNotEquals(a.types, b.types)
        self.assertNotEquals(a.languages, b.languages)
        self.assertNotEquals(a.relations, b.relations)
        self.assertNotEquals(a.coverages, b.coverages)
        self.assertNotEquals(a.rights, b.rights)
        self.assertNotEquals(a.contexts, b.contexts)
        self.assertNotEquals(a.locations, b.locations)
    

class ProfileTest(unittest.TestCase):
    '''Unit test for class Profile.
    '''
    def testCmp(self):
        a = Profile()
        b = Profile()
        self.assertEquals(a, b)
        
    def testXML(self):
        '''Test XML serialization and recomposition from XML.
        '''
        a = Profile()
        doc = xml.dom.minidom.getDOMImplementation().createDocument(None, None, None)
        node = a.toXML(doc)
        b = Profile(node=node)
        self.assertEquals(a, b)

        x = UnspecifiedProfileElement('tastiness', 'How tasty it was', 'char', 'subjective', ['yumminess'],
            'This is highly subjective.')
        y = EnumeratedProfileElement('meal', 'What meal was eaten', 'char', 'meal', ['serving'], 'Typical values',
            ['Breakfast', 'Brunch', 'Lunch', 'Dinner'])
        z = RangedProfileElement('spicyness', 'How spicy it was', 'float', 'scovilles', ['piquancy'],
            'Hotter the better, I say', 0.0, 1000000.0)
        a.profElements['tastiness'], a.profElements['meal'], a.profElements['spicyness'] = x, y, z
        node = a.toXML(doc)
        b = Profile(node=node)
        self.assertEquals(a, b)

    def testInstances(self):
        '''Test to ensure isntances don't share isntance data.
        '''
        a = Profile()
        b = Profile()
        self.assertEquals(a, b)
        a.profElements['a'] = 'b'
        self.assertNotEquals(a, b)
    

def test_suite():
    '''Make the test suite.
    '''
    import doctest
    suite = unittest.TestSuite()
    suite.addTest(unittest.makeSuite(ProfileAttributesTest))
    suite.addTest(unittest.makeSuite(ResourceAttributesTest))
    suite.addTest(unittest.makeSuite(ProfileTest))
    return suite
    

if __name__ == '__main__':
    unittest.main(defaultTest='test_suite')
