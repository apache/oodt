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
import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.util.PathUtils;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Java ClassLoader for loading plugin classes in Catalogs and QueryExpressions</p>.
 */
public class PluginClassLoader extends URLClassLoader {

	private static final Logger LOG = Logger.getLogger(PluginClassLoader.class.getName());
	
	public PluginClassLoader() {
		super(getPluginURLs());
	}
	
	public PluginClassLoader(ClassLoader parent) {
		super(new URL[0], parent);
	}

	protected void addURL(PluginURL pluginURL) {
		super.addURL(pluginURL.getURL());
	}
	
	protected void addURLs(List<URL> urls) {
		for (URL url : urls) {
		  this.addURL(url);
		}
	}
	
	public static URL[] getPluginURLs() {
		List<URL> urls = new Vector<URL>();
		try {
			String pluginDirs = System.getProperty("org.apache.oodt.cas.catalog.plugin.dirs");
			if (pluginDirs != null) {
				for (String pluginDir : PathUtils.doDynamicReplacement(pluginDirs).split(",")) {
					File[] jarFiles = new File(pluginDir).listFiles(new FileFilter() {
						public boolean accept(File pathname) {
							return pathname.getName().endsWith(".jar");
						}					
					});
					for (File jarFile : jarFiles) {
						try {
							urls.add(jarFile.toURL());
						}catch (Exception e) {
							LOG.log(Level.SEVERE, "Failed to load jar file '" + jarFile + "' : " + e.getMessage(), e);
						}
					
					}
				}
			}
		}catch (Exception ignored) {}
		return urls.toArray(new URL[urls.size()]);
	}
	
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			Class<?> clazz = this.findLoadedClass(name);
			if (clazz == null) {
			  clazz = this.findClass(name);
			}
			return clazz;
		}catch (Exception ignored) {}
		return super.loadClass(name, resolve);
	}

}
