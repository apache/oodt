/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.config.distributed.utils;

import org.apache.oodt.config.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.oodt.config.Constants.SEPARATOR;

public class FilePathUtils {

    private static final Logger logger = LoggerFactory.getLogger(FilePathUtils.class);

    private FilePathUtils() {
    }

    public static String fixForComponentHome(Component component, String suffixPath) {
        String prefix = System.getenv().get(component.getHome());
        StringBuilder path = new StringBuilder();
        if (prefix != null) {
            logger.debug("Found prefix {}:{} for suffixPath: {}", component.getHome(), prefix, suffixPath);
            path.append(prefix.endsWith(SEPARATOR) ? prefix : prefix + SEPARATOR);
        }
        path.append(suffixPath.startsWith(SEPARATOR) ? suffixPath.substring(1) : suffixPath);
        logger.debug("Fixed path for {} is {}", suffixPath, path.toString());
        return path.toString();
    }

    public static String unfixForComponentHome(Component component, String path) {
        String prefix = System.getenv().get(component.getHome());
        if (prefix != null && path.startsWith(prefix)) {
            return path.substring(prefix.length() + SEPARATOR.length());
        }

        return path;
    }
}
