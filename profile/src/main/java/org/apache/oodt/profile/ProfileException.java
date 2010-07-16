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


package org.apache.oodt.profile;

/**
 * A profile-related exception.
 *
 * @author Kelly
 */
public class ProfileException extends Exception {
	/**
	 * Create a profile exception with no detail message.
	 */
	public ProfileException() {}

	/**
	 * Create a profile exception with the given detail message.
	 */
	public ProfileException(String message) {
		super(message);
	}

	/**
	 * Create a chained profile exception.
	 *
	 * @param cause Causing exception.
	 */
	public ProfileException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a chained profile exception with detail message.
	 *
	 * @param msg Detail message.
	 * @param cause Causing exception.
	 */
	public ProfileException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
