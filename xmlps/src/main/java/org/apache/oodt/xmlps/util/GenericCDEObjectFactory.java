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

package org.apache.oodt.xmlps.util;

//JDK imports
import org.apache.oodt.xmlps.mapping.funcs.MappingFunc;

import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports

/**
 * 
 * <p>
 * An object factory for creating CDE objects.
 * </p>.
 */
public final class GenericCDEObjectFactory {

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(GenericCDEObjectFactory.class.getName());

    private GenericCDEObjectFactory() throws InstantiationException {
        throw new InstantiationException("Don't construct object factories!");
    }

    public static MappingFunc getMappingFuncFromClassName(String className) {
        MappingFunc func;
        Class funcClazz;
        try {
            funcClazz = Class.forName(className);
            func = (MappingFunc) funcClazz.newInstance();
            return func;
        } catch (ClassNotFoundException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Unable to load class: [" + className
                    + "]: class not found! message: " + e.getMessage(), e);
            return null;
        } catch (InstantiationException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Unable to load class: [" + className
                    + "]: cannot instantiate! message: " + e.getMessage(), e);
            return null;
        } catch (IllegalAccessException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            LOG.log(Level.WARNING, "Unable to load class: [" + className
                    + "]: illegal access! message: " + e.getMessage(), e);
            return null;
        }

    }

}
