/**
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

package org.apache.oodt.grid;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.Serializable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * An abstract server defines the code base and class name of a query handler.
 * 
 */
public abstract class Server implements Serializable {
  /**
   * Creates a new <code>Server</code> instance.
   * 
   * @param configuration
   *          System configuration.
   * @param className
   *          Class name of the query handler.
   */
  public Server(Configuration configuration, String className) {
    this.configuration = configuration;
    this.className = className;
  }

  /**
   * Render this server into XML.
   * 
   * @param owner
   *          Owning document.
   * @return This server, as XML.
   */
  public Node toXML(Document owner) {
    Element elem = owner.createElement("server");
    elem.setAttribute("className", className);
    elem.setAttribute("type", getType());
    return elem;
  }

  /**
   * Get the class name of the query handler.
   * 
   * @return Class name of the query handler.
   */
  public String getClassName() {
    return className;
  }

  /**
   * Create the handler.
   * 
   * @return an instantiated handlre.
   * @throws ClassNotFoundException
   *           if the handler class can't be found.
   * @throws InstantiationException
   *           if the handler object can't be created.
   * @throws IllegalAccessException
   *           if the handler class doesn't provide a public no-args
   *           constructor.
   */
  public Object createHandler() throws ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    List urlList = configuration.getCodeBases();
    Class clazz;
    if (urlList.isEmpty())
      clazz = Class.forName(className);
    else {
      URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);
      URLClassLoader loader = new URLClassLoader(urls, getClass()
          .getClassLoader());
      clazz = loader.loadClass(className);
    }
    return clazz.newInstance();
  }

  /**
   * Set the class name of the handler.
   * 
   * @param className
   *          Class name of the handler.
   */
  public void setClassName(String className) {
    this.className = className;
  }

  /**
   * Return the type of the handler.
   * 
   * @return Either <code>product</code> or <code>profile</code>.
   */
  protected abstract String getType();

  public int hashCode() {
    return configuration.hashCode() ^ className.hashCode();
  }

  public boolean equals(Object obj) {
    if (obj == this)
      return true;
    if (obj instanceof Server) {
      Server rhs = (Server) obj;
      return className.equals(rhs.className);
    }
    return false;
  }

  public String toString() {
    return "Server[className=" + className + "]";
  }

  /**
   * Create a server from an XML element.
   * 
   * @param elem
   *          XML element, presumed to be a &lt;server&gt; element.
   * @return a <code>Server</code> subclass.
   * @throws SAXException
   *           if the element can't be properly parsed.
   */
  public static Server create(Configuration configuration, Element elem)
      throws SAXException {
    String type = elem.getAttribute("type");

    String className = elem.getAttribute("className");

    // Replace with a factory some day...
    if ("product".equals(type))
      return new ProductServer(configuration, className);
    else if ("profile".equals(type))
      return new ProfileServer(configuration, className);
    else
      throw new SAXException("unknown server type `" + type + "'");
  }

  /** Configuration. */
  protected Configuration configuration;

  /** Class name of the handler class. */
  protected String className;

  private static final URL[] EMPTY_URL_ARRAY = new URL[0];
}
