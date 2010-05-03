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

'''Profiles.  A profile is a metadata description of a resource.

Profiles capture:

* Inception metadata.  This includes items such as a resouce's title,
  description, creators, contributors, publishers, language, and so on.  This
  set of metdata is based on the Dublin Core.  Class `ResourceAttributes`
  captures this metadata.

* Composition metadata.  This includes data elements that describe what
  the resource contains, such as ranges of data values (high and low
  temperature, latitude/longitude) or enumerated data values (zoning code,
  genus/species, etc.).  Subclasses of `ProfileElement` captures this
  metadata.

* Profile metadata.  This is metadata describing the profile itself, such
  as revision notes, ID number, etc.  Class `ProfileAttributes` captures this
  metadata.

Objects of these classes are unified into a single `Profile` object.
'''

__docformat__ = 'restructuredtext'

import xmlutils
import xml.dom
from xmlutils import DocumentableField

class ProfileAttributes(xmlutils.Documentable):
    '''Attributes of a profile.  These are attributes not related to the
    resource that a profile profiles, but rather the profile itself.  In
    most cases, simply constructing this object with no initializer
    arguments will suffice.
    '''
    def __init__(self, id='UNKNOWN', version='1.0.0', type='profile', statusID='active', securityType='unclassified',
        parentID='UNKNOWN', childIDs=None, regAuthority='UNKNOWN', revNotes=None, node=None):
        '''Initialize profile attributes.
        
        - `id` unique ID for the profile
        - `version` number
        - `type` should always be the string "profile"
        - `statusID` should always be the string "active"
        - `securityType` tells whether the profile is secret
        - `parentID` gives the unique ID for any "parent" profile
        - `childIDs` should be a sequnece of any "children" profiles
        - `regAuthority` is a unique ID identifying the registration
           authority responsible for the profile
        - `revNotes` is a sequence of comments detailing historical
           changes to the profile
        - `node` is an XML DOM node.  If the `node` argument is given,
           it's used to initialize the object.
        '''
        self.id = id
        self.version = version
        self.type = type
        self.statusID = statusID
        self.securityType = securityType
        self.parentID = parentID
        self.childIDs = childIDs
        self.regAuthority = regAuthority
        self.revNotes = revNotes
        if self.childIDs is None:
            self.childIDs = []
        if self.revNotes is None:
            self.revNotes = []
        if node is not None:
            self.parse(node)
    
    def getDocumentElementName(self):
        '''Get the XML tag for objects of this class: `profAttributes`.
        '''
        return 'profAttributes'
    
    def getDocumentableFields(self):
        '''Get the attributes that are put into XML.
        '''
        return (DocumentableField('id', u'profId', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('version', u'profVersion', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('type', u'profType', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('statusID', u'profStatusId', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('securityType', u'profSecurityType', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('parentID', u'profParentId', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('childIDs', u'profChildId', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('regAuthority', u'profRegAuthority', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('revNotes', u'profRevisionNote', DocumentableField.MULTI_VALUED_KIND))

    def __repr__(self):
        return ('ProfileAttributes(id=%s,version=%s,type=%s,statusID=%s,securityType=%s,parentID=%s,childIDs=%s,' \
            'regAuthority=%s,revNotes=%s)') % (self.id, self.version, self.type, self.statusID, self.securityType,
            self.parentID, self.childIDs, self.regAuthority, self.revNotes)
            
    def __cmp__(self, other):
        return cmp(self.id, other.id)
    
    def __hash__(self):
        return hash(self.id)
    

class ResourceAttributes(xmlutils.Documentable):
    '''Attributes of the resource.  Objects of this class collect data about the resource's
    inception and are based on Dublin Core.
    '''
    def __init__(self, identifier='UNKNOWN', title='UNKNOWN', formats=None, description='UNKNOWN', creators=None, subjects=None,
        publishers=None, contributors=None, dates=None, types=None, sources=None, languages=None, relations=None,
        coverages=None, rights=None, contexts=None, aggregation='UNKNOWN', resClass='UNKNOWN', locations=None, node=None):
        '''Initialize ResourceAttributes.
        
        The following arguments are required:
        
        - `identifier` is a URI uniquely identifying the resource
        - `title` names the resource
        - `description` gives a summary or abstract of it
        - `aggregation` tells the gross structure of the resource and
           should be one of the following values:
            - data.granule
            - data.dataSet
            - data.dataSetCollection
        - `resClass` gives the kind of the resource (what to expect
           when connecting to one of its locations)
        
        All of the others should be initialized with sequences as the
        resource attributes follows the Dublin Core recommendation on
        multiplicity.
        
        If `node` is given, it's treated as an XML DOM node and is used to
        initialize the resource attributes.
        '''
        self.identifier = identifier
        self.title = title
        self.formats = formats
        self.description = description
        self.creators = creators
        self.subjects = subjects
        self.publishers = publishers
        self.contributors = contributors
        self.dates = dates
        self.types = types
        self.sources = sources
        self.languages = languages
        self.relations = relations
        self.coverages = coverages
        self.rights = rights
        self.contexts = contexts
        self.aggregation = aggregation
        self.resClass = resClass
        self.locations = locations
        for attr in ('formats', 'creators', 'subjects', 'publishers', 'contributors', 'dates', 'sources', 'languages',
            'relations', 'coverages', 'rights', 'contexts', 'locations', 'types'):
            if getattr(self, attr) is None:
                setattr(self, attr, [])
        if node is not None:
            self.parse(node)
    
    def getDocumentElementName(self):
        '''Give the XML tag name: `resAttributes`.
        '''
        return 'resAttributes'
        
    def __hash__(self):
        return hash(self.identifier)
    
    def __cmp__(self, other):
        return cmp(self.identifier, other.identifier)
    
    def getDocumentableFields(self):
        '''Get the attributes that go into XML.
        '''
        return (DocumentableField('identifier', u'Identifier', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('title', u'Title', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('formats', u'Format', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('description', u'Description', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('creators', u'Creator', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('subjects', u'Subject', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('publishers', u'Publisher', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('contributors', u'Contributor', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('dates', u'Date', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('types', u'Type', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('sources', u'Source', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('languages', u'Language', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('relations', u'Relation', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('coverages', u'Coverage', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('rights', u'Rights', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('contexts', u'resContext', DocumentableField.MULTI_VALUED_KIND),
            DocumentableField('aggregation', u'resAggregation', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('resClass', u'resClass', DocumentableField.SINGLE_VALUE_KIND),
            DocumentableField('locations', u'resLocation', DocumentableField.MULTI_VALUED_KIND))
    

class Profile(object):
    '''A profile "profiles" a resource by describing it with metadata.
    '''
    def __init__(self, profAttr=None, resAttr=None, profElements=None, node=None):
        '''Initialize a profile.  The profElements should be a dicitonary that maps
        from element name to instance of a `ProfileElement`.
        '''
        self.profAttr, self.resAttr, self.profElements = profAttr, resAttr, profElements
        if self.profAttr is None:
            self.profAttr = ProfileAttributes()
        if self.resAttr is None:
            self.resAttr = ResourceAttributes()
        if self.profElements is None:
            self.profElements = {}
        if node is not None:
            self.parse(node)
    
    def parse(self, node):
        '''Initialize this object from the given XML DOM `node`.
        '''
        if node.nodeName != 'profile':
            raise ValueError('Expected profile element but got "%s"' % node.nodeName)
        for child in filter(lambda node: node.nodeType == xml.dom.Node.ELEMENT_NODE, node.childNodes):
            name = child.nodeName
            if name == u'profAttributes':
                self.profAttr = ProfileAttributes()
                self.profAttr.parse(child)
            elif name == u'resAttributes':
                self.resAttr = ResourceAttributes()
                self.resAttr.parse(child)
            elif name == u'profElement':
                elem = _parseProfileElement(child)
                self.profElements[elem.name] = elem
                
    def toXML(self, owner):
        '''Convert this object into XML owned by the given `owner` document.
        '''
        root = owner.createElement(u'profile')
        root.appendChild(self.profAttr.toXML(owner))
        root.appendChild(self.resAttr.toXML(owner))
        for elem in self.profElements.itervalues():
            root.appendChild(elem.toXML(owner))
        return root
    
    def __cmp__(self, other):
        profAttr = cmp(self.profAttr, other.profAttr)
        if profAttr < 0:
            return -1
        elif profAttr == 0:
            resAttr = cmp(self.resAttr, other.resAttr)
            if resAttr < 0:
                return -1
            elif resAttr == 0:
                return cmp(self.profElements, other.profElements)
        return 1
    
    def __hash__(self):
        return hash(self.profAttr) ^ hash(self.resAttr)
    
    def __repr__(self):
        return 'Profile(profAttr=%s,resAttr=%s,profElements=%s)' % (self.profAttr, self.resAttr, self.profElements)
    

class ProfileElement(object):
    '''Abstract profile element.
    '''
    def __init__(self, name, description, type, units, synonyms, comment):
        '''Initialize profile element.
        '''
        self.name = name
        self.description = description
        self.type = type
        self.units = units
        self.synonyms = synonyms
        self.comment = comment
    
    def __repr__(self):
        return 'ProfileElement(name=%r,description=%r,type=%r,units=%r,synonyms=%r,comment=%r)' % (self.name,
            self.description, self.type, self.units, self.synonyms, self.comment)
    
    def __cmp__(self, other):
        rc = cmp(self.name, other.name)
        if rc < 0:
            return -1
        elif rc == 0:
            rc = cmp(self.description, other.description)
            if rc < 0:
                return -1
            elif rc == 0:
                rc = cmp(self.type, other.type)
                if rc < 0:
                    return -1
                elif rc == 0:
                    rc = cmp(self.units, other.units)
                    if rc < 0:
                        return -1
                    elif rc == 0:
                        rc = cmp(self.synonyms, other.synonyms)
                        if rc < 0:
                            return -1
                        elif rc == 0:
                            return cmp(self.comment, other.comment)
        return 1
    
    def __hash__(self):
        return reduce(lambda x, y: hash(x) ^ hash(y), [getattr(self, attr) for attr in ('name', 'description', 'type',
            'units', 'synonyms', 'comment')])
    
    def isEnumerated(self):
        '''Is this an enumerated element?  Enumerated elements include those with
        discrete values as well unspecified elements.
        '''
        return False;
    
    def getValues(self):
        '''Get the discrete values of this element, which may be empty.
        '''
        return []
    
    def getMinValue(self):
        '''Get the minimum value of this element, which may be zero.
        '''
        return 0.0
    
    def getMaxValue(self):
        '''Get the maximum value of this element, which may be zero.
        '''
        return 0.0
    
    def toXML(self, owner):
        '''Convert this object into XML owned by the given `owner` document.
        '''
        root = owner.createElement('profElement')
        xmlutils.add(root, u'elemId', self.name)
        xmlutils.add(root, u'elemName', self.name)
        xmlutils.add(root, u'elemDesc', self.description)
        xmlutils.add(root, u'elemType', self.type)
        xmlutils.add(root, u'elemUnit', self.units)
        if self.isEnumerated():
            flag = 'T'
        else:
            flag = 'F'
        xmlutils.add(root, u'elemEnumFlag', flag)
        for value in self.getValues():
            elem = owner.createElement('elemValue')
            root.appendChild(elem)
            elem.appendChild(owner.createCDATASection(value))
        if not self.isEnumerated():
            xmlutils.add(root, u'elemMinValue', str(self.getMinValue()))
            xmlutils.add(root, u'elemMaxValue', str(self.getMaxValue()))
        xmlutils.add(root, u'elemSynonym', self.synonyms)
        xmlutils.add(root, u'elemComment', self.comment)
        return root
    

class UnspecifiedProfileElement(ProfileElement):
    '''An unspecified profile element merely documents the existence of a element within a
    dataset but says nothing about its actual values.
    '''
    def __init__(self, name, description, type, units, synonyms, comment):
        '''Initialize an "unspecified" profile element.
        '''
        super(UnspecifiedProfileElement, self).__init__(name, description, type, units, synonyms, comment)
    
    def isEnumerated(self):
        '''An unspecified profile element is indeed enumerated.  It just has no enumerated values.
        '''
        return True
    

class EnumeratedProfileElement(ProfileElement):
    '''An enumerated profile element describes set of discrete values.
    '''
    def __init__(self, name, description, type, units, synonyms, comment, values):
        '''Initialize an enumerated profile element.
        '''
        super(EnumeratedProfileElement, self).__init__(name, description, type, units, synonyms, comment)
        self.values = values
        
    def isEnumerated(self):
        '''An enumerated profile element is indeed enumerated.
        '''
        return True
    
    def getValues(self):
        '''Return the sequence of values.
        '''
        return self.values
        
    def __cmp__(self, other):
        rc = super(EnumeratedProfileElement, self).__cmp__(other)
        if rc < 0:
            return -1
        elif rc == 0:
            return cmp(self.values, other.values)
        return 1
    

class RangedProfileElement(ProfileElement):
    '''A ranged profile element describes a value between two numeric ranges.
    '''
    def __init__(self, name, description, type, units, synonyms, comment, minValue, maxValue):
        '''Initialize a ranged profile element.
        '''
        super(RangedProfileElement, self).__init__(name, description, type, units, synonyms, comment)
        self.minValue = minValue
        self.maxValue = maxValue
    
    def getMinValue(self):
        '''Get the minimum value.
        '''
        return self.minValue
    
    def getMaxValue(self):
        '''Get the maximum value.
        '''
        return self.maxValue
    
    def __repr__(self):
        return 'RangedProfileElement(%r,minValue=%r,maxValue=%r)' % (super(RangedProfileElement, self).__repr__(),
            self.minValue, self.maxValue)
    
    def __cmp__(self, other):
        rc = super(RangedProfileElement, self).__cmp__(other)
        if rc < 0:
            return -1
        elif rc == 0:
            rc = self.minValue - other.minValue
            if rc < 0:
                return -1
            elif rc == 0:
                return self.maxValue - other.maxValue
        return 1
    

def _parseProfileElement(node):
    '''Construct an appropriate profile element from the given DOM node.
    '''
    if node.nodeName != u'profElement':
        raise ValueError('Expected profElement element but got "%s"' % node.nodeName)
    settings = dict(elemId=u'UNKNOWN', elemDesc=u'UNKNOWN', elemType=u'UNKNOWN', elemUnit=u'UNKNOWN', elemEnumFlag=u'F',
        elemComment=u'UNKNOWN')
    values, syns = [], []
    for child in filter(lambda node: node.nodeType == xml.dom.Node.ELEMENT_NODE, node.childNodes):
        text = xmlutils.text(child)
        if child.nodeName == u'elemValue':
            values.append(text)
        elif child.nodeName == u'elemSynonym':
            syns.append(text)
        else:
            settings[str(child.nodeName)] = text
    if 'elemName' not in settings:
        raise ValueError('profElement requires elemName but none specified')
    if 'elemEnumFlag' not in settings:
        raise ValueError('profElement requires elemEnumFlag but none specified')
        
    # Normally I'd treat only those XML elements where elemEnumFlag as T as possibly producing
    # unspecified or enumerated, and F producing *only* ranged elements.  But PDS profile
    # servers are producing profile elements with F for elemEnumFlag and yet NO elemMinValue
    # and elemMaxValue.  If they're using the Java profile code, I'd call that a bug in that
    # code.  If they're not, then I'd say they're producing profiles incorrectly.
    if settings['elemEnumFlag'] == 'T':
        if len(values) == 0:
            return UnspecifiedProfileElement(settings['elemName'], settings['elemDesc'], settings['elemType'],
                settings['elemUnit'], syns, settings['elemComment'])
        else:
            return EnumeratedProfileElement(settings['elemName'], settings['elemDesc'], settings['elemType'],
                settings['elemUnit'], syns, settings['elemComment'], values)
    else:
        if 'elemMinValue' not in settings or 'elemMaxValue' not in settings:
            return UnspecifiedProfileElement(settings['elemName'], settings['elemDesc'], settings['elemType'],
                settings['elemUnit'], syns, settings['elemComment'])
        else:
            return RangedProfileElement(settings['elemName'], settings['elemDesc'], settings['elemType'],
            settings['elemUnit'], syns, settings['elemComment'], float(settings['elemMinValue']),
            float(settings['elemMaxValue']))
    

# Some sample code:
# if __name__ == '__main__':
#   import urllib2, xml.dom.minidom
#   x = urllib2.urlopen('http://starbrite.jpl.nasa.gov/q?object=urn%3Aeda%3Armi%3AJPL.PDS.MasterProd&type=profile&keywordQuery=TARGET_NAME+%3D+MARS')
#   d = xml.dom.minidom.parse(x)
#   x.close()
#   profiles = []
#   for i in d.documentElement.getElementsByTagName(u'profile'):
#       profiles.append(Profile(node=i))
#       
#   doc = xml.dom.minidom.getDOMImplementation().createDocument(None, None, None)
#   print '<?xml version="1.0" encoding="UTF-8"?>'
#   print '<!DOCTYPE profiles PUBLIC "-//JPL//DTD Profile 1.1//EN"'
#   print '  "http://oodt.jpl.nasa.gov/grid-profile/dtd/prof.dtd">'
#   print '<profiles>'
#   for profile in profiles:
#       print profile.toXML(doc).toxml()
#   print '</profiles>'
#   
