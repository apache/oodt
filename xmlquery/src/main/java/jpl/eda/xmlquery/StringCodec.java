// This software was developed by the Object Oriented Data Technology task of the Science
// Data Engineering group of the Engineering and Space Science Directorate of the Jet
// Propulsion Laboratory of the National Aeronautics and Space Administration, an
// independent agency of the United States Government.
// 
// This software is copyrighted (c) 2000 by the California Institute of Technology.  All
// rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification, is not
// permitted under any circumstance without prior written permission from the California
// Institute of Technology.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
// THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// $Id: StringCodec.java,v 1.1.1.1 2004-03-02 19:37:16 kelly Exp $

package jpl.eda.xmlquery;

import java.io.*;
import java.util.zip.*;
//import jpl.oodt.util.*;
import jpl.eda.util.*;
import org.w3c.dom.*;

/** A result encoder/decoder for strings.
 *
 * This codec uses a string format for objects.
 *
 * @author Kelly
 */
class StringCodec implements Codec {
	public Node encode(Object object, Document doc) throws DOMException {
		Element value = doc.createElement("resultValue");
		value.setAttribute("xml:space", "preserve");
		value.appendChild(doc.createTextNode(object.toString()));
		return value;
	}

	public Object decode(Node node) {
		return XML.text(node);
	}

	public InputStream getInputStream(Object value) throws IOException {
		return new ByteArrayInputStream(((String) value).getBytes());
	}

	public long sizeOf(Object object) {
		return ((String) object).getBytes().length;
	}
}
