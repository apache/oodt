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

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Ulimit returned property, mapping a name to a particular
 * value. The value may be "unlimited", indicating there is no
 * limit on the properties value. In this case, a call to
 * {@link #isUnlimited()} can be used to detect this.
 * </p>.
 */
public class UlimitProperty {

    private String name;

    private String value;

    private static final String UNLIMITED_VAL = "unlimited";

    public UlimitProperty() {
    }

    /**
     * @param name
     * @param value
     */
    public UlimitProperty(String name, String value) {
        super();
        this.name = name;
        this.value = value;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    public boolean isUnlimited() {
        if (this.value.equals(UNLIMITED_VAL)) {
            return true;
        } else {
            try {
                Integer.parseInt(this.value);
                return false;
            } catch (Exception ignore) {
                // not a number, so unlimited
                return true;
            }
        }

    }

    public int getIntValue() {
        if (isUnlimited()) {
            return -1;
        } else {
            return Integer.parseInt(this.value);
        }
    }

}
