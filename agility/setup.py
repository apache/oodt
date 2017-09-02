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

import os.path
from ez_setup import use_setuptools
use_setuptools()
from setuptools import find_packages, setup


# Package data
# ------------

_name         = 'oodt'
_version      = '1.2'
_description  = 'Apache OODT'
_url          = 'http://oodt.apache.org/'
_downloadURL  = 'http://pypi.python.org/pypi/oodt/'
_author       = 'Sean Kelly'
_authorEmail  = 'kelly@apache.org'
_license      = 'Apache 2.0'
_namespaces   = []
_testSuite    = 'oodt.tests.test_suite'
_zipSafe      = True
_keywords     = 'data grid discovery query optimization object middleware archive catalog index'
_requirements = []
_entryPoints  = {
    'console_scripts': ['webgrid = oodt.webgrid:_main'],
}
_classifiers  = [
    'Environment :: Console',
    'Environment :: No Input/Output (Daemon)',
    'Intended Audience :: Developers',
    'Intended Audience :: Information Technology',
    'Intended Audience :: Science/Research',
    'Topic :: Database :: Front-Ends',
    'Topic :: Internet :: WWW/HTTP :: Dynamic Content',
    'Topic :: Internet :: WWW/HTTP :: HTTP Servers',
    'Topic :: Internet :: Z39.50',
    'Topic :: Scientific/Engineering',
    'Development Status :: 5 - Production/Stable',
    'Environment :: Web Environment',
    'License :: OSI Approved :: Apache Software License',
    'Operating System :: OS Independent',
    'Programming Language :: Python',
    'Topic :: Internet :: WWW/HTTP',
    'Topic :: Software Development :: Libraries :: Python Modules',
]


# Setup Metadata
# --------------

def _read(*rnames):
    return open(os.path.join(os.path.dirname(__file__), *rnames)).read()

_header = '*' * len(_name) + '\n' + _name + '\n' + '*' * len(_name)
_longDescription = '\n\n'.join([
    _header,
    _read('README.txt'),
    _read('docs', 'INSTALL.txt'),
    _read('docs', 'HISTORY.txt')
])
open('doc.txt', 'w').write(_longDescription)

setup(
    author=_author,
    author_email=_authorEmail,
    classifiers=_classifiers,
    description=_description,
    download_url=_downloadURL,
    entry_points=_entryPoints,
    include_package_data=True,
    install_requires=_requirements,
    keywords=_keywords,
    license=_license,
    long_description=_longDescription,
    name=_name,
    namespace_packages=_namespaces,
    packages=find_packages(exclude=['ez_setup']),
    test_suite=_testSuite,
    url=_url,
    version=_version,
    zip_safe=_zipSafe,
)
