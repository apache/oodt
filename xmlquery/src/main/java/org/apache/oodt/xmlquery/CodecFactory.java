/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oodt.xmlquery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
		if (codec == null) {
		  try {
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
		}
		return codec;
	}

	/** Cachec codecs; the mapping is from {@link String} class name to {@link Codec} object. */
	private static Map codecs = new ConcurrentHashMap();
}
