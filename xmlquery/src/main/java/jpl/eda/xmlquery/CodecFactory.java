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
// $Id: CodecFactory.java,v 1.1.1.1 2004-03-02 19:37:14 kelly Exp $

package jpl.eda.xmlquery;

import java.io.*;
import java.util.*;

/** A factory for codecs.
 *
 * The codec factory creates and maintains codec objects.
 *
 * @author Kelly
 */
class CodecFactory {
	/** Create a codec.
	 *
	 * If the codec with the given class name already exists, it's returned.
	 * Otherwise, the factory creates a new instance of the codec and returns it.  Any
	 * to instantiate the codec results in a runtime exception.
	 *
	 * @param className Name of the codec class to create.
	 * @return The codec object of the class with the given <var>className</var>.
	 */
	public static Codec createCodec(String className) {
		Codec codec = (Codec) codecs.get(className);
		if (codec == null) try {
			Class clazz = Class.forName(className);
			codec = (Codec) clazz.newInstance();
			codecs.put(className, codec);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("Class \"" + className + "\" not found");
		} catch (InstantiationException ex) {
			throw new RuntimeException("Class \"" + className + "\" is abstract or is an interface");
		} catch (IllegalAccessException ex) {
			throw new RuntimeException("Class \"" + className + "\" doesn't have public no-args constructor");
		}
		return codec;
	}

	/** Cachec codecs; the mapping is from {@link String} class name to {@link Codec} object. */
	private static Map codecs = new HashMap();
}
