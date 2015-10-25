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

  int PARSING_VEC = -10;

  int PARSING_SCALAR = -20;

  int PARSING_MATRIX = -30;

  int UNSET = -1;

  String GROUP_TAG_NAME = "group";

  String SCALAR_TAG_NAME = "scalar";

  String VECTOR_TAG_NAME = "vector";

  String MATRIX_TAG_NAME = "matrix";

  String PGE_INPUT_TAG_NAME = "input";

  String VECTOR_ELEMENT_TAG = "element";

  String MATRIX_ROW_TAG = "tr";

  String MATRIX_COL_TAG = "td";

  String NAME_ATTR = "name";

  String ROWS_ATTR = "rows";

  String COLS_ATTR = "cols";
}
