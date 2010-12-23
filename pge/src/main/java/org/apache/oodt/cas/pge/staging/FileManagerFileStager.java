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
package org.apache.oodt.cas.pge.staging;

//OODT imports
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.datatransfer.RemoteDataTransferFactory;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.pge.metadata.PGETaskMetKeys;
import org.apache.oodt.cas.workflow.metadata.ControlMetadata;

//JDK imports
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.logging.Level;

/**
 * 
 * @author bfoster
 *
 */
public class FileManagerFileStager extends FileStager {
	
	public void stageFile(String origPath, String destDir, ControlMetadata ctrlMetadata) throws Exception {
		DataTransfer dataTransferer = null;
		if (ctrlMetadata.getMetadata(PGETaskMetKeys.QUERY_CLIENT_TRANSFER_SERVICE_FACTORY) != null) 
			dataTransferer = GenericFileManagerObjectFactory.getDataTransferServiceFromFactory(ctrlMetadata.getMetadata(PGETaskMetKeys.QUERY_CLIENT_TRANSFER_SERVICE_FACTORY));
		else
			dataTransferer = new RemoteDataTransferFactory().createDataTransfer();
		String filemgrUrl = ctrlMetadata.getMetadata(PGETaskMetKeys.QUERY_FILE_MANAGER_URL);
		if (filemgrUrl != null)
			dataTransferer.setFileManagerUrl(new URL(filemgrUrl));
		else
			LOG.log(Level.WARNING, "Metadata field '" + PGETaskMetKeys.QUERY_FILE_MANAGER_URL + "' was not set, if DataTranferer requires filemgr server, your transfers will fail");
		dataTransferer.copyProduct(createDummyProduct(origPath), new File(destDir));
	}

	private Product createDummyProduct(String path) {
		Product dummy = new Product();
		Reference reference = new Reference();
		reference.setDataStoreReference("file:" + new File(path).getAbsolutePath());
		dummy.setProductReferences(Collections.singletonList(reference));
		return dummy;
	}
	
}
