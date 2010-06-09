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
 * Recorder of statistics.
 *
 * A recorder is a class implementing this interface and, in addition, providing a one
 * argument constructor that takes a {@link javax.servlet.ServletConfig} object and may
 * throw a {@link javax.servlet.ServletException}.
 *
 * @author Kelly
 * @version $Revision: 1.1 $
 */
public interface Recorder {
	/**
	 * Record the given transaction.
	 *
	 * @param transaction a <code>Transaction</code> value.
	 * @throws StatsException if an error occurs.
	 */
	void record(Transaction transaction) throws StatsException;
}
