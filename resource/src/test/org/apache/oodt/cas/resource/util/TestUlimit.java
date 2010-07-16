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
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test case for the Ulimit monitoring API.
 * </p>.
 */
public class TestUlimit extends TestCase {

    public void testGetMaxOpenFiles() {
        String maxOpenFiles = Ulimit.getMaxOpenFiles();
        assertNotNull(maxOpenFiles);
        assertTrue(new UlimitProperty("foo", maxOpenFiles).isUnlimited()
                || !new UlimitProperty("foo", maxOpenFiles).isUnlimited());

    }

    public void testUlimitProperty() {
        String maxStackSize = Ulimit.getMaxStackSize();
        if (new UlimitProperty("foo", maxStackSize).isUnlimited()) {
            assertTrue(new UlimitProperty("foo", maxStackSize).getIntValue() == -1);
        } else {
            assertTrue(new UlimitProperty("foo", maxStackSize).getIntValue() != -1);
        }
    }

}
