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

'''
Agile OODT Query Expressions.

Query expressions in OODT are based on the "DIS" style expressions originally
developed for the Planetary Data System.  They consist of
keyword/operator/literal-value triples, such as `targetName = Mars`, each
linked with logical operators (and, or, not) and grouped with parentheses.
For more information, see OODT_.

This module defines classes that model the aspects of a query.  In general,
use the `Query` class, passing a string containing your keyword expression as
the first constructor argument.  From there, you have a `Query` object you can
pass around to profile servers, product servers, and so forth.

.. _OODT: http://oodt.apache.org/
'''

__docformat__ = 'restructuredtext'

import oodterrors, shlex, xmlutils, xml.dom
from xmlutils import DocumentableField

class _QueryExpressionScanner(shlex.shlex):
    '''Extend the shlex scanner but for the DIS-style query expressions we expect.
    This means adding a dot to the characters that comprise a word so we can easily parse
    floating-point numbers.  Also, there's no comment character.
    '''
    def __init__(self, str):
        '''Create scanner.  `str` is the string to scan.
        '''
        shlex.shlex.__init__(self, str)
        self.commenters = ''
        self.wordchars = self.wordchars + '.-/:'
        

    def get_token(self):
        '''Get the next token.  We strip quotes from strings, attach negative signs
        to the numbers they're negating, and attach the = to <, >, and ! where needed.
        '''
        token = shlex.shlex.get_token(self)
        if token == self.eof or token == None:
            return None
        if token[0] in self.quotes:
            token = token[1:-1]
        elif token in ('<', '>', '!'):
            next = shlex.shlex.get_token(self)
            if next == self.eof or next == None:
                return None
            elif next == '=':
                token = token + next
            else:
                self.push_token(next)
        return token
    

class QueryException(oodterrors.OODTException):
    '''Exceptions related to query expression or query services
    '''
    pass
    

class ExpressionParseError(QueryException):
    '''Error in parsing a query expression.
    '''
    pass
    

