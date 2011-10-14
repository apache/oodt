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
import static org.apache.oodt.cas.cl.util.CmdLineUtils.convertToType;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.cl.action.CmdLineAction;
import org.apache.oodt.cas.cl.option.CmdLineOption;
import org.apache.oodt.cas.cl.option.CmdLineOptionInstance;
import org.apache.oodt.cas.cl.store.spring.SpringSetContextInjectionType;

//Spring imports
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.context.ApplicationContext;

/**
 * @author bfoster
 * @version $Revision$
 */
public class CmdLineOptionBeanHandler implements CmdLineOptionHandler,
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

	public void handleOption(CmdLineAction action, CmdLineOptionInstance optionInstance) {
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

	public String getHelp(CmdLineOption option) {
		Validate.notNull(applyToBeans, "Apply to beans must be set!");

		HashSet<String> affectedClasses = new HashSet<String>();
		for (BeanInfo beanInfo : applyToBeans) {
				affectedClasses.add(beanInfo.getBeanId());
		}
		return "Affects: " + affectedClasses.toString();
	}
}
