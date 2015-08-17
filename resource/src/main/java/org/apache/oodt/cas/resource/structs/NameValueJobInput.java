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


package org.apache.oodt.cas.resource.structs;

//JDK imports
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A {@link JobInput} backed by a {@link Properties} object of NameValue pairs.
 * </p>.
 */
public class NameValueJobInput implements JobInput {

  /* our properties object */
  private Properties props = null;

  /* input id */
  private static final String INPUT_ID = "NameValInput";

  /**
   * Default Constructor.
   */
  public NameValueJobInput() {
    props = new Properties();
  }

  public void setNameValuePair(String name, String value) {
    props.setProperty(name, value);
  }

  public String getValue(String name) {
    return props.getProperty(name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.structs.JobInput#getId()
   */
  public String getId() {
    return INPUT_ID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.util.XmlRpcWriteable#read(java.lang.Object)
   */
  public void read(Object in) {
    // we want to make sure that we're reading in
    // a java.util.Hashtable
    // if not then just return
    if (!(in instanceof Hashtable)) {
      return;
    }

    Hashtable readable = (Hashtable) in;
    for (Iterator i = readable.keySet().iterator(); i.hasNext();) {
      String key = (String) i.next();
      String value = (String) readable.get(key);
      this.props.setProperty(key, value);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.util.XmlRpcWriteable#write()
   */
  public Object write() {
    Hashtable writeable = new Hashtable();
    if (props != null && props.size() > 0) {
      for (Iterator i = props.keySet().iterator(); i.hasNext();) {
        String key = (String) i.next();
        String val = props.getProperty(key);
        writeable.put(key, val);
      }
    }

    return writeable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.oodt.cas.resource.util.Configurable#configure(java.util.Properties)
   */
  public void configure(Properties props) {
    if (props != null) {
      this.props = props;
    }
  }

  public Properties getProps(){
    return this.props;
  }

}
