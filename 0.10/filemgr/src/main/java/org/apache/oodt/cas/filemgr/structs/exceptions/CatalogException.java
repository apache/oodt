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

package org.apache.oodt.cas.filemgr.structs.exceptions;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>A generic exception that occured when dealing with the
 * Catalog.</p>
 * 
 */
public class CatalogException extends Exception {

    private static final long serialVersionUID = 3690753990686029110L;

    public CatalogException() {
        super();
    }

    public CatalogException(String message) {
        super(message);
    }

    public CatalogException(String message, Throwable t) {
        super(message, t);
    }
}
