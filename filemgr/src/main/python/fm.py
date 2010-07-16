#!/usr/bin/env python
# encoding: utf-8
#/*
# * Licensed to the Apache Software Foundation (ASF) under one or more
# * contributor license agreements.  See the NOTICE file distributed with
# * this work for additional information regarding copyright ownership.
# * The ASF licenses this file to You under the Apache License, Version 2.0
# * (the "License"); you may not use this file except in compliance with
# * the License.  You may obtain a copy of the License at
# *
# *     http://www.apache.org/licenses/LICENSE-2.0
# *
# * Unless required by applicable law or agreed to in writing, software
# * distributed under the License is distributed on an "AS IS" BASIS,
# * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# * See the License for the specific language governing permissions and
# * limitations under the License.
# */
'''
CAS Filemgr Python Server

This is the Python server API for an authenticated Catalog and Archive System.  It uses the
OODT Catalog and Archive System as its core, adding user/password-based authentication
and role-based authorization.
'''

import sys, os, os.path
import getopt, sha, pickle

from ConfigParser import ConfigParser
from org.apache.oodt.cas.filemgr.system.auth import SecureWebServer, Dispatcher, Result
from org.apache.oodt.cas.filemgr.datatransfer import TransferStatusTracker
from org.apache.oodt.cas.filemgr.structs import Product
from org.apache.oodt.cas.filemgr.util import GenericFileManagerObjectFactory
from org.apache.oodt.cas.filemgr.util import XmlRpcStructFactory as Structs
from org.apache.oodt.cas.metadata import Metadata
from java.lang import Boolean, Double, Integer
from java.util import Hashtable, Vector

# We choose these default factory classes because it minimizes our dependencies
# on heavyweight external packages, like smelly old SQL databases.
_defaultFactories = {
	'catalog': 'org.apache.oodt.cas.filemgr.catalog.LuceneCatalogFactory',
	'repository': 'org.apache.oodt.cas.filemgr.repository.XMLRepositoryManagerFactory',
	'datatransfer': 'org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory',
	'validation': 'org.apache.oodt.cas.filemgr.validation.XMLValidationLayerFactory'
}

# All available permissions.  By default, the "root" user will be in the "wheel"
# group and will have these permissions.
_allPerms = [
	'filemgr.addMetadata',
	'filemgr.addProductReferences',
	'filemgr.addProductType',
	'filemgr.catalogProduct',
	'filemgr.getCurrentFileTransfer',
	'filemgr.getCurrentFileTransfers',
	'filemgr.getElementById',
	'filemgr.getElementByName',
	'filemgr.getElementsByProductType',
	'filemgr.getFirstPage',
	'filemgr.getLastPage',
	'filemgr.getMetadata',
	'filemgr.getNextPage',
	'filemgr.getNumProducts',
	'filemgr.getPrevPage',
	'filemgr.getProductById',
	'filemgr.getProductByName',
	'filemgr.getProductPctTransferred',
	'filemgr.getProductReferences',
	'filemgr.getProductsByProductType',
	'filemgr.getProductTypeById',
	'filemgr.getProductTypeByName',
	'filemgr.getProductTypes',
	'filemgr.getRefPctTransferred',
	'filemgr.getTopNProducts',
	'filemgr.handleRequest',
	'filemgr.hasProduct',
	'filemgr.ingestProduct',
	'filemgr.isTransferComplete',
	'filemgr.pagedQuery',
	'filemgr.query',
	'filemgr.removeFile',
	'filemgr.removeProductTransferStatus',
	'filemgr.setProductTransferStatus',
	'filemgr.transferFile',
	'filemgr.transferringProduct',
	'usermgr.addGroup',
	'usermgr.addPermissionToGroup',
	'usermgr.addUser',
	'usermgr.addUserToGroup',
	'usermgr.removeGroup',
	'usermgr.removePermissionFromGroup',
	'usermgr.removeUser',
	'usermgr.removeUserFromGroup'
]

def _toJavaBoolean(truthiness):
	'''Convert a Python boolean into the string format that Java uses: true or false.
	'''
	if truthiness:
		return 'true'
	else:
		return 'false'
	

def _encodePassword(pw):
	'''Encode a password using an SHA-1 digest.
	'''
	return sha.new(pw).digest()
	

