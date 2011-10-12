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
package org.apache.oodt.cas.cl.option.handler;

//JDK imports
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//OODT imports
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.option.store.spring.SpringSetContextInjectionType;

//Spring imports
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.context.ApplicationContext;

/**
 * 
 * @author bfoster
 * @version $Revision$
 */
public class CmdLineOptionBeanHandler extends CmdLineOptionHandler<String> implements
		SpringSetContextInjectionType {

	private List<BeanInfo> applyToBeans;
	private ApplicationContext appContext;

	public void setContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}

	public ApplicationContext getContext() {
		return appContext;
	}

	public void setApplyToBeans(List<BeanInfo> applyToBeans) {
		this.applyToBeans = applyToBeans;
	}

	public void handleOption(CmdLineOptionInstance<String> optionInstance) {
		Validate.notNull(appContext, "Spring ApplicationContext must be set!");
		Validate.notNull(applyToBeans, "Apply to beans must be set!");

		for (BeanInfo beanInfo : applyToBeans) {
			try {
				Class<?> type = optionInstance.getOption().getType();
				Object[] vals = (optionInstance.getValues().isEmpty()) ? convertToType(
						Arrays.asList(new String[] { "true" }), type = Boolean.TYPE)
						: convertToType(optionInstance.getValues(), type);
				Object applyToBean = appContext.getBean(beanInfo.getBeanId());
				if (beanInfo.getMethodName() != null) {
					applyToBean.getClass()
							.getMethod(beanInfo.getMethodName(), type)
							.invoke(applyToBean, vals);
				} else {
					applyToBean
							.getClass()
							.getMethod(
									"set" + StringUtils.capitalize(optionInstance.getOption().getLongOption()), type)
							.invoke(applyToBean, vals);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public String getCustomOptionHelp(CmdLineOption option) {
		Validate.notNull(applyToBeans, "Apply to beans must be set!");

		HashSet<String> affectedClasses = new HashSet<String>();
		for (BeanInfo beanInfo : applyToBeans) {
				affectedClasses.add(beanInfo.getBeanId());
		}
		return "Affects: " + affectedClasses.toString();
	}

	private Object[] convertToType(List<String> values, Class<?> type)
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

	@Override
	public boolean affectsOption(CmdLineOptionInstance optionInstance) {
		Validate.notNull(appContext, "Spring ApplicationContext must be set!");

		@SuppressWarnings("unchecked")
		Map<String, CmdLineOptionInstance> instances = appContext.getBeansOfType(CmdLineOptionInstance.class);
		for (Entry<String, CmdLineOptionInstance> instance : instances.entrySet()) {
			if (instance.getValue().equals(optionInstance)) {
				for (BeanInfo beanInfo : this.applyToBeans) {
					if (beanInfo.getBeanId().equals(instance.getKey())) {
						return true;
					}
				}
				break;
			}
		}
		return false;
	}

}
