#!/usr/bin/perl -w
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
#limitations under the License.

# This is an example of an extern script that is used for a pre-ingest
# condition. In this example, if the string 'PARTIAL' or 'INCOMPLETE'
# are found in 
#
# Returns:     0 upon success, 1 for failure
#
use strict;
use Env;
use File::Basename;
use Time::Piece;
#
# Globals
#
my  $scriptName = $0;

#
# Function Prototypes
#
sub Main();

#
# Main Program
#
Main();
exit 0;  # 0 = success

#
# Functions
#
sub Main() 
{

   # Get the command line argument
   my $fullSpec = shift(@ARGV);

   #
   # Check to make sure the input file exists.
   #
   unless ( -e $fullSpec ) {
      print "ERROR: ". $scriptName . ": Input file \[$fullSpec\] Doesn't exist!\n";
      exit 1;
   }

   #
   # Break up the file specification into parts. In this form of the fileparse
   # command, the file extension includes the '.' and the directory name includes
   # the '/' at the end.
   #
   my ($baseName, $dir, $ext ) = fileparse( $fullSpec , qr/\.[^.]*/);
   print "INFO: ". $scriptName . ": Processing file: \[$fullSpec\]\n";
   chop( $dir ); # remove last '/' character.

   #
   # If the file is a .out science file, check that there is a coresponding .dtl
   # available to check for the .out file's complete status. 
   #
   if ( $ext =~ /.*\.out/ ) {
      my $dtlFile = $fullSpec;
      $dtlFile =~ s/\.out/\.dtl/;
      unless ( -e $dtlFile ) {
          print "ERROR: ". $scriptName . ": .dtl file \[$dtlFile\] Doesn't exist for $baseName$ext!\n";
          exit 1;
      }
      #
      # If the .dtl file does exist, open it and check for the existence of strings
      # indicating whether the .out file is a complete file or not. 
      #
      open DTLFILE, "<$dtlFile";
      my @dtlInfo = <DTLFILE>;
      close DTLFILE;
      if ( grep /DATA_INCOMPLETE/, @dtlInfo or grep /PARTIAL/, @dtlInfo ) {
          print "INFO: ". $scriptName . ": Input file \[$fullSpec\] Is not a complete file.\n";
          exit 1;
      }
   }

} # End of Main