class User:
	'''A user of the CAS.  Users don't have permissions directly; instead they receive
	them implicitly by being members of groups, which do have permissions.
	'''
	def __init__(self, userID, name, email, password, groups=[]):
		self.userID, self.name, self.email, self.password, self.groups = userID, name, email, password, groups
		
	def __cmp__(self, other):
		return cmp(self.userID, other.userID)
		
	def __hash__(self):
		return hash(self.userID)
	
	def __repr__(self):
		return 'User(userID=%s,name=%s,email=%s,password=%s,groups=%r)' % (
			self.userID, self.name, self.email, self.password, self.groups
		)
	

class Group:
	'''A CAS group.  The group contains a sequence of permissions, which are strings
	that name the XML-RPC methods that the group is allowed to call.
	'''
	def __init__(self, groupID, name, email, perms=[]):
		self.groupID, self.name, self.email, self.perms = groupID, name, email, perms
		
	def __cmp__(self, other):
		return cmp(self.groupID, other.groupID)
		
	def __hash__(self):
		return hash(self.groupID)
		
	def __repr__(self):
		return 'Group(groupdID=%s,name=%s,email=%s,perms=%r)' % (self.groupdID, self.name, self.email, self.perms)
	

class UserDB:
	'''The user database records all the users and groups.
	'''
	def __init__(self, users, groups, filename):
		self.users, self.groups, self.filename = users, groups, filename
		
	def authenticate(self, name, password):
		'''Authenticate a user by checking the user name and password.
		Return true if the user's password matches the given one.  The
		password given should be in SHA-1 digest format.
		'''
		user = self.users[name]
		return user.userID == name and user.password == password
	
	def authorize(self, name, perm):
		'''Authorize if the user has the given permission.  Return true if
		the user can do it, false otherwise.
		'''
		user = self.users[name]
		for group in user.groups:
			if perm in group.perms:
				return True
		return False
	
	def save(self):
		'''Save the user database to disk.
		'''
		f = file(self.filename, 'wb')
		pickle.dump(self, f)
		f.close()
	

