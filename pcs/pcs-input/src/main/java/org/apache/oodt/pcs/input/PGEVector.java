// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.pcs.input;

//JDK imports
import java.util.List;
import java.util.Vector;

/**
 * 
 * <p>
 * A PGEVector is a dynamic set of information, coupled with a name. The
 * information may include number, or string data.
 * </p>
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PGEVector {

  private String name;

  private List<Object> elements;

  /**
   * @param name
   * @param elements
   */
  public PGEVector(String name, List<Object> elements) {
    super();
    this.name = name;
    this.elements = elements;
  }

  /**
   * 
   */
  public PGEVector() {
    this(null, new Vector<Object>());
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
   * @return the elements
   */
  public List<Object> getElements() {
    return elements;
  }

  /**
   * @param elements
   *          the elements to set
   */
  public void setElements(List<Object> elements) {
    this.elements = elements;
  }

}
