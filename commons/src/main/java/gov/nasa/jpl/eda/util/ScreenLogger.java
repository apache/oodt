//
// ScreenLogger.java
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
// $Id: ScreenLogger.java,v 1.1 2004-03-01 16:52:06 kelly Exp $
//

package jpl.eda.util;

import java.io.*;
import jpl.eda.io.*;

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
