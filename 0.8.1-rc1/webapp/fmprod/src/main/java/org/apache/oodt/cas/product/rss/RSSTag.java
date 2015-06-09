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


package org.apache.oodt.cas.product.rss;

//JDK imports
import java.util.List;
import java.util.Vector;

/**
 * 
 * An output RSS tag to include in an RSS feed as defined by the
 * {@link RSSProductServlet}'s {@link RSSConfig}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class RSSTag {

  private String name;

  private String source;

  private List<RSSTagAttribute> attrs;

  /**
   * Default constructor.
   */
  public RSSTag() {
    this.name = null;
    this.source = null;
    this.attrs = new Vector<RSSTagAttribute>();
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the source
   */
  public String getSource() {
    return source;
  }

  /**
   * @param source
   *          the source to set
   */
  public void setSource(String source) {
    this.source = source;
  }

  /**
   * @return the attrs
   */
  public List<RSSTagAttribute> getAttrs() {
    return attrs;
  }

  /**
   * @param attrs
   *          the attrs to set
   */
  public void setAttrs(List<RSSTagAttribute> attrs) {
    this.attrs = attrs;
  }

}
