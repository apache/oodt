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

package org.apache.oodt.cas.filemgr.datatransfer;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.oodt.cas.filemgr.structs.Product;

//JDK imports
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Objects implementing this interface define how to transfer products to the
 * {@link DataStore} repository.
 * </p>
 * 
 */
public interface DataTransfer {
	/* extension point ID */
	public static final String X_POINT_ID = DataTransfer.class.getName();
	
	/**
	 * 
	 * @param url The URL to the File Manager that this transferer will be
	 * transferring Products to.
	 */
	public void setFileManagerUrl(URL url);
	
	/**
	 * 
	 * @param product
	 *            The product that is being transferred. The product should have
	 *            both its origFileLocation, as well as its dataStoreRefs filled
	 *            in to perform the transfer.
	 * @throws DataTransferException
	 *             If a general error occurs during the transfer.
	 * @throws IOException
	 *             If there is an IO eerror when performing the transfer.
	 */
	public void transferProduct(Product product) throws DataTransferException,
			IOException;

	  /**
    * Requires that the data store reference be set, nothing else is used
    * @param product The product whose data store reference will be copied
    * @param directory The directory where the data store reference will be copied to
    * @throws DataTransferException
    *             If a general error occurs during the transfer.
    * @throws IOException
    *             If there is an IO eerror when performing the transfer.
    */
   public void retrieveProduct(Product product, File directory) throws DataTransferException,
         IOException;
   
   public void deleteProduct(Product product) throws DataTransferException, IOException;
}
