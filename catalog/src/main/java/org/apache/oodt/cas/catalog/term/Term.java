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
package org.apache.oodt.cas.catalog.term;

//JDK imports
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A ingestable or queriable item
 * <p>
 */
public class Term implements Cloneable {

	protected String name;
	protected List<String> values;
	protected Type type;
	public enum Type { 
		xml_boolean,
		xml_base64Binary,
		xml_hexBinary,
		xml_anyURI,
		xml_language,
		xml_normalizedString,
		xml_string,
		xml_token,
		xml_byte,
		xml_decimal,
		xml_double,
		xml_float,
		xml_int,
		xml_integer,
		xml_long,
		xml_negativeInteger,
		xml_nonNegativeInteger,
		xml_nonPositiveInteger,
		xml_positiveInteger,
		xml_short,
		xml_unsignedByte,
		xml_unsignedInt,
		xml_unsignedLong,
		xml_unsignedShort,
		xml_date,
		xml_dateTime,
		xml_duration,
		xml_gDay,
		xml_gMonth,
		xml_gMonthDay,
		xml_gYear,
		xml_gYearMonth,
		xml_time,
		xml_Name,
		xml_NCName,
		xml_NOTATION,
		xml_QName,
		xml_ENTITY,
		xml_ENTITIES,
		xml_ID,
		xml_IDREF,
		xml_IDREFS,
		xml_NMTOKEN,
		xml_NMTOKENS,
		xml_anyType,
		xml_anySimpleType
	}

  public Term() {
		this.type = Type.xml_string;
		this.values = Collections.emptyList();
	}
	
	public Term(String name) {
		this();
		this.name = name;
	}
	
	public Term(String name, List<String> values) {
		this(name);
		this.setValues(values);
	}
	
	public Term(String name, List<String> values, Type type) {
		this(name, values);
		if (type != null)
			this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = new Vector<String>(values);
	}
	
	public String getFirstValue() {
		String firstValue = null;
		if (this.values.size() > 0)
			firstValue = this.values.get(0);
		return firstValue; 
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Term) {
			Term compareToTerm = (Term) obj;
			return (this.name.equals(compareToTerm.name)
					&& this.type.equals(compareToTerm.type)
					&& this.values.containsAll(compareToTerm.values) && compareToTerm.values
					.containsAll(this.values));
		}else {
			return false;
		}
	}
	
	@Override
	public Term clone() {
		return new Term(this.name, this.values, this.type);
	}

}
