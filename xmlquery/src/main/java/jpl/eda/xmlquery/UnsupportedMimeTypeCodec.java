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
// $Id: UnsupportedMimeTypeCodec.java,v 1.1.1.1 2004-03-02 19:37:16 kelly Exp $

package jpl.eda.xmlquery;

import java.io.*;
import org.w3c.dom.*;

/** A result encoder/decoder for unsupported MIME types.
 *
 * This codec throws <code>UnsupportedOperationException</code>s on any encoding or
 * decoding attempt.
 *
 * @author Kelly
 */
class UnsupportedMimeTypeCodec implements Codec {
	public Node encode(Object object, Document doc) {
		throw new UnsupportedOperationException("MIME type not supported for encoding");
	}
	public Object decode(Node node) {
		throw new UnsupportedOperationException("MIME type not supported for decoding");
	}
	public long sizeOf(Object object) {
		throw new UnsupportedOperationException("MIME type not supported for sizing");
	}
	public InputStream getInputStream(Object object) {
		throw new UnsupportedOperationException("MIME type not supported for streaming");
	}
}
