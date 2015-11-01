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
package org.apache.oodt.cas.catalog.struct;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * A Interface for storing TransactionIds
 * <p>
 */
public abstract class TransactionId<NativeType> {

	protected NativeType nativeId;
	
	public TransactionId() {}

	public TransactionId(NativeType nativeId) {
		this.nativeId = nativeId;
	}
	
	public TransactionId(String stringId) {
		this.nativeId = this.fromString(stringId);
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	public NativeType getNativeId() {
		return this.nativeId;
	}
		
	/**
	 * Should override this method if NativeType.toString()
	 * does not properly represent the String value of the
	 * native type.  The string value of the NativeType should
	 * be as unique as in its native form.
	 */
	public String toString() {
		return this.nativeId.toString();
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof TransactionId<?>) {
		  return this.toString().equals(obj.toString());
		} else if (obj instanceof String) {
		  return this.toString().equals((String) obj);
		} else {
		  return false;
		}
	}
	
	protected abstract NativeType fromString(String stringId);
	
}
