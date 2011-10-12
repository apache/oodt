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
package org.apache.oodt.cas.cl.option;

//JDK imports
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * @author bfoster
 * @version $Revision$
 */
public abstract class CmdLineOptionInstance<T> {

	private CmdLineOption<T> option;
	private List<T> values;

	public CmdLineOptionInstance() {
		this.option = null;
		this.values = new ArrayList<T>();
	}

	public CmdLineOptionInstance(CmdLineOption<T> option, List<T> values) {
		Validate.notNull(option);
		Validate.notNull(values);

		this.option = option;
		this.values = values;
	}

	public CmdLineOption<T> getOption() {
		return option;
	}

	public void setOption(CmdLineOption<T> option) {
		this.option = option;
	}

	public List<T> getValues() {
		return values;
	}

	public void addValue(T value) {
		values.add(value);
	}

	public void setValues(List<T> values) {
		Validate.notNull(values);

		this.values = values;
	}

	public boolean equals(Object obj) {
		if (obj instanceof CmdLineOptionInstance) {
			CmdLineOptionInstance<?> compareObj = (CmdLineOptionInstance<?>) obj;
			return compareObj.option.equals(this.option)
					&& compareObj.values.equals(this.values);
		} else
			return false;
	}
}
