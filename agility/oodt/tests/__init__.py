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

'''Agile OODT Tests.
'''

__docformat__ = 'restructuredtext'

import unittest
import profileTest, queryTest, xmlutilsTest

def test_suite():
    '''Create the suite of tests.
    '''
    suite = unittest.TestSuite()
    suite.addTest(profileTest.test_suite())
    suite.addTest(queryTest.test_suite())
    suite.addTest(xmlutilsTest.test_suite())
    return suite
    

if __name__ == '__main__':
    unittest.main(defaultTest='test_suite')
    