class FileMgrDispatcher(Dispatcher):
	'''The file manager dispatcher handles all XML-RPC calls.
	'''
	def __init__(self, catalog, repo, xfer, userDB):
		self.catalog, self.repo, self.xfer, self.userDB = catalog, repo, xfer, userDB
		self.tracker = TransferStatusTracker(self.catalog)
	
	def handleRequest(self, methodSpecifier, params, user, password):
		'''Handle an XML-RPC request.  First, authenticate the user.  If the user's
		authentic (by dint of providing a correct user ID and password pair), then
		authorize if the method the user is trying to call is available.
		'''
		password = _encodePassword(password)
		if self.userDB.authenticate(user, password):
			if self.userDB.authorize(user, methodSpecifier):
				obj, method = methodSpecifier.split('.')
				if obj not in ('filemgr', 'usermgr'):
					raise ValueError('Unknown object')
				func = getattr(self, method)
				return func(params)
			raise ValueError('Not authorized for "%s"' % methodSpecifier)
		raise ValueError('Illegal user name "%s" and/or password' % user)
	
	def getProductTypeByName(self, params):
		return Result(None, Structs.getXmlRpcProductType(self.repo.getProductTypeByName(params[0])))
	
	def ingestProduct(self, params):
		productHash, metadata, clientXfer = params
		p = Structs.getProductFromXmlRpc(productHash)
		p.setTransferStatus(Product.STATUS_TRANSFER)
		self.catalog.addProduct(p)
		
		m = Metadata()
		m.addMetadata(metadata)
		self.catalog.addMetadata(m, p)
		
		if not clientXfer:
	                versioner = GenericFileManagerObjectFactory.getVersionerFromClassName(p.getProductType().getVersioner())
			versioner.createDataStoreReferences(p, m)
			self.catalog.addProductReferences(p)
			self.xfer.transferProduct(p)
			p.setTransferStatus(Product.STATUS_RECEIVED)
			self.catalog.setProductTranfserStatus(p)
		return Result(None, p.getProductId())
	
	def addProductReferences(self, params):
		self.catalog.addProductReferences(Structs.getProductFromXmlRpc(params[0]))
		return Result(Boolean, 'true')
	
	def transferringProduct(self, params):
		self.tracker.transferringProduct(Structs.getProductFromXmlRpc(params[0]))
		return Result(Boolean, 'true')
	
	def removeProductTransferStatus(self, params):
		self.tracker.removeProductTransferStatus(Structs.getProductFromXmlRpc(params[0]))
		return Result(Boolean, 'true')
	
	def setProductTransferStatus(self, params):
		self.catalog.setProductTransferStatus(Structs.getProductFromXmlRpc(params[0]))
		return Result(Boolean, 'true')
		
	def getCurrentFileTransfer(self, params):
		status = self.tracker.getCurrentFileTransfer()
		if status is None:
			return Result(None, Hashtable())
		else:
			return Result(None, Structs.getXmlRpcFileTransferStatus(status))
	
	def getCurrentFileTransfers(self, params):
		xfers = self.tracker.getCurrentFileTransfers()
		if xfers is not None and len(xfers) > 0:
			return Result(None, Structs.getXmlRpcFileTransferStatuses(xfers))
		else:
			return Result(None, Vector())
	
	def getProductPctTransferred(self, params):
		return Result(Double, str(self.tracker.getPctTransferred((Structs.getProductFromXmlRpc(params[0])))))
	
	def getRefPctTransferred(self, params):
		pct = self.tracker.getPctTransferred(Structs.getReferenceFromXmlRpc(params[0]))
		return Result(Double, str(pct))
	
	def isTransferComplete(self, params):
		return Result(Boolean, _toJavaBoolean(self.tracker.isTransferComplete(Structs.getProductFromXmlRpc(params[0]))))
	
	def pagedQuery(self, params):
		ptype = Structs.getProductTypeFromXmlRpc(params[1])
		query = Structs.getQueryFromXmlRpc(params[0])
		return Result(None, Structs.getXmlRpcProductPage(self.catalog.pagedQuery(query, ptype, params[2])))
	
	def getFirstPage(self, params):
		ptype = Structs.getProductTypeFromXmlRpc(params[0])
		return Result(None, Structs.getXmlRpcProductPage(self.catalog.getFirstPage(ptype)))
	
	def getLastPage(self, params):
		ptype = Structs.getProductTypeFromXmlRpc(params[0])
		return Result(None, Structs.getXmlRpcProductPage(self.catalog.getLastProductPage(ptype)))
	
	def getNextPage(self, params):
		ptype = Structs.getProductTypeFromXmlRpc(params[0])
		page = Structs.getProductPageFromXmlRpc(params[1])
		return Result(None, Structs.getXmlRpcProductPage(self.catalog.getNextPage(ptype, page)))
	
	def getPrevPage(self, params):
		ptype = Structs.getProductTypeFromXmlRpc(params[0])
		page = Structs.getProductPageFromXmlRpc(params[1])
		return Result(None, Structs.getXmlRpcProductPage(self.catalog.getPrevPage(ptype, page)))
		
	def addProductType(self, params):
		ptype = Structs.getProductTypeFromXmlRpc(params[0])
		self.repo.addProductType(ptype)
		return Result(None, ptype.getProductTypeId())
	
	def getNumProducts(self, params):
		ptype = Structs.getProductTypeFromXmlRpc(params[0])
		return Result(Integer, str(self.catalog.getNumProducts(ptype)))
	
	def getTopNProducts(self, params):
		if len(params) == 1:
			return Result(None, Structs.getXmlRpcProductList(self.catalog.getTopNProducts(params[0])))
		ptype = Structs.getProductTypeFromXmlRpc(params[1])
		return Result(None, Structs.getXmlRpcProductList(self.catalog.getTopNProducts(params[0], ptype)))
	
	def hasProduct(self, params):
		p = self.catalog.getProductByName(params[0])
		return Result(Boolean, _toJavaBoolean(p is not None and p.transferStatus == Product.STATUS_RECEIVED))
	
	def getMetadata(self, params):
		return Result(None, self.catalog.getMetadata(Structs.getProductFromXmlRpc(params[0])).getHashtable())
	
	def getProductTypes(self, params):
		return Result(None, Structs.getXmlRpcProductList(self.repo.getProductTypes()))
	
	def getProductReferences(self, params):
		p = Structs.getProductFromXmlRpc(params[0])
		return Result(None, Structs.getXmlRpcReferences(self.catalog.getProductReferences(p)))
	
	def getProductById(self, params):
		return Result(None, Structs.getXmlRpcProduct(self.catalog.getProductById(params[0])))
	
	def getProductByName(self, params):
		return Result(None, Structs.getXmlRpcProduct(self.catalog.getProductByName(params[0])))
	
	def getProductsByProductType(self, params):
		ptype = Structs.getProductTypeFromXmlRpc(params[0])
		return Result(None, Structs.getXmlRpcProductList(self.catalog.getProductsByProductType(ptype)))
	
	def getElementsByProductType(self, params):
		ptype = Structs.getProductTypeFromXmlRpc(params[0])
		return Structs.getXmlRpcElementList(self.catalog.getValidationLayer().getElements(ptype))
	
	def getElementById(self, params):
		return Structs.getXmlRpcElement(self.catalog.getValidationLayer().getElementById(params[0]))
		
	def getElementByName(self, params):
		return Structs.getXmlRpcElement(self.catalog.getValidationLayer().getElementByName(params[0]))
	
	def query(self, params):
		q = Structs.getQueryFromXmlRpc(params[0])
		ptype = Structs.getProductTypeFromXmlRpc(params[1])
		ids = self.catalog.query(q, ptype)
		if ids is not None and len(ids) > 0:
			return Result(None, [self.catalog.getProductById(i) for i in ids])
		return Result(None, Vector())
	
	def getProductTypeById(self, params):
		ptype = self.repo.getProductTypeById(params[0])
		return Result(None, Structs.getXmlRpcProductType(ptype))
	
	def catalogProduct(self, params):
		p = Structs.getProductFromXmlRpc(params[0])
		return Result(None, self.catalog.addProduct(p))
	
	def addMetadata(self, params):
		p = Structs.getProductFromXmlRpc(params[0])
		m = Metadata()
		m.addMetadata(params[1])
		self.catalog.addMetadata(m, p)
		return Result(Boolean, 'true')
		
	def transferFile(self, params):
		outFile, data, offset, numBytes = params
		if os.path.exists(outFile):
			out = file(outFile, 'ab')
		else:
			dirPath = os.dirname(outFile)
			os.makedirs(dirPath)
			out = file(outFile, 'wb')
		out.seek(offset)
		out.write(data)
		out.close()
		return Result(Boolean, 'true')
	
	def removeFile(self, params):
		os.remove(params[0])
		return Result(Boolean, 'true')
		
	def addUser(self, params):
		userID = params[0]
		user = User(userID, params[1], params[2], _encodePassword(params[3]), [])
		self.userDB.users[userID] = user
		self.userDB.save()
		return Result(Boolean, 'true')
		
	def removeUser(self, params):
		del self.userDB.users[params[0]]
		return Result(Boolean, 'true')
	
	def addGroup(self, params):
		groupID = params[0]
		group = Group(groupID, params[1], params[2])
		self.userDB.groups[groupID] = group
		self.userDB.save()
		return Result(Boolean, 'true')
	
	def removeGroup(self, params):
		groupID = params[0]
		del self.userDB.groups[groupID]
		for user in self.userDB.users.itervalues():
			indexes = []
			index = 0
			for group in user.groups:
				if group.groupID == groupID:
					indexes.append(index)
				index += 1
			indexes.reverse()
			for index in indexes:
				del user.groups[index]
		self.userDB.save()
		return Result(Boolean, 'true')
		
	def addUserToGroup(self, params):
		self.userDB.users[params[0]].groups.append(self.userDB.groups[params[1]])
		return Result(Boolean, 'true')

	def removeUserFromGroup(self, params):
		groupID = params[1]
		user = self.userDB.users[params[0]]
		indexes = []
		index = 0
		for group in user.group:
			if group.groupID == groupID:
				indexes.append(index)
			index += 1
		indexes.reverse()
		for index in indexes:
			del user.groups[index]
		self.userDB.save()
		return Result(Boolean, 'true')
		
	def addPermissionToGroup(self, params):
		self.userDB.groups[params[0]].perms.append(params[1])
		self.userDB.save()
		return Result(Boolean, 'true')
		
	def removePermissionFromGroup(self, params):
		permName = params[1]
		group = self.userDB.groups[params[0]]
		indexes = []
		index = 0
		for perm in group.perms:
			if perm == permName:
				indexes.append(index)
		indexes.reverse()
		for index in indexes:
			del group.perms[index]
		self.userDB.save()
		return Result(Boolean, 'true')
	

