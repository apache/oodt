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
package org.apache.oodt.cas.catalog.server.channel;

//JDK imports
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.catalog.util.Serializer;

/**
 * @author bfoster
 * @version $Revision$
 *
 * <p>
 * An Abstract Communication Channel Client Interface that automatically handles throw execeptions
 * <p>
 */
public abstract class AbstractCommunicationChannelClient implements CommunicationChannelClient {
	
	private static Logger LOG = Logger.getLogger(AbstractCommunicationChannelClient.class.getName());
	protected Serializer serializer;

	public AbstractCommunicationChannelClient() {
		this.serializer = new Serializer(false);
	}
	
}
