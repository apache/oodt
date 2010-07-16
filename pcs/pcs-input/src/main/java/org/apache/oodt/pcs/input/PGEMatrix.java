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
 * A PGEMatrix is a set of rows and columns with values in each cell defined by
 * a row number and column number.
 * </p>
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class PGEMatrix {

  private List<List<Object>> rows;

  private int numCols;

  private String name;

  /**
   * <p>
   * Constructs a new PGEMatrix with no rows or columns.
   * </p>
   */
  public PGEMatrix() {
    this(null, 0, 0);
  }

  /**
   * <p>
   * Constructs a new PGEMatrix with the specified <code>numrows</code> and
   * <code>numcols</code>.
   * 
   * @param numrows
   *          The number of rows in the matrix.
   * @param numcols
   *          The number of columns for each row in the matrix.
   */
  public PGEMatrix(String name, int numrows, int numcols) {
    super();
    this.rows = new Vector<List<Object>>(numrows);
    this.numCols = numcols;
    this.name = name;

    for (int i = 0; i < numrows; i++) {
      List<Object> colVector = new Vector<Object>(numcols);
      this.rows.add(i, colVector);
    }
  }

  public void addValue(Object value, int row, int col) {
    if ((row > this.rows.size() || row < 0) || (col > this.numCols || col < 0)) {
      return;
    }

    ((List<Object>) this.rows.get(row)).add(col, value);
  }

  public Object getValue(int row, int col) {
    if ((row > this.rows.size() || row < 0) || (col > this.numCols || col < 0)) {
      return null;
    }

    return ((List<Object>) this.rows.get(row)).get(col);
  }

  /**
   * @return Returns the rows.
   */
  public List<List<Object>> getRows() {
    return this.rows;
  }

  /**
   * @param rows
   *          The rows to set.
   */
  public void setRows(List<List<Object>> rows) {
    this.rows = rows;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return this.name;
  }

  /**
   * @param name
   *          The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return Returns the numCols.
   */
  public int getNumCols() {
    return this.numCols;
  }

  /**
   * @param numCols
   *          The numCols to set.
   */
  public void setNumCols(int numCols) {
    this.numCols = numCols;
  }

}