def _usage():
	'''Show a usage message to the stderr and quit.
	'''
	print >>sys.stderr, 'Usage: %s [-c <configFile>]' % sys.argv[0]
	print >>sys.stderr, '   or: %s [--config=<configFile>]' % sys.argv[0]
	sys.exit(2)
	

def _parseCommandLine():
	'''Parse the command line options.  If any.  The only option is -c (or --config)
	that names a configuration file to use.  If none given, reasonable defaults are
	used.  Well, mostly reasonable.
	'''
	try:
		opts, args = getopt.getopt(sys.argv[1:], 'c:', 'config=')
	except getopt.GetoptError:
		_usage()
	configFile = None
	for option, arg in opts:
		if option in ('-c', '--config'):
			configFile = arg
			if configFile is None or len(configFile) == 0:
				_usage()
	return configFile
	

def _getConfig(configFile):
	'''Get the configuration.  This populates a configuration with default values
	and then overwrites them with the configFile, which may be None (in which case,
	no overwriting happens).
	'''
	configParser = ConfigParser()
	configParser.add_section('factories')
	for key, val in _defaultFactories.iteritems():
		configParser.set('factories', key, val)

	configParser.add_section('index')
	configParser.set('index', 'path', 'index')
	configParser.set('index', 'pageSize', '20')

	current = '/'.join(os.path.split(os.getcwd()))
	configParser.add_section('policies')
	configParser.set('policies', 'repo', 'file:%s/policy' % current)
	configParser.set('policies', 'validation', 'file:%s/policy' % current)
	configParser.set('policies', 'user', '%s/user.db' % current)

	if configFile is not None:
		configParser.readfp(file(configFile))
	return configParser
	

