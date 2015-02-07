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

'''Unit tests for the query module.

This module tests various query classes and the query expression parser.
'''

__docformat__ = 'restructuredtext'

import unittest, xml.dom, oodt.xmlutils
from oodt.query import QueryElement, QueryHeader, QueryResult, Query, _parseQuery, ExpressionParseError

class QueryElementTest(unittest.TestCase):
    '''Test the `QueryElement` class.
    '''
    def testDefaults(self):
        '''Test to see if defaults are reasonable.
        '''
        qe = QueryElement()
        self.assertEqual('UNKNOWN', qe.role)
        self.assertEqual('UNKNOWN', qe.value)

    def testArgs(self):
        '''Test to see if initializer arguments are used.
        '''
        qe = QueryElement(role='role', value='value')
        self.assertEqual('role', qe.role)
        self.assertEqual('value', qe.value)
    
    def testComparisons(self):
        '''Test comparison operators.
        '''
        a = QueryElement('a', '1')
        b = QueryElement('a', '1')
        c = QueryElement('b', '1')
        d = QueryElement('a', '2')
        self.assertEqual(a, a)
        self.assertEqual(a, b)
        self.assertEqual(b, a)
        self.assertNotEqual(a, c)
        self.assertNotEqual(c, a)
        self.assertNotEqual(a, d)
        self.assertNotEqual(d, a)
        self.assertNotEqual(c, d)
        self.assert_(a <= a)
        self.assert_(a <= c)
        self.assert_(a < c)
        self.assert_(a <= d)
        self.assert_(a < d)
    
    def testBadArgs(self):
        '''Test reactions to bad arugments.
        '''
        domImpl = xml.dom.getDOMImplementation()
        doc = domImpl.createDocument(None, None, None) # namespace URI, qualified Name, doctype
        self.assertRaises(ValueError, QueryElement, node=doc.createElement('notAQueryElement'))
    
    def testXML(self):
        '''Test XML serialization.
        '''
        domImpl = xml.dom.getDOMImplementation()
        doc = domImpl.createDocument(None, None, None) # namespace URI, qualified Name, doctype
        q1 = QueryElement(role='a', value='1')
        root = q1.toXML(doc)
        self.assertEqual('queryElement', root.nodeName)
        for child in root.childNodes:
            if 'tokenRole' == child.nodeName:
                self.assertEquals('a', oodt.xmlutils.text(child))
            elif 'tokenValue' == child.nodeName:
                self.assertEquals('1', oodt.xmlutils.text(child))
            else:
                self.fail('Unknown node "' + child.nodeName + '" in XML result')

        q2 = QueryElement(node=root)
        self.assertEqual(q1, q2)

        root = doc.createElement('queryElement')
        doc.appendChild(root)
        elem = doc.createElement('tokenRole')
        root.appendChild(elem)
        elem.appendChild(doc.createTextNode('a'))
        elem = doc.createElement('tokenValue')
        root.appendChild(elem)
        elem.appendChild(doc.createTextNode('2'))
        q3 = QueryElement(node=root)
        self.assertNotEqual(q1, q3)
    

_QUERY_HEADER_ATTRS = {
    'queryId': 'id',
    'queryTitle': 'title',
    'queryDesc': 'desc',
    'queryType': 'type',
    'queryStatusId': 'status',
    'querySecurityType': 'security',
    'queryRevisionNote': 'rev',
    'queryDataDictId': 'dataDict'
}

