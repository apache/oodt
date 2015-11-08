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


package org.apache.oodt.cas.metadata.util;

//JDK imports
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Utility methods for handing property values
 * </p>.
 */
public final class PropertiesUtils {

    /**
     * Loads a set of System properties that are specified as comma-delimited
     * strings and returns an array of String values for the specified property.
     * 
     * @param property
     *            The property to read from the system properties.
     * @param defaultValues
     *            A set of default values to use for the property.
     * @return An array of String property values for the specified property.
     */
    public static String[] getProperties(String property, String[] defaultValues) {
        String[] values = getProperties(property);
        if (values.length < 1) {
            values = new String[defaultValues.length];
            System.arraycopy(defaultValues, 0, values, 0, defaultValues.length);
        }
        return values;
    }

    /**
     * Loads and parses a given property
     * 
     * @param property
     *            The property to be loaded and parsed
     * @return A string array of properties values
     */
    public static String[] getProperties(String property) {
        Vector propList = new Vector();
        StringTokenizer st = new StringTokenizer(System.getProperty(property,
                ""), ",");
        while (st.hasMoreTokens()) {
            propList.add(PathUtils.replaceEnvVariables(st.nextToken().trim()));
        }
        return (String[]) propList.toArray(new String[propList.size()]);
    }
    
    /**
     * Loads and parses a given property
     * 
     * @param property
     *            The property to read from the system properties.
     * @return A string properties value
     */
    public static String getProperty(String property, String defaultValue) {
        return getProperties(property, new String[] { defaultValue })[0];
    }
    
    /**
     * Loads and parses a given property
     * 
     * @param property
     *            The property to be loaded and parsed
     * @return A string properties value
     */
    public static String getProperty(String property) {
        return getProperties(property)[0];
    }

}
