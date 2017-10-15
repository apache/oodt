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
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

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
    // a java.util.Map
    // if not then just return
    if (!(in instanceof Map)) {
      return;
    }

    Map readable = (Map) in;
    for (Object o : readable.keySet()) {
      String key = (String) o;
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
    Map writeable = new ConcurrentHashMap();
    if (props != null && props.size() > 0) {
      for (Object o : props.keySet()) {
        String key = (String) o;
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

  @Override
  public Map<String, Vector<String>> getMetadata() {
    Map<String, Vector<String>> met = new HashMap<String, Vector<String>>(); 
    if (props != null && props.keySet() != null && props.keySet().size() > 0){
       for (Object key: props.values()){
         String keyName = (String)key;
         Vector<String> vals = new Vector<String>();
         vals.add(props.getProperty(keyName));
         met.put(keyName, vals);
       }
     }
    return met;
  }
  
  public Properties getProps(){
    return this.props;
  }

}
