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

'''Agile Web Grid.  HTTP-based profile and product servers.
'''

__docformat__ = 'restructuredtext'

from oodt.query import Query
from xml.dom.minidom import parseString, getDOMImplementation
from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler
import cgi, os, shutil, stat

_validKinds = ('profile', 'product')
_doc = getDOMImplementation().createDocument(None, None, None) # qname, nsuri, doctype
    
class WebGridRequestHandler(BaseHTTPRequestHandler):
    '''HTTP request handler for Web-Grid requests.  This request handler accepts GET
    or POST requests and directs them to profile or product handlers.  Additionally,
    requests to install new handlers, to list currently installed handlers, and to
    remove handlers by ID are supported.
    '''
    def do_POST(self):
        '''Handle a POST.
        '''
        try:
            length = int(self.headers['content-length'])
            kind = self.headers['content-type']
            if kind.endswith('www-form-urlencoded'):
                if length > 0:
                    params = cgi.parse_qs(self.rfile.read(length), True, True) # Keep blanks, strict parse
                else:
                    params = {}
                self.__execute(self.path, params)
            else:
                raise ValueError('Unknown encoding "%s"' % kind)
        except Exception, e:
            self.send_error(500, str(e))
    
    def do_GET(self):
        '''Handle a GET.
        '''
        try:
            index = self.path.find('?')
            if index >= 0:
                params = cgi.parse_qs(self.path[index+1:], True, True) # Keep blanks, strict parse
                path = self.path[0:index]
            else:
                params, path = {}, self.path
            self.__execute(path, params)
        except Exception, e:
            self.send_error(500, str(e))
    
    def __execute(self, path, params):
        '''Execute an HTTP request.
        '''
        components = path.split('/')
        if len(components) == 3:
            context, command = components[1], components[2]
            if context != self.server.serverID:
                raise ValueError('Unknown server ID "%s"' % context)
            func = getattr(self, command)
            if callable(func):
                func(params)
                return
        raise KeyError('Unknown command')
    
    def echo(self, params):
        '''Debugging method that echoes back the request parameters.
        '''
        u = unicode(params)
        self.send_response(200)
        self.send_header('Content-type', 'text/plain;charset=utf-8')
        self.send_header('Content-length', str(len(u)))
        self.end_headers()
        self.wfile.write(u)
    
    def sendEmptyResponse(self):
        '''Send an empty response to the HTTP client.
        '''
        self.send_response(200)
        self.send_header('Content-type', 'text/plain;charset=utf-8')
        self.send_header('Content-length', '0')
        self.end_headers()
    
    def install(self, params):
        '''Install a new handler.  This will overwrite existing handlers with the
        same ID.
        '''
        handlers = self.server.getHandlers(params['kind'][0])
        globs = dict(globals())
        del globs['__name__']
        # TODO: use rexec or otherwise limit the code than can be uploaded.
        exec params['code'][0] in globs, globs
        handlers[params['id'][0]] = globs['handler']
        self.sendEmptyResponse()
    
    def remove(self, params):
        '''Remove an existing handler.
        '''
        handlers = self.server.getHandlers(params['kind'][0])
        del handlers[params['id'][0]]
        self.sendEmptyResponse()
    
    def list(self, params):
        '''List installed handlers.
        '''
        handlers = {}
        for kind in _validKinds:
            handlers[kind] = self.server.getHandlers(kind).keys()
        handlers = unicode(handlers)
        self.send_response(200)
        self.send_header('Content-type', 'text/plain;charset=utf-8')
        self.send_header('Content-length', str(len(handlers)))
        self.end_headers()
        self.wfile.write(handlers)
    
    def __createQuery(self, params):
        '''Create a Query from the request parameters.  This method prefers the
        xmlq parameter and parses it as an XML document and into a Query object.
        However, if it's not provided, or fails to parse, it'll use the q parameter,
        which is expected to be just a query expression.
        '''
        try:
            doc = parseString(params['xmlq'][0])
            return Query(node=doc.documentElement)
        except KeyError:
            return Query(params['q'][0])
    
    def sendProduct(self, match):
        '''Send a matching product.
        '''
        self.send_response(200)
        self.send_header('Content-type', match.contentType)
        self.send_header('Content-length', str(match.length))
        self.end_headers()
        shutil.copyfileobj(match.data, self.wfile)
        self.log_request(200, match.length)
    
    def prod(self, params):
        '''Handle a product query.
        '''
        query = self.__createQuery(params)
        for handler in self.server.getHandlers('product'):
            matches = handler.query(query)
            if len(matches) > 0:
                self.sendProduct(matches[0])
        self.send_error(404, 'No matching products')
    
    def prof(self, params):
        '''Handle a profile query.
        '''
        query = self.__createQuery(params)
        tmp = os.tmpfile()
        tmp.writelines((u'<?xml version="1.0" encoding="UTF-8"?>\n',
            u'<!DOCTYPE profiles PUBLIC "-//JPL//DTD Profile 1.1//EN"\n',
            u'  "http://oodt.jpl.nasa.gov/grid-profile/dtd/prof.dtd">\n',
            u'<profiles>\n'))
        for handler in self.server.getHandlers('profile').itervalues():
            for profile in handler.query(query):
                node = profile.toXML(_doc)
                tmp.write(node.toxml())
        tmp.write(u'</profiles>')
        tmp.flush()
        tmp.seek(0L)
        self.send_response(200)
        self.send_header('Content-type', 'text/xml;charset=utf-8')
        size = os.fstat(tmp.fileno())[stat.ST_SIZE]
        self.send_header('Content-length', str(size))
        self.end_headers()
        shutil.copyfileobj(tmp, self.wfile)
        self.log_request(200, size)
    

class WebGridServer(HTTPServer):
    '''Web grid HTTP server.  This server handles incoming HTTP requests and directs them to a
    WebGridRequestHandler.  It also contains the server's ID, and the sequences of profile and
    product handlers.
    '''
    def __init__(self, addr, serverID):
        '''Initialize by saving the server ID and creating empty sequences of profile
        and product handlers.
        '''
        HTTPServer.__init__(self, addr, WebGridRequestHandler)
        self.serverID = serverID
        self.__handlers = {}
        for kind in _validKinds:
            self.__handlers[kind] = {}
    
    def getHandlers(self, kind):
        '''Get the map of handlers for the given kind, which is either "product" or "profile".
        '''
        if kind not in _validKinds:
            raise ValueError('Invalid handler kind "%s"' % kind)
        return self.__handlers[kind]
    

def _main():
    '''Run the web grid server.
    '''
    import sys
    try:
        serverID = sys.argv[1]
    except IndexError:
        serverID = 'oodt'
    listenAddr = ('', 7576)
    httpd = WebGridServer(listenAddr, serverID)
    httpd.serve_forever()
    

if __name__ == '__main__':
    _main()
