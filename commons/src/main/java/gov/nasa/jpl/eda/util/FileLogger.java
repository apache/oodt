//
// FileLogger.java
//
// S. Hardman - 11/28/00
//
// This software was developed by the the Jet Propulsion Laboratory, an
// operating division of the California Institute of Technology, for the
// National Aeronautics and Space Administration, an independent agency of
// the United States Government.
// 
// This software is copyrighted (c) 2000 by the California Institute of
// Technology.  All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, is not permitted under any circumstance without prior
// written permission from the California Institute of Technology.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//
// $Id: FileLogger.java,v 1.1 2004-03-01 16:52:06 kelly Exp $
//

package jpl.eda.util;

import java.io.*;
import java.util.*;
import jpl.eda.io.*;
import jpl.eda.util.*;

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

		<p>jpl.eda.util.FileLogger.path - If specified, this is the directory where
			the log file will be found or created, otherwise the local directory
			will be utilized.

		<p>jpl.eda.util.FileLogger.name - If specified, this is the name of the log
			file to be found or created, otherwise the name will be as follows:
			"eda_yyyymmddhhmmssSSS.log".

		@throws FileNotFoundException If the file cannot be opened for any
			reason.
	*/
	public FileLogger () throws FileNotFoundException {
		this(new FileOutputStream(System.getProperty("jpl.eda.util.FileLogger.path", ".") + "/" + System.getProperty("jpl.eda.util.FileLogger.name", "eda_" + DateConvert.tsFormat(new Date()) + ".log"), true));
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
