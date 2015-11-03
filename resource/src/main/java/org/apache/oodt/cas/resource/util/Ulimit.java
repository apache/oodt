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


package org.apache.oodt.cas.resource.util;

//JDK imports

import org.apache.oodt.cas.resource.exceptions.ResourceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A static API to detect ulimit properties on an underlying
 * *-nix system.
 * </p>.
 */
public final class Ulimit implements UlimitMetKeys {
    private static Logger LOG = Logger.getLogger(Ulimit.class.getName());
    private static final String shell = "/bin/bash";

    private static final String runShellCmdOption = "-c";

    private static final String ulimitCommand = "ulimit -a";

    private static final String UNKNOWN_VALUE = "N/A";

    public static String getCoreFileSize() {
        try {
            return ((UlimitProperty) getUlimitPropertiesMap().get(
                    CORE_FILE_SIZE)).getValue();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return UNKNOWN_VALUE;
        }

    }

    public static String getDataSegmentSize() {
        try {
            return ((UlimitProperty) getUlimitPropertiesMap().get(
                    DATA_SEGMENT_SIZE)).getValue();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return UNKNOWN_VALUE;
        }

    }

    public static String getFileSize() {
        try {
            return ((UlimitProperty) getUlimitPropertiesMap().get(FILE_SIZE))
                    .getValue();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return UNKNOWN_VALUE;
        }

    }

    public static String getMaxLockedMemory() {
        try {
            return ((UlimitProperty) getUlimitPropertiesMap().get(
                    MAX_LOCKED_MEMORY)).getValue();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return UNKNOWN_VALUE;
        }

    }

    public static String getMaxMemorySize() {
        try {
            return ((UlimitProperty) getUlimitPropertiesMap().get(
                    MAX_MEMORY_SIZE)).getValue();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return UNKNOWN_VALUE;
        }

    }

    public static String getMaxOpenFiles() {
        try {
            return ((UlimitProperty) getUlimitPropertiesMap().get(
                    MAX_OPEN_FILES)).getValue();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return UNKNOWN_VALUE;
        }

    }

    public static String getMaxPipeSize() {
        try {
            return ((UlimitProperty) getUlimitPropertiesMap()
                    .get(MAX_PIPE_SIZE)).getValue();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return UNKNOWN_VALUE;
        }

    }

    public static String getMaxStackSize() {
        try {
            return ((UlimitProperty) getUlimitPropertiesMap().get(
                    MAX_STACK_SIZE)).getValue();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return UNKNOWN_VALUE;
        }

    }

    public static String getMaxCpuTime() {
        try {
            return ((UlimitProperty) getUlimitPropertiesMap().get(MAX_CPU_TIME))
                    .getValue();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return UNKNOWN_VALUE;
        }

    }

    public static String getMaxUserProcesses() {
        try {
            return ((UlimitProperty) getUlimitPropertiesMap().get(
                    MAX_USER_PROCESSES)).getValue();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return UNKNOWN_VALUE;
        }

    }

    public static String getMaxVirtualMemory() {
        try {
            return ((UlimitProperty) getUlimitPropertiesMap().get(
                    MAX_VIRTUAL_MEMORY)).getValue();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage());
            return UNKNOWN_VALUE;
        }

    }

    public static Map getUlimitPropertiesMap() throws ResourceException, IOException {
        Process p;
        try {
            p = Runtime.getRuntime().exec(
                    new String[] { shell, runShellCmdOption, ulimitCommand });
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new ResourceException(
                    "IOException executing ulimit command: Message: "
                            + e.getMessage());
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(p
                .getInputStream()));

        String line;
        Map properties = new ConcurrentHashMap();

        while ((line = in.readLine()) != null) {
            UlimitProperty property = parseProperty(line);
            properties.put(property.getName(), property);
        }
        
        try{
            p.waitFor();
        }
        catch(Exception ignore){}


        return properties;
    }

    public static List getUlimitProperties() throws ResourceException, IOException {
        Process p;
        try {
            p = Runtime.getRuntime().exec(
                    new String[] { shell, runShellCmdOption, ulimitCommand });
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage());
            throw new ResourceException(
                    "IOException executing ulimit command: Message: "
                            + e.getMessage(), e);
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(p
                .getInputStream()));

        String line;
        List properties = new Vector();

        while ((line = in.readLine()) != null) {
            UlimitProperty property = parseProperty(line);
            properties.add(property);
        }
        
        try{
            p.waitFor();
        }
        catch(Exception ignore){}

        return properties;
    }

    private static UlimitProperty parseProperty(String line) {
        // line looks like: cpu time (seconds, -t) unlimited
        String propName = line.substring(0, line.indexOf('(')).trim();
        String propValue = line.substring(line.indexOf(')') + 1).trim();
        return new UlimitProperty(propName, propValue);
    }

}
