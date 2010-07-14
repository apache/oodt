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

package org.apache.oodt.commons.option.handler;

//JDK imports
import java.util.Collections;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.commons.option.CmdLineOption;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Tests the cmd line option bean handler
 * </p>.
 */
public class TestCmdLineOptionBeanHandler extends TestCase {

    /**
     * @since OODT-241
     * 
     */
    public void testHandleOption() {
        TestBean myTestBean = new TestBean();
        BeanInfo info = new BeanInfo();
        info.setBean(myTestBean);
        info.setMethodName("setFlag");
        List<BeanInfo> applyBeans = new Vector<BeanInfo>();
        applyBeans.add(info);

        CmdLineOptionBeanHandler handler = new CmdLineOptionBeanHandler();
        handler.setApplyToBeans(applyBeans);

        CmdLineOption option = new CmdLineOption();
        option.setHandler(handler);
        option.setHasArgs(false);
        option.setId("foo");
        option.setDescription("bar");
        option.setLongOption("foobar");
        option.setShortOption("f");
        option.setRequired(false);
        handler.handleOption(option, Collections.EMPTY_LIST);

        assertTrue(myTestBean.isFlag());
    }

    private class TestBean {

        private boolean flag = false;

        /**
         * @return the flag
         */
        public boolean isFlag() {
            return flag;
        }

        /**
         * @param flag
         *            the flag to set
         */
        public void setFlag(boolean flag) {
            this.flag = flag;
        }

    }

}
