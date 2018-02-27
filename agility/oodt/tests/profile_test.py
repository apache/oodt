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
from ..profile import ProfileAttributes, ResourceAttributes, Profile, UnspecifiedProfileElement, RangedProfileElement,\
    EnumeratedProfileElement
from testfixtures import compare

class TestProfileAttributes(unittest.TestCase):
    '''Unit test for the ProfileAttributes class.
    '''
    def test_defaults(self):
        '''Test to see if default values are reasonable.
        '''
        pa = ProfileAttributes()
        self.assertEqual('UNKNOWN', pa.id)
        self.assertEqual('1.0.0', pa.version)
        self.assertEqual('profile', pa.type)
        self.assertEqual('active', pa.statusID)
        self.assertEqual('unclassified', pa.securityType)
        self.assertEqual('UNKNOWN', pa.parentID)
        self.assertEqual(0, len(pa.childIDs))
        self.assertEqual('UNKNOWN', pa.regAuthority)
        self.assertEqual(0, len(pa.revNotes))

    def test_cmp(self):
        '''Test comparison operators.
        '''
        a = ProfileAttributes('1')
        b = ProfileAttributes('1')
        c = ProfileAttributes('2')
        self.assertEqual(a, a)
        self.assertEqual(a, b)
        self.assertNotEqual(a, c)
        self.assertTrue(a <= a)
        self.assertTrue(a <= b)
        self.assertTrue(a <= c)
        self.assertTrue(a < c)
        
    def test_xml(self):
        '''Test XML serialization and re-composition from XML.
        '''
        a = ProfileAttributes('1.3.1.9', '2.0.0', 'profile', 'inactive', 'classified', '1.3.1', ['1.3.1.9.1', '1.3.1.9.2'],
            'NASA', ['Updated', 'Created'])
        doc = xml.dom.minidom.getDOMImplementation().createDocument(None, None, None)
        node = a.toXML(doc)
        b = ProfileAttributes(node=node)
        self.assertEqual(a, b)
        
    def test_xml_validity(self):
        '''Test to see if all required XML elements are in there.
        '''
        a = ProfileAttributes('1.3.1.9', '2.0.0', 'profile', 'inactive', 'classified', '1.3.1', ['1.3.1.9.1', '1.3.1.9.2'],
            'NASA', ['Updated', 'Created'])
        doc = xml.dom.minidom.getDOMImplementation().createDocument(None, None, None)
        node = a.toXML(doc)
        self.assertEqual('profAttributes', node.nodeName)
        childElements = [n.nodeName for n in node.childNodes]
        self.assertEqual(['profId', 'profVersion', 'profType', 'profStatusId', 'profSecurityType', 'profParentId',
            'profChildId', 'profChildId', 'profRegAuthority', 'profRevisionNote', 'profRevisionNote'],
            childElements)
    
    def test_instances(self):
        '''Test to ensure instances don't share instance data.
        '''
        a = ProfileAttributes(id='1')
        b = ProfileAttributes(id='2')
        self.assertNotEqual(a, b)
        a.childIDs.append('3')
        self.assertNotEqual(a.childIDs, b.childIDs)
        a.revNotes.append('Uhhhhh, spam?')
        self.assertNotEqual(a.revNotes, b.revNotes)
    
    
