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
		