/**
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

package org.apache.oodt.cas.filemgr.metadata;

/**
 * Met key field names used to augment {@link Product} {@link Metadata}.
 * @author adhulipala
 * @version $Revision$
 * @since OODT-847
 */
public interface FileAttributesMetKeys {

    // Basic file attributes
    String IS_SYMBOLIC_LINK = "isSymbolicLink";

    String CREATION_TIME = "creationTime";

    String LAST_MODIFIED_TIME = "lastModifiedTime";

    String IS_OTHER = "isOther";

    String IS_DIRECTORY = "isDirectory";

    String FILE_KEY = "fileKey";

    String LAST_ACCESS_TIME = "lastAccessTime";

    String IS_REGULAR_FILE = "isRegularFile";

    String SIZE = "size";

    // POSIX File attributes
    String OWNER = "owner";

    String PERMISSIONS = "permissions";

    String GROUP = "group";
}
