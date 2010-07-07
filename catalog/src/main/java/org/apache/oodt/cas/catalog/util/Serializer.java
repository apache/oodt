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

package org.apache.oodt.cas.catalog.util;

//JDK imports
import java.io.InputStream;
import java.io.OutputStream;

//XStream imports
import com.thoughtworks.xstream.XStream;

/**
 * @author bfoster
 * @version $Revision$
 *
 */
public class Serializer {

	protected ClassLoader classLoader;
	protected boolean usePluginUrls;
	
	public Serializer() {
		this(true);
	}

	public Serializer(boolean usePluginUrls) {
		this.usePluginUrls = usePluginUrls;
		this.refreshClassLoader();
	}
	
	public void refreshClassLoader() {
		if (usePluginUrls)
			this.classLoader = new PluginClassLoader();
		else
			this.classLoader = Serializer.class.getClassLoader();
	}
	
	public void setUsePluginUrls(boolean usePluginUrls) {
		this.usePluginUrls = usePluginUrls;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
	/**
	 * Serializes any java object into a java Serializable String
	 * @param <T> Object type to be serialized
	 * @param object The Object of type <T> to be serialized
	 * @return A String for of the object arg
	 */
	public <T> String serializeObject(T object) {
		XStream xStream = new XStream();
		return xStream.toXML(object);
	}
	
	public <T> void serializeObject(T object, OutputStream outStream) {
		XStream xStream = new XStream();
		xStream.toXML(object, outStream);
	}
	
	/**
	 * Deserializes any object that has been serialized by the serializedObject(T) method
	 * @param <T> Type of Object that was originally serialized
	 * @param clazz The class object representation of the object type
	 * @param xmlObject The String for of the object to be deserialized
	 * @return The deserialized object
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public <T> T deserializeObject(Class<T> clazz, String xmlObject) {
		XStream xStream = new XStream();
		xStream.setClassLoader(getClassLoader());
		return (T) xStream.fromXML(xmlObject);
	}
	
	public <T> T deserializeObject(Class<T> clazz, InputStream inStream) {
		XStream xStream = new XStream();
		xStream.setClassLoader(getClassLoader());
		return (T) xStream.fromXML(inStream);
	}

}