class QueryHeaderTest(unittest.TestCase):
    '''Unit test for the `QueryHeader` class.
    '''
    def testDefaults(self):
        '''Test if defaults are reasonable.
        '''
        qh = QueryHeader()
        self.assertEqual('UNKNOWN', qh.id)
        self.assertEqual('UNKNOWN', qh.title)
        self.assertEqual('UNKNOWN', qh.desc)
        self.assertEqual('QUERY', qh.type)
        self.assertEqual('ACTIVE', qh.status)
        self.assertEqual('UNKNOWN', qh.security)
        self.assertEqual('2005-10-01 SCK v0.0.0 Under Development', qh.rev)
        self.assertEqual('UNKNOWN', qh.dataDict)

    def testArgs(self):
        '''Test if initializer arguments are used.
        '''
        qh = QueryHeader('id', 'title', 'desc', 'type', 'status', 'security', 'rev', 'dataDict')
        self.assertEqual(qh.id, 'id')
        self.assertEqual(qh.title, 'title')
        self.assertEqual(qh.desc, 'desc')
        self.assertEqual(qh.type, 'type')
        self.assertEqual(qh.status, 'status')
        self.assertEqual(qh.security, 'security')
        self.assertEqual(qh.rev, 'rev')
        self.assertEqual(qh.dataDict, 'dataDict')

    def testBadArgs(self):
        '''Test reaction to bad arguments.
        '''
        domImpl = xml.dom.getDOMImplementation()
        doc = domImpl.createDocument(None, None, None) # namespace URI, qualified Name, doctype
        self.assertRaises(ValueError, QueryHeader, node=doc.createElement('notAQueryHeader'))

    def testXML(self):
        '''Test XML serialization.
        '''
        domImpl = xml.dom.getDOMImplementation()
        doc = domImpl.createDocument(None, None, None) # namespace URI, qualified Name, doctype
        q1 = QueryHeader('id', 'title', 'desc', 'type', 'status', 'security', 'rev', 'dataDict')
        root = q1.toXML(doc)
        self.assertEqual('queryAttributes', root.nodeName)
        for child in root.childNodes:
            self.check(child.nodeName, oodt.xmlutils.text(child))
        q2 = QueryHeader(node=root)
        self.assertEqual(q1, q2)

    def check(self, name, value):
        '''Check if the given tag name is valid.
        '''
        if name in _QUERY_HEADER_ATTRS:
            self.assertEqual(value, _QUERY_HEADER_ATTRS[name])
        else:
            fail('Unknown element ' + name + ' in query header')
    

class QueryResultTest(unittest.TestCase):
    '''Unit test for the `QueryResults` class.
    '''
    def testDefaults(self):
        '''Test if defaults are reasonable.
        '''
        qr = QueryResult()
        self.assertEquals(0, len(qr.results))
        self.assertEquals(0, len(qr))
        self.assertRaises(IndexError, qr.__getitem__, 0)

    def testBadArgs(self):
        '''Test reaction to bad arguments.
        '''
        domImpl = xml.dom.getDOMImplementation()
        doc = domImpl.createDocument(None, None, None) # namespace URI, qualified Name, doctype
        self.assertRaises(ValueError, QueryResult, node=doc.createElement('notAQueryResult'))

    def testXML(self):
        '''Test XML serialization.
        '''
        domImpl = xml.dom.getDOMImplementation()
        doc = domImpl.createDocument(None, None, None) # namespace URI, qualified Name, doctype
        q1 = QueryResult()
        root = q1.toXML(doc)
        self.assertEqual('queryResultSet', root.nodeName)
        q2 = QueryResult(node=root)
        self.assertEqual(q1, q2)
    

