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


package org.apache.oodt.cas.resource.structs.exceptions;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * An exception thrown when persisting a {@link Job} to a {@link JobRepository}.
 * </p>.
 */
public class JobRepositoryException extends Exception {

  /* serial version UID */
  private static final long serialVersionUID = 7431338125659704680L;

  /**
   * 
   */
  public JobRepositoryException() {
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   */
  public JobRepositoryException(String arg0) {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   */
  public JobRepositoryException(Throwable arg0) {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param arg0
   * @param arg1
   */
  public JobRepositoryException(String arg0, Throwable arg1) {
    super(arg0, arg1);
    // TODO Auto-generated constructor stub
  }

}
