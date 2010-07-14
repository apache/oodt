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
import org.apache.oodt.commons.io.*;

/**
	The <code>ScreenLogger</code> class is intended to be used with the
	{@link Log} class in order to setup the System.err as a {@link LogListener}.

	@author S. Hardman
	@version $Revision: 1.1 $
*/
public class ScreenLogger extends WriterLogger {

	/**
		Constructor given no arguments.

		This constructor will specify System.err as the output stream
		and then call the other constructor with the output stream argument.
	*/
	public ScreenLogger () {
		this(System.err);
	}


	/**
		Constructor given an output stream.

		This constructor calls the {@link WriterLogger} constructor with the
		same signature.

		@param outputStream The output stream representing the log destination.
	*/
	public ScreenLogger (OutputStream outputStream) {
		super(outputStream);
	}
}