class QueryElement(xmlutils.Documentable):
    '''An element of a query.
    '''
    def __init__(self, role='UNKNOWN', value='UNKNOWN', node=None):
        '''Create a QueryElement.  You can provide role and value settings, or provide an XML
        DOM node which will be parsed for role/value.
        '''
        self.role, self.value = role, value
        if node != None:
            self.parse(node)

    def getDocumentElementName(self):
        '''Give the XML tag name: `queryElement`.
        '''
        return 'queryElement'
    
    def getDocumentableFields(self):
        '''Get the attributes that go into XML.
        '''
        return (DocumentableField('role', u'tokenRole', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('value', u'tokenValue', DocumentableField.SINGLE_VALUE_KIND))
    
    def __repr__(self):
        return 'QueryElement(role="%s",value="%s")' % (self.role, self.value)
    

class QueryHeader(xmlutils.Documentable):
    '''Header of a query.  Captures metadata like data dictionary in use, etc.
    '''
    def __init__(self, id='UNKNOWN', title='UNKNOWN', desc='UNKNOWN', type='QUERY', status='ACTIVE', security='UNKNOWN',
        rev='2005-10-01 SCK v0.0.0 Under Development', dataDict='UNKNOWN', node=None):
        '''Initialize a QueryHeader.  Provide id, title, desc, type, status, security, rev,
        and dataDict settings.  Or, provide just an XML DOM node to be parsed.
        '''
        self.id, self.title, self.desc, self.type, self.status, self.security, self.rev, self.dataDict = \
            id, title, desc, type, status, security, rev, dataDict
        if node != None:
            self.parse(node)
    
    def getDocumentElementName(self):
        '''Give the XML tag name: `queryAttributes`.
        '''
        return 'queryAttributes'
    
    def getDocumentableFields(self):
        '''Get the attributes that go into XML.
        '''
        return (DocumentableField('id', u'queryId', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('title', u'queryTitle', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('desc', u'queryDesc', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('type', u'queryType', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('status', u'queryStatusId', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('security', u'querySecurityType', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('rev', u'queryRevisionNote', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('dataDict', u'queryDataDictId', DocumentableField.SINGLE_VALUE_KIND))
    
    def __repr__(self):
        return 'QueryHeader(id="%s",title="%s",desc="%s",type="%s",status="%s",security="%s",rev="%s",dataDict="%s")' % (
            self.id, self.title, self.desc, self.type, self.status, self.security, self.rev, self.dataDict
        )
    
    def __cmp__(self, other):
        return cmp((self.id, self.title, self.desc, self.type, self.status, self.security, self.rev, self.dataDict),
               (other.id, other.title, other.desc, other.type, other.status, other.security, other.rev, other.dataDict))
    

class QueryResult(object):
    '''Result of a query.
    '''
    def __init__(self, results=[], node=None):
        '''Results of a query are captured as a sequence of generic objects.
        '''
        self.results = results
        if node != None: self.parse(node)
    
    def parse(self, node):
        '''Initialize this object from the given XML DOM `node`.
        '''
        if 'queryResultSet' != node.nodeName:
            raise ValueError('Expected queryResultSet but got "%s"' % node.nodeName)
        for child in node.childNodes:
            if child.nodeType == xml.dom.Node.ELEMENT_NODE and 'resultElement' == child.nodeName:
                self.results.append(Result(node=child))
    
    def toXML(self, owner):
        '''Convert this object into XML owned by the given `owner` document.
        '''
        root = owner.createElement('queryResultSet')
        for result in self.results:
            root.appendChild(result.toXML(owner))
        return root
    
    def clear(self):
        '''Remove all results.
        '''
        self.results = []
    
    def __getitem__(self, i):
        return self.results[i]
    
    def __len__(self):
        return len(self.results)
    
    def __cmp__(self, other):
        return cmp(self.results, other.results)
    

_RELOPS = {
    'LT':      'LT', 
    'lt':      'LT', 
    '<':       'LT',
    'LE':      'LE',
    'le':      'LE',
    '<=':      'LE',
    'EQ':      'EQ', 
    'eq':      'EQ', 
    '=':       'EQ',
    'GE':      'GE',
    'ge':      'GE',
    '>=':      'GE',
    'GT':      'GT',
    'gt':      'GT',
    '>':       'GT',
    'NE':      'NE',
    'ne':      'NE',
    '!=':      'NE',
    'LIKE':    'LIKE',
    'like':    'LIKE',
    'NOTLIKE': 'NOTLIKE',
    'notlike': 'NOTLIKE',
    'notLike': 'NOTLIKE',
    'IS':      'IS',
    'is':      'is',
    'ISNOT':   'ISNOT',
    'isnot':   'isnot',
    'isNot':   'isnot'
}

_LOGOPS = {
    'AND': 'AND',
    'and': 'AND',
    '&':   'AND',
    'OR':  'OR',
    'or':  'OR',
    '|':   'OR',
    'NOT': 'NOT',
    'not': 'NOT',
    '!':   'NOT'
}

_PRECEDENCE = {
    'NOT': 2,
    'AND': 1,
    'OR': 0,
}

class Query(object):
    '''Query.  In old OODT, this was called XMLQuery, even though XML was tagential to it.
    Captures aspects of a query, including header, results, and so forth.  Most importantly, it
    captures the query expression, which contains the constraints on user's desiderata and
    range on what to return.
    
    As with other classes in this module, you can provide an XML DOM node to be parsed
    for the query's settings, or provide each of them individually.
    '''
    def __init__(self, keywordQuery=None, header=QueryHeader(), resultModeID='ATTRIBUTE', propType='BROADCAST',
        propLevels='N/A', maxResults=1, mimeAccept=[], parseQuery=True, node=None):
        '''Initialize a query.  Usually you provide just the `keywordQuery` argument
        which should be a keyword/query expression in the DIS-style; or you provide
        the `node` which is an XML DOM node describing a query.
        '''
        self.header, self.resultModeID, self.propType, self.propLevels, self.maxResults, self.mimeAccept = \
            header, resultModeID, propType, propLevels, maxResults, mimeAccept
        self.wheres, self.selects, self.froms, self.resultSet = [], [], [], QueryResult()
        if keywordQuery != None:
            self.keywordQuery = keywordQuery
            if parseQuery:
                self.wheres, self.selects = _parseQuery(keywordQuery)
        else:
            self.keywordQuery = ''
        if node != None:
            self.parse(node)
        
    def toXML(self, owner):
        '''Yield this query as an XML DOM.
        '''
        query = owner.createElement('query')
        query.appendChild(self.header.toXML(owner))
        xmlutils.add(query, u'queryResultModeId', self.resultModeID)
        xmlutils.add(query, u'queryPropogationType', self.propType)
        xmlutils.add(query, u'queryPropogationLevels', self.propLevels)
        for mimeType in self.mimeAccept:
            xmlutils.add(query, u'queryMimeAccept', mimeType)
        xmlutils.add(query, u'queryMaxResults', str(self.maxResults))
        xmlutils.add(query, u'queryKWQString', self.keywordQuery)
        selects = owner.createElement(u'querySelectSet')
        query.appendChild(selects)
        for select in self.selects:
            selects.appendChild(select.toXML(owner))
        fromElement = owner.createElement(u'queryFromSet')
        query.appendChild(fromElement)
        for i in self.froms:
            fromElement.appendChild(i.toXML(owner))
        wheres = owner.createElement(u'queryWhereSet')
        query.appendChild(wheres)
        for where in self.wheres:
            wheres.appendChild(where.toXML(owner))
        query.appendChild(self.resultSet.toXML(owner))
        return query
        
    def parse(self, node):
        '''Parse the XML DOM node as a query document.
        '''
        if 'query' != node.nodeName:
            raise ValueError('Expected query but got "%s"' % node.nodeName)
        self.mimeAccept, self.results = [], []
        for child in node.childNodes:
            if child.nodeType == xml.dom.Node.ELEMENT_NODE:
                if child.nodeName == u'queryAttributes':
                    self.header = QueryHeader(node=child)
                elif child.nodeName == u'resultModeID':
                    self.resultModeID = xmlutils.text(child)
                elif child.nodeName == u'queryPropogationType':
                    self.propType = xmlutils.text(child)
                elif child.nodeName == u'queryPropogationLevels':
                    self.propLevels = xmlutils.text(child)
                elif child.nodeName == u'queryMimeAccept':
                    self.mimeAccept.append(xmlutils.text(child))
                elif child.nodeName == u'queryMaxResults':
                    self.maxResults = int(xmlutils.text(child))
                elif child.nodeName == u'queryKWQString':
                    self.keywordQuery = xmlutils.text(child)
                elif child.nodeName == u'querySelectSet':
                    self.selects = _parseQueryElements(child)
                elif child.nodeName == u'queryFromSet':
                    self.froms = _parseQueryElements(child)
                elif child.nodeName == u'queryWhereSet':
                    self.wheres = _parseQueryElements(child)
                elif child.nodeName == u'queryResultSet':
                    self.resultSet = QueryResult(node=child)
    
    def __cmp__(self, other):
        header = cmp(self.header, other.header)
        if header < 0:
            return -1
        elif header == 0:
            resultModeID = cmp(self.resultModeID, other.resultModeID)
            if resultModeID < 0:
                return -1
            elif resultModeID == 0:
                propType = cmp(self.propType, other.propType)
                if propType < 0:
                    return -1
                elif propType == 0:
                    propLevels = cmp(self.propLevels, other.propLevels)
                    if propLevels < 0:
                        return -1
                    elif propLevels == 0:
                        maxResults = self.maxResults - other.maxResults
                        if maxResults < 0:
                            return -1
                        elif maxResults == 0:
                            mimeAccept = cmp(self.mimeAccept, other.mimeAccept)
                            if mimeAccept < 0:
                                return -1
                            elif mimeAccept == 0:
                                selects = cmp(self.selects, other.selects)
                                if selects < 0:
                                    return -1
                                elif selects == 0:
                                    froms = cmp(self.froms, other.froms)
                                    if froms < 0:
                                        return -1
                                    elif froms == 0:
                                        wheres = cmp(self.wheres, other.wheres)
                                        if wheres < 0:
                                            return -1
                                        elif wheres == 0:
                                            return cmp(self.resultSet, other.resultSet)
        return 1
    

def _parseQueryElements(node):
    '''The children of the given XML DOM node are a sequence of queryElements.  Parse them
    and return a list of QueryElement objects.
    '''
    a = []
    for child in node.childNodes:
        if child.nodeType == xml.dom.Node.ELEMENT_NODE:
            a.append(QueryElement(node=child))
    return a
    

def _parseQuery(s):
    '''Parse the query expression in `s`.
    '''
    if s is None:
        return [], []
    if len(s.strip()) == 0:
        return [], []
    if s.count('(') != s.count(')'):
        raise ExpressionParseError('Unbalanced parentheses')
    scanner = _QueryExpressionScanner(s)
    return _buildQuery(scanner)
    

def _buildQuery(scanner):
    '''Build the query stacks using the given `scanner`.
    '''
    operators, expression, selectors = [], [], []
    while True:
        token = scanner.get_token()
        if token is None: break
        if token in _LOGOPS:
            op = QueryElement('LOGOP', _LOGOPS[token])
            if len(operators) == 0:
                operators.append(op)
            else:
                while len(operators) > 0 and _PRECEDENCE[operators[-1].value] > _PRECEDENCE[op.value]:
                    expression.append(operators.pop())
                operators.append(op)
        elif token == '(':
            subExpr, subSelectors = _buildQuery(scanner)
            expression.extend(subExpr)
            selectors.extend(subSelectors)
        elif token == ')':
            break
        else:
            _addTerm(token, scanner, expression, operators, selectors)
    if len(operators) > 0 and len(expression) == 0:
        raise ExpressionParseError('Query contains only logical operators')
    operators.reverse()
    expression.extend(operators)
    return expression, selectors
    

def _addTerm(elemName, scanner, expression, operators, selectors):
    '''Add a term to the correct stack.
    '''
    relop = scanner.get_token()
    if relop is None:
        raise ExpressionParseError('Expected relational operator after element name "%s"' % elemName)
    if relop not in _RELOPS:
        raise ExpressionParseError('Unknown relational operator "%s"' % relop)
    literal = scanner.get_token()
    if literal is None:
        raise ExpressionParseError('Expected literal value for "%s %s" comparison' % (elemName, relop))
    if elemName == 'RETURN':
        selectors.append(QueryElement('elemName', literal))
        if len(operators) > 0:
            operators.pop()
        else:
            scanner.get_token()
    else:
        expression.append(QueryElement('elemName', elemName))
        expression.append(QueryElement('LITERAL', literal))
        expression.append(QueryElement('RELOP', _RELOPS[relop]))
    

# Sample code:
# if __name__ == '__main__':
#   import urllib, xml.dom.minidom
# 
#   impl = xml.dom.minidom.getDOMImplementation()
#   doc = impl.createDocument(None, None, None) # nsURI, qName, docType
# 
#   q = Query('track = Innocente')
#   node = q.toXML(doc)
#   doc.appendChild(node)
#   q = doc.toxml()
#   f = urllib.urlopen('http://localhost:8080/pds/prof', urllib.urlencode(dict(xmlq=q)), {}) # url, postdata, proxies (none)
#   print f.read()
