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
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * <p>
 * A PGEGroup is a named set of named {@link PGEVector}s, {@link PGEScalar}s and
 * {@link PGEMatrix}s.
 * </p>
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PGEGroup {

  /* the name of the PGEGroup */
  private String name = null;

  /* the ConcurrentHashMap of PGEScalars */
  private Map<String, PGEScalar> scalars = null;

  /* the ConcurrentHashMap of PGEVectors */
  private Map<String, PGEVector> vectors = null;

  /* the ConcurrentHashMap of PGEMatrixs */
  private Map<String, PGEMatrix> matrixs = null;

  /* the ConcurrentHashMap of PGEGroups */
  private Map<String, PGEGroup> groups = null;

  /**
   * <p>
   * Constructs a new PGEGroup with the given name
   * </p>
   */
  public PGEGroup(String name) {
    this.name = name;
    this.scalars = new ConcurrentHashMap<String, PGEScalar>();
    this.vectors = new ConcurrentHashMap<String, PGEVector>();
    this.matrixs = new ConcurrentHashMap<String, PGEMatrix>();
    this.groups = new ConcurrentHashMap<String, PGEGroup>();
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
   * @return the scalars
   */
  public Map<String, PGEScalar> getScalars() {
    return scalars;
  }

  /**
   * @param scalars
   *          the scalars to set
   */
  public void setScalars(Map<String, PGEScalar> scalars) {
    this.scalars = scalars;
  }

  /**
   * @return the vectors
   */
  public Map<String, PGEVector> getVectors() {
    return vectors;
  }

  /**
   * @param vectors
   *          the vectors to set
   */
  public void setVectors(Map<String, PGEVector> vectors) {
    this.vectors = vectors;
  }

  /**
   * @return the matrixs
   */
  public Map<String, PGEMatrix> getMatrixs() {
    return matrixs;
  }

  /**
   * @param matrixs
   *          the matrixs to set
   */
  public void setMatrixs(Map<String, PGEMatrix> matrixs) {
    this.matrixs = matrixs;
  }

  /**
   * @return the groups
   */
  public Map<String, PGEGroup> getGroups() {
    return groups;
  }

  /**
   * @param groups
   *          the groups to set
   */
  public void setGroups(Map<String, PGEGroup> groups) {
    this.groups = groups;
  }

  public void addScalar(PGEScalar scalar) {
    if (this.scalars != null && !this.scalars.containsKey(scalar.getName())) {
      this.scalars.put(scalar.getName(), scalar);
    }
  }

  public void addVector(PGEVector vector) {
    if (this.vectors != null && !this.vectors.containsKey(vector.getName())) {
      this.vectors.put(vector.getName(), vector);
    }
  }

  public void addMatrix(PGEMatrix matrix) {
    if (this.matrixs != null && !this.matrixs.containsKey(matrix.getName())) {
      this.matrixs.put(matrix.getName(), matrix);
    }
  }

  public PGEScalar getScalar(String name) {
    if (this.scalars != null) {
      return this.scalars.get(name);
    } else {
      return null;
    }
  }

  public PGEVector getVector(String name) {
    if (this.vectors != null) {
      return this.vectors.get(name);
    } else {
      return null;
    }
  }

  public PGEMatrix getMatrix(String name) {
    if (this.matrixs != null) {
      return this.matrixs.get(name);
    } else {
      return null;
    }
  }

  public PGEGroup getGroup(String name) {
    if (this.groups != null) {
      return this.groups.get(name);
    } else {
      return null;
    }
  }

  public int getNumScalars() {
    if (this.scalars != null) {
      return this.scalars.size();
    } else {
      return 0;
    }
  }

  public int getNumVectors() {
    if (this.vectors != null) {
      return this.vectors.size();
    } else {
      return 0;
    }
  }

  public int getNumMatrixs() {
    if (this.matrixs != null) {
      return this.matrixs.size();
    } else {
      return 0;
    }
  }

  public int getNumGroups() {
    if (this.groups != null) {
      return this.groups.size();
    } else {
      return 0;
    }
  }

}