def _setJavaProperties(config):
	'''Set Java-based properties.  The Java-based cas expects a whole bunch of
	system properties to be set, sort of like global variables.  Woot!  Global
	variables!
	'''
	from java.lang import System
	System.setProperty('org.apache.oodt.cas.filemgr.catalog.lucene.idxPath', config.get('index', 'path'))
	System.setProperty('org.apache.oodt.cas.filemgr.catalog.lucene.pageSize', config.get('index', 'pageSize'))
	System.setProperty('org.apache.oodt.cas.filemgr.repositorymgr.dirs', config.get('policies', 'repo'))
	System.setProperty('org.apache.oodt.cas.filemgr.validation.dirs', config.get('policies', 'validation'))
	System.setProperty('org.apache.oodt.cas.filemgr.datatransfer.remote.chunkSize', '1024')
	System.setProperty('filemgr.repository.factory', config.get('factories', 'repository'))
	System.setProperty('filemgr.catalog.factory', config.get('factories', 'catalog'))
	System.setProperty('filemgr.datatransfer.factory', config.get('factories', 'datatransfer'))
	System.setProperty('filemgr.validationLayer.factory', config.get('factories', 'validation'))
	
def _getUserDB(path):
	'''Get the user database, creating it if necessary.
	'''
	try:
		f = file(path, 'rb')
		db = pickle.load(f)
		f.close()
		db.filename = path
		db.save()
	except:
		wheel = Group('wheel', 'Administrators', 'teh.power@power.users', _allPerms)
		root = User('root', 'Super User', 'teh.root@power.users', _encodePassword('poipu'), [wheel])
		db = UserDB({'root': root}, {'wheel': wheel}, path)
		db.save()
	return db
	

def main():
	'''Start the CAS Filemgr Backend.
	'''
	configFile = _parseCommandLine()
	configParser = _getConfig(configFile)
	_setJavaProperties(configParser)

	catalog = GenericFileManagerObjectFactory.getCatalogServiceFromFactory(configParser.get('factories', 'catalog'))
	repo = GenericFileManagerObjectFactory.getRepositoryManagerServiceFromFactory(configParser.get('factories', 'repository'))
	xfer = GenericFileManagerObjectFactory.getDataTransferServiceFromFactory(configParser.get('factories', 'datatransfer'))
	userDB = _getUserDB(configParser.get('policies', 'user'))

	ws = SecureWebServer(1999)
	ws.addDispatcher(FileMgrDispatcher(catalog, repo, xfer, userDB))
	ws.start()
	

if __name__ == '__main__':
	main()
	
