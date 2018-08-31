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

import static com.amazonaws.services.s3.model.ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.apache.tika.io.IOUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

/**
 * {@link DataTransfer} which put/gets files in/from Amazon S3 storage.
 * 
 * @author bfoster@apache.org (Brian Foster)
 */
public class S3DataTransferer implements DataTransfer {

	private final AmazonS3 s3Client;
	private final String bucketName;
	private final boolean encrypt;

	public S3DataTransferer(AmazonS3 s3Client, String bucketName, boolean encrypt) {
		this.s3Client = checkNotNull(s3Client);
		this.bucketName = checkNotNull(bucketName);
		this.encrypt = encrypt;
	}

	@Override
	public void setFileManagerUrl(URL url) {}

	@Override
	public void transferProduct(Product product) throws DataTransferException, IOException {
		for (Reference ref : product.getProductReferences()) {
      String origRef = stripProtocol(ref.getOrigReference(), false);
		  String dataStoreRef = stripProtocol(ref.getDataStoreReference(), true);
			try {
			  PutObjectRequest request = new PutObjectRequest(
			      bucketName, dataStoreRef, new File(origRef));
			  if (encrypt) {
  				ObjectMetadata requestMetadata = new ObjectMetadata();
  				requestMetadata.setSSEAlgorithm(AES_256_SERVER_SIDE_ENCRYPTION);
  				request.setMetadata(requestMetadata);
			  }
        s3Client.putObject(request);
			} catch (AmazonClientException e) {
				throw new DataTransferException(String.format(
				    "Failed to upload product reference %s to S3 at %s", origRef,
				    dataStoreRef), e);
			}
		}
	}

	@Override
	public void retrieveProduct(Product product, File directory) throws DataTransferException,
	    IOException {
		for (Reference ref : product.getProductReferences()) {
      GetObjectRequest request = new GetObjectRequest(bucketName, stripProtocol(
          ref.getDataStoreReference(), true));
			S3Object file = s3Client.getObject(request);
			stageFile(file, ref, directory);
		}
	}

  @Override
  public void deleteProduct(Product product) throws DataTransferException, IOException {
    for (Reference ref : product.getProductReferences()) {
      DeleteObjectRequest request = new DeleteObjectRequest(bucketName, stripProtocol(
          ref.getDataStoreReference(), true));
      try {
        s3Client.deleteObject(request);
      } catch (AmazonClientException e) {
        throw new DataTransferException(String.format(
            "Failed to delete product reference %s from S3", ref.getDataStoreReference()), e);
      }
    }
  }

	private void stageFile(S3Object file, Reference ref, File directory) throws IOException {
		S3ObjectInputStream inStream = null;
		FileOutputStream outStream = null;
		try {
			inStream = file.getObjectContent();
			outStream = new FileOutputStream(new File(directory, new File(
			    stripProtocol(ref.getDataStoreReference(), false)).getName()));
			IOUtils.copy(inStream, outStream);
		} finally {
			try { inStream.close(); } catch (Exception ignored) {}
			try { outStream.close(); } catch (Exception ignored) {}
		}
	}

	private String stripProtocol(String ref, boolean stripLeadingSeparator) throws IOException {
	  try {
      String path = new URI(ref).getPath();
      if (stripLeadingSeparator) {
        path = path.replaceAll("^/", "");
      }
      return path;
    } catch (URISyntaxException e) {
      throw new IOException(e);
    }
	}
}
