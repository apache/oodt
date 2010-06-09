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


package jpl.eda.product;

/**
 * Statistics exception.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public class StatsException extends Exception {
	/**
	 * Creates a new <code>StatsException</code> instance.
	 *
	 * @param msg Detail message
	 */
	public StatsException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new <code>StatsException</code> instance.
	 *
	 * @param cause Chained exception.
	 */
	public StatsException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new <code>StatsException</code> instance.
	 *
	 * @param msg Detail message
	 * @param cause Chained exception
	 */
	public StatsException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
