// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.commons.util;

import java.io.*;
import java.util.*;
import org.apache.oodt.commons.io.*;
import org.apache.oodt.commons.util.*;

/**
	The <code>FileLogger</code> class is intended to be used with the
	{@link Log} class in order to setup a log file as a {@link LogListener}.

	@author S. Hardman
	@version $Revision: 1.1 $
*/
public class FileLogger extends WriterLogger {

	/**
		Constructor given no arguments.

		This constructor will create a new log file or append to an existing
		log file if the specified file exists. The constructor will then call
		the other constructor with the output stream argument. This constructor
		utilizes two system properties for specifying the path and name of the
		log file. They are as follows:

		<p>org.apache.oodt.commons.util.FileLogger.path - If specified, this is the directory where
			the log file will be found or created, otherwise the local directory
			will be utilized.

		<p>org.apache.oodt.commons.util.FileLogger.name - If specified, this is the name of the log
			file to be found or created, otherwise the name will be as follows:
			"eda_yyyymmddhhmmssSSS.log".

		@throws FileNotFoundException If the file cannot be opened for any
			reason.
	*/
	public FileLogger () throws FileNotFoundException {
		this(new FileOutputStream(System.getProperty("org.apache.oodt.commons.util.FileLogger.path", ".") + "/" + System.getProperty("org.apache.oodt.commons.util.FileLogger.name", "eda_" + DateConvert.tsFormat(new Date()) + ".log"), true));
	}


	/**
		Constructor given an output stream.

		This constructor calls the {@link WriterLogger} constructor with the
		same signature.

		@param outputStream The output stream representing the log destination.
	*/
	public FileLogger (OutputStream outputStream) {
		super(outputStream);
	}
}
