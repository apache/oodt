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


package org.apache.oodt.cas.pushpull.retrievalsystem;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Met keys needed by the {@link RemoteFile}
 * </p>.
 */
public interface RemoteFileMetKeys {

    public static final String PRODUCT_NAME = "ProductName";

    public static final String RETRIEVED_FROM_LOC = "RetrievedFromLoc";

    public static final String FILENAME = "Filename";

    public static final String DATA_PROVIDER = "DataProvider";

    public static final String FILE_SIZE = "FileSize";

    public static final String RENAMING_STRING = "RenamingString";

    public static final String DOWNLOAD_TO_DIR = "DownloadToDir";

    public static final String PRODUCT_TYPE = "ProductType";

    public static final String SUPER_TYPE = "SuperType";

    public static final String DELETE_AFTER_DOWNLOAD = "DeleteAfterDownload";

}