class QueryTest(unittest.TestCase):
    '''Unit test for the `Query` class.
    '''
    def testDefaults(self):
        '''Test if defaults are reasonable.
        '''
        q = Query()
        self.assertEqual(QueryHeader(), q.header)
        self.assertEqual('ATTRIBUTE', q.resultModeID)
        self.assertEqual('BROADCAST', q.propType)
        self.assertEqual('N/A', q.propLevels)
        self.assertEqual(1, q.maxResults)
        self.assertEqual(0, len(q.mimeAccept))

    def testParser(self):
        '''Test the query expresion parser.
        '''
        # Empty
        self.assertEqual(([], []), _parseQuery(''))
        
        # Simple
        self.assertEqual(([ QueryElement(role='elemName', value='x'), QueryElement(role='LITERAL', value='1'),
            QueryElement(role='RELOP', value='EQ') ], []), _parseQuery('x = 1'))

        # Logical or
        self.assertEqual(([ QueryElement(role='elemName', value='x'), QueryElement(role='LITERAL', value='1'),      
            QueryElement(role='RELOP', value='LT'), QueryElement(role='elemName', value='y'),
            QueryElement(role='LITERAL', value='-2'), QueryElement(role='RELOP', value='GT'),
            QueryElement(role='LOGOP', value='OR') ], []), _parseQuery('x < 1 or y > -2'))

        # Logical and has higher precendence
        self.assertEqual(([ QueryElement(role='elemName', value='x'), QueryElement(role='LITERAL', value='1'),
            QueryElement(role='RELOP', value='LE'), QueryElement(role='elemName', value='y'),
            QueryElement(role='LITERAL', value='2'), QueryElement(role='RELOP', value='GE'),
            QueryElement(role='elemName', value='z'), QueryElement(role='LITERAL', value='-3.96'),
            QueryElement(role='RELOP', value='NE'), QueryElement(role='LOGOP', value='AND'),
            QueryElement(role='LOGOP', value='OR') ], []), _parseQuery('x <= 1 | y >= 2 and z != -3.96'))
        
        # Logical or has lower precedence
        self.assertEqual(([ QueryElement(role='elemName', value='x'), QueryElement(role='LITERAL', value='1'),
            QueryElement(role='RELOP', value='LT'), QueryElement(role='elemName', value='y'),
            QueryElement(role='LITERAL', value='2'), QueryElement(role='RELOP', value='GT'),
            QueryElement(role='LOGOP', value='AND'), QueryElement(role='elemName', value='z'),
            QueryElement(role='LITERAL', value='3'), QueryElement(role='RELOP', value='NE'), 
            QueryElement(role='LOGOP', value='OR') ], []), _parseQuery('x LT 1 & y GT 2 or z NE 3'))

        # Parenthesis
        self.assertEqual(([ QueryElement(role='elemName', value='x'), QueryElement(role='LITERAL', value='1'),
            QueryElement(role='RELOP', value='LE'), QueryElement(role='elemName', value='y'), \
            QueryElement(role='LITERAL', value='2'), QueryElement(role='RELOP', value='GE'),
            QueryElement(role='LOGOP', value='OR'), QueryElement(role='elemName', value='z'),
            QueryElement(role='LITERAL', value='3'), QueryElement(role='RELOP', value='EQ'), 
            QueryElement(role='LOGOP', value='AND') ], []), _parseQuery('(x LE 1 or y GE 2) and z EQ 3'))

        # Logical not
        self.assertEqual(([ QueryElement(role='elemName', value='x'), QueryElement(role='LITERAL', value='1'),
            QueryElement(role='RELOP', value='LE'), QueryElement(role='elemName', value='y'),
            QueryElement(role='LITERAL', value='2'), QueryElement(role='RELOP', value='GE'),
            QueryElement(role='LOGOP', value='OR'), QueryElement(role='LOGOP', value='NOT'),
            QueryElement(role='elemName', value='z'), QueryElement(role='LITERAL', value='3'),
            QueryElement(role='RELOP', value='EQ'), QueryElement(role='LOGOP', value='NOT'),
            QueryElement(role='LOGOP', value='AND') ], []), _parseQuery('not (x LE 1 or y GE 2) and ! z EQ 3'))

        # Quoted strings
        self.assertEqual(([ QueryElement(role='elemName', value='x'), QueryElement(role='LITERAL', value='Fish Poop'),
            QueryElement(role='RELOP', value='EQ'), QueryElement(role='elemName', value='y'),
            QueryElement(role='LITERAL', value='Monkey Poop'), QueryElement(role='RELOP', value='EQ'),
            QueryElement(role='LOGOP', value='AND') ], []),
            _parseQuery("""x = "Fish Poop" and y = 'Monkey Poop'"""))
                
        # RETURN elements
        self.assertEqual(([ QueryElement(role='elemName', value='x'), QueryElement(role='LITERAL', value='Fish Poop'),
            QueryElement(role='RELOP', value='EQ'), QueryElement(role='elemName', value='y'),
            QueryElement(role='LITERAL', value='Monkey Poop'), QueryElement(role='RELOP', value='EQ'),
            QueryElement(role='LOGOP', value='AND') ], [QueryElement(role='elemName', value='fish')]),
            _parseQuery("""x = "Fish Poop" and RETURN > fish and y = 'Monkey Poop'"""))
        
        # RETURN at beginning and middle
        self.assertEqual(([ QueryElement(role='elemName', value='x'), QueryElement(role='LITERAL', value='Fish Poop'),
            QueryElement(role='RELOP', value='EQ'), QueryElement(role='elemName', value='y'),
            QueryElement(role='LITERAL', value='Monkey Poop'), QueryElement(role='RELOP', value='EQ'),
            QueryElement(role='LOGOP', value='AND') ], [QueryElement(role='elemName', value='fish'),
            QueryElement(role='elemName', value='poop')]),
            _parseQuery("""RETURN < fish and x = "Fish Poop" and RETURN > poop and y = 'Monkey Poop'"""))
            
        # Bad expressions
        for expr in ('heya', 'RETURN =', 'AND', 'LAT ~ fish', 'NOT', '(', ')'):
            self.assertRaises(ExpressionParseError, _parseQuery, expr)
            
        # Unusual symbols
        self.assertEqual(([QueryElement('elemName', 'DATA_SET_ID'), QueryElement('LITERAL', 'ARCB-L-RTLS-3-70CM-V1.0'),
            QueryElement('RELOP', 'EQ')], []), _parseQuery('''DATA_SET_ID = ARCB-L-RTLS-3-70CM-V1.0'''))
        self.assertEqual(([QueryElement('elemName', 'file'), QueryElement('LITERAL', '/usr/local/bin/poop'),
            QueryElement('RELOP', 'EQ')], []), _parseQuery('''file = /usr/local/bin/poop'''))
        self.assertEqual(([QueryElement('elemName', 'start_time'), QueryElement('LITERAL', '2006-02-06T13:12:13'),
            QueryElement('RELOP', 'EQ')], []), _parseQuery('''start_time = 2006-02-06T13:12:13'''))
    
    def testCmp(self):
        '''Test comparison operators.
        '''
        a = Query('lat > 3 and lon < -92.6')
        b = Query('lat > 3 and lon < -92.6')
        c = Query('lat < 3 and lon > -92.6')
        self.assert_(a == a)
        self.assert_(a == b)
        self.assert_(a < c)
        self.assert_(c > a)
        self.assert_(a <= b)
        self.assert_(a >= b)
        self.assert_(a <= c)
        self.assert_(c >= a)
        self.assert_(c != a)

    def testXML(self):
        '''Test XML serialization.
        '''
        domImpl = xml.dom.getDOMImplementation()
        doc = domImpl.createDocument(None, None, None) # namespace URI, qualified Name, doctype
        q1 = Query('lat > 3 and lon < -92.6')
        root = q1.toXML(doc)
        self.assertEqual('query', root.nodeName)
        q2 = Query(node=root)
        self.assertEqual(q1, q2)
    

def test_suite():
    '''Create the suite of tests.
    '''
    suite = unittest.TestSuite()
    suite.addTest(unittest.makeSuite(QueryElementTest))
    suite.addTest(unittest.makeSuite(QueryHeaderTest))
    suite.addTest(unittest.makeSuite(QueryResultTest))
    suite.addTest(unittest.makeSuite(QueryTest))
    return suite
    

if __name__ == '__main__':
    unittest.main(defaultTest='test_suite')
    
