//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.commons.option.handler;

//JDK imports
import java.util.Collections;
import java.util.List;
import java.util.Vector;

//OODT imports
import gov.nasa.jpl.oodt.cas.commons.option.CmdLineOption;

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
