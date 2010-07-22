#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# make the Metadata library
#
CFLAGS= -g -Wall
CC=gcc
LIBRARIES = -lxerces-c -lstdc++
INCLUDES = -I/usr/include/xercesc/

all:    XMLUtils.o metadata.o

# clean out the dross
clean:
		rm *.o test
        
metadata.o: Metadata.h XStr.h ./util/xml/XMLUtils.h
		$(CC) -c Metadata.cpp $(INCLUDES)

XMLUtils.o: ./util/xml/XMLUtils.h
		$(CC) -c ./util/xml/XMLUtils.cpp $(INCLUDES)

test: XMLUtils.o metadata.o
		$(CC) -g metadata.o XMLUtils.o testMetadata.cpp $(LIBRARIES) -o test
		
