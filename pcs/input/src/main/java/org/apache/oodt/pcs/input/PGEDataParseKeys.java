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

/**
 * 
 * <p>
 * A collection of metadata keys relating to the {@link PGEDataHandler}
 * </p>
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface PGEDataParseKeys {

  public static final int PARSING_VEC = -10;

  public static final int PARSING_SCALAR = -20;

  public static final int PARSING_MATRIX = -30;

  public static final int UNSET = -1;

  public static final String GROUP_TAG_NAME = "group";

  public static final String SCALAR_TAG_NAME = "scalar";

  public static final String VECTOR_TAG_NAME = "vector";

  public static final String MATRIX_TAG_NAME = "matrix";

  public static final String PGE_INPUT_TAG_NAME = "input";

  public static final String VECTOR_ELEMENT_TAG = "element";

  public static final String MATRIX_ROW_TAG = "tr";

  public static final String MATRIX_COL_TAG = "td";

  public static final String NAME_ATTR = "name";

  public static final String ROWS_ATTR = "rows";

  public static final String COLS_ATTR = "cols";
}
