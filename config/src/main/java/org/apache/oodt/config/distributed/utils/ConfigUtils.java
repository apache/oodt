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
import org.apache.oodt.config.Constants;
import org.apache.oodt.config.Constants.Env;
import org.apache.oodt.config.Constants.ZPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.oodt.config.Constants.Properties.OODT_PROJECT;
import static org.apache.oodt.config.Constants.SEPARATOR;

/**
 * A utility class to be used for configuration related tasks.
 *
 * @author Imesha Sudasingha
 */
public class ConfigUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    private ConfigUtils() {
    }

    /**
     * Fix a given path to start from given {@link Component}'s home directory. Home directory will be fetched either
     * through a system property or through an environment variable.
     *
     * @param component  OODT {@link Component}
     * @param suffixPath path to be fixed
     * @return fixed path
     */
    public static String fixForComponentHome(Component component, String suffixPath) {
        String prefix = System.getProperty(component.getHome());
        if (prefix == null) {
            prefix = System.getenv().get(component.getHome());
        }

        StringBuilder path = new StringBuilder();
        if (prefix != null && !prefix.trim().isEmpty()) {
            prefix = prefix.trim();
            logger.debug("Found prefix {}:{} for suffixPath: {}", component.getHome(), prefix, suffixPath);
            path.append(prefix.endsWith(SEPARATOR) ? prefix : prefix + SEPARATOR);
        }
        path.append(suffixPath.startsWith(ZPaths.SEPARATOR) ? suffixPath.substring(ZPaths.SEPARATOR.length()) : suffixPath);
        logger.debug("Fixed path for {} is {}", suffixPath, path.toString());
        return path.toString();
    }

    /**
     * Get the name of the project name (optional) if specified. Else return a default value
     *
     * @return OODT project name
     */
    public static String getOODTProjectName() {
        String project = System.getProperty(OODT_PROJECT);
        if (project == null) {
            project = System.getenv(Env.OODT_PROJECT);
        }

        logger.debug("Project name {}", project);
        return project == null ? Constants.DEFAULT_PROJECT : project;
    }
}