class TestResourceAttributes(unittest.TestCase):
    '''Unit test for the ResourceAttributes class.
    '''
    def test_defaults(self):
        '''Test if default values are reasonable.
        '''
        ra = ResourceAttributes()
        self.assertEqual('UNKNOWN', ra.identifier)
        self.assertEqual('UNKNOWN', ra.title)
        self.assertEqual(0, len(ra.formats))
        self.assertEqual('UNKNOWN', ra.description)
        self.assertEqual(0, len(ra.creators))
        self.assertEqual(0, len(ra.subjects))
        self.assertEqual(0, len(ra.publishers))
        self.assertEqual(0, len(ra.contributors))
        self.assertEqual(0, len(ra.dates))
        self.assertEqual(0, len(ra.types))
        self.assertEqual(0, len(ra.sources))
        self.assertEqual(0, len(ra.languages))
        self.assertEqual(0, len(ra.relations))
        self.assertEqual(0, len(ra.coverages))
        self.assertEqual(0, len(ra.rights))
        self.assertEqual(0, len(ra.contexts))
        self.assertEqual('UNKNOWN', ra.aggregation)
        self.assertEqual('UNKNOWN', ra.resClass)
        self.assertEqual(0, len(ra.locations))
    
    def test_cmp(self):
        '''Test comparison operations.
        '''
        a = ResourceAttributes('uri:fish', 'Fish', ['text/html'], 'A book about fish.')
        b = ResourceAttributes('uri:fish', 'Fish', ['text/html'], 'A book about fish.')
        c = ResourceAttributes('uri:clams', 'Clams', ['text/html'], 'A book about clams.')
        self.assertEqual(a, a)
        self.assertEqual(a, b)
        self.assertNotEqual(a, c)
        self.assertTrue(a <= b)
        self.assertTrue(a >= c)
        self.assertTrue(a > c)
        self.assertTrue(a <= a)
    
    def test_xml(self):
        '''Test XML serialization and recomposition from XML.
        '''
        a = ResourceAttributes('uri:fish', 'Fish', ['text/html'], 'A book about fish.')
        doc = xml.dom.minidom.getDOMImplementation().createDocument(None, None, None)
        node = a.toXML(doc)
        b = ResourceAttributes(node=node)
        compare(a, b)
    
    def test_xml_validity(self):
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
        self.assertEqual(['Identifier', 'Title', 'Format', 'Description', 'Creator', 'Subject', 'Subject',
            'Subject', 'Publisher', 'Contributor', 'Type', 'Source', 'Language', 'Relation', 'Coverage',
            'Rights', 'resContext', 'resAggregation', 'resClass', 'resLocation'], childElements)
    
    def test_instances(self):
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
        self.assertNotEqual(a.formats, b.formats)
        self.assertNotEqual(a.creators, b.creators)
        self.assertNotEqual(a.subjects, b.subjects)
        self.assertNotEqual(a.publishers, b.publishers)
        self.assertNotEqual(a.contributors, b.contributors)
        self.assertNotEqual(a.types, b.types)
        self.assertNotEqual(a.languages, b.languages)
        self.assertNotEqual(a.relations, b.relations)
        self.assertNotEqual(a.coverages, b.coverages)
        self.assertNotEqual(a.rights, b.rights)
        self.assertNotEqual(a.contexts, b.contexts)
        self.assertNotEqual(a.locations, b.locations)
    

class TestProfile(unittest.TestCase):
    '''Unit test for class Profile.
    '''
    def test_cmp(self):
        a = Profile()
        b = Profile()
        self.assertEqual(a, b)
        
    def test_xml(self):
        '''Test XML serialization and recomposition from XML.
        '''
        a = Profile()
        doc = xml.dom.minidom.getDOMImplementation().createDocument(None, None, None)
        node = a.toXML(doc)
        b = Profile(node=node)
        self.assertEqual(a, b)

        x = UnspecifiedProfileElement('tastiness', 'How tasty it was', 'char', 'subjective', ['yumminess'],
            'This is highly subjective.')
        y = EnumeratedProfileElement('meal', 'What meal was eaten', 'char', 'meal', ['serving'], 'Typical values',
            ['Breakfast', 'Brunch', 'Lunch', 'Dinner'])
        z = RangedProfileElement('spicyness', 'How spicy it was', 'float', 'scovilles', ['piquancy'],
            'Hotter the better, I say', 0.0, 1000000.0)
        a.profElements['tastiness'], a.profElements['meal'], a.profElements['spicyness'] = x, y, z
        node = a.toXML(doc)
        b = Profile(node=node)
        self.assertEqual(a, b)

    def test_instances(self):
        '''Test to ensure isntances don't share isntance data.
        '''
        a = Profile()
        b = Profile()
        self.assertEqual(a, b)
        a.profElements['a'] = 'b'
        self.assertNotEqual(a, b)
