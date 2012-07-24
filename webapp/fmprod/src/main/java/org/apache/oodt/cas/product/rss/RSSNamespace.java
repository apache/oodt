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

/**
 * 
 * RSS namespace definition to include in an RSS feed as defined by the
 * {@link RSSProductServlet}'s {@link RSSConfig}.
 * 
 * @author rlaidlaw
 * @version $Revision$
 */
public class RSSNamespace 
{
  private String prefix;
  private String uri;

  /**
   * Default constructor.
   */
  public RSSNamespace() { }

  /**
   * @return the prefix
   */
  public String getPrefix() { return prefix; }

  /**
   * @param prefix
   *          the prefix to set
   */
  public void setPrefix(String prefix) { this.prefix = prefix;}

  /**
   * @return the uri
   */
  public String getUri() { return uri; }

  /**
   * @param uri
   *          the uri to set
   */
  public void setUri(String uri) { this.uri = uri; }
}