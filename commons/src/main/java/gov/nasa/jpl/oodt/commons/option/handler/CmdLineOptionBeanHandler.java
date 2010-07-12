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


package gov.nasa.jpl.oodt.cas.commons.option.handler;

//JDK imports
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//OODT imports
import gov.nasa.jpl.oodt.cas.commons.option.CmdLineOption;
import gov.nasa.jpl.oodt.cas.commons.spring.SpringSetIdInjectionType;

//Spring imports
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class CmdLineOptionBeanHandler extends CmdLineOptionHandler {

    private List<BeanInfo> applyToBeans;

    @Required
    public void setApplyToBeans(List<BeanInfo> applyToBeans) {
        this.applyToBeans = applyToBeans;
    }

    public void handleOption(CmdLineOption option, List<String> values) {
        try {
            for (BeanInfo applyToBean : this.applyToBeans) {
                Class type = (option.getType() != null) ? option.getType()
                        : String.class;
                Object[] vals = null;
                if (values.size() != 0) {
                    vals = this.convertToType(values, type);
                } else {
                    vals = this.convertToType(Arrays
                            .asList(new String[] { "true" }), type = Boolean.TYPE);
                }
                try {
                    if (applyToBean.getMethodName() != null) {
                        applyToBean.getBean().getClass().getMethod(
                                applyToBean.getMethodName(), type).invoke(
                                applyToBean.getBean(), vals);
                    } else {
                        applyToBean.getBean().getClass().getMethod(
                                "set" + StringUtils.capitalize(option.getId()),
                                type).invoke(applyToBean.getBean(), vals);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCustomOptionUsage(CmdLineOption option) {
        Set<String> affectedClasses = new HashSet<String>();
        for (BeanInfo beanInfo : applyToBeans) {
            if (beanInfo.getBean() instanceof SpringSetIdInjectionType)
                affectedClasses.add(((SpringSetIdInjectionType) beanInfo
                        .getBean()).getId());
            else
                affectedClasses.add(beanInfo.getBean().getClass().getName());
        }
        return "Affects: " + affectedClasses.toString();
    }

    private Object[] convertToType(List<String> values, Class type)
            throws MalformedURLException, ClassNotFoundException {
        if (type.equals(File.class)) {
            List<Object> files = new LinkedList<Object>();
            for (String value : values)
                files.add(new File(value));
            return files.toArray(new Object[files.size()]);
        } else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
            List<Object> booleans = new LinkedList<Object>();
            for (String value : values)
                booleans.add(value.toLowerCase().trim().equals("true"));
            return booleans.toArray(new Object[booleans.size()]);
        } else if (type.equals(URL.class)) {
            List<Object> urls = new LinkedList<Object>();
            for (String value : values)
                urls.add(new URL(value));
            return urls.toArray(new Object[urls.size()]);
        } else if (type.equals(Class.class)) {
            List<Object> classes = new LinkedList<Object>();
            for (String value : values)
                classes.add(Class.forName(value));
            return classes.toArray(new Object[classes.size()]);
        } else if (type.equals(List.class)) {
            return new Object[] { values };
        } else if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            List<Object> ints = new LinkedList<Object>();
            for (String value : values)
                ints.add(new Integer(value));
            return ints.toArray(new Object[ints.size()]);
        } else if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            List<Object> longs = new LinkedList<Object>();
            for (String value : values)
                longs.add(new Long(value));
            return longs.toArray(new Object[longs.size()]);
        } else if (type.equals(Double.class) || type.equals(Double.TYPE)) {
            List<Object> doubles = new LinkedList<Object>();
            for (String value : values)
                doubles.add(new Double(value));
            return doubles.toArray(new Object[doubles.size()]);
        } else if (type.equals(String.class)) {
        	StringBuffer combinedString = new StringBuffer("");
        	for (String value : values)
        		combinedString.append(value + " ");
        	return new String[] { combinedString.toString().trim() };
        } else {
            return values.toArray(new Object[values.size()]);
        }
    }

}
