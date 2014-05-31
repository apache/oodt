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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.exceptions.DataTransferException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.common.collect.Lists;

/**
 * Test class for {@link S3DataTransferer}.
 * 
 * @author bfoster@apache.org (Brian Foster)
 */
@RunWith(JUnit4.class)
public class TestS3DataTransferer {

	private final static String S3_BUCKET_NAME = "TestBucket";
	private final static String ORGINAL_REF = "file:/path/to/file";
	private final static String DATA_STORE_REF = "s3:/path/in/s3/storage/file";
  private final static String EXPECTED_ORGINAL_REF = "/path/to/file";
	private final static String EXPECTED_DATA_STORE_REF = "path/in/s3/storage/file";

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Mock private AmazonS3 s3Client;
	@Mock private Product product;
	@Mock private Reference reference;
	@Mock private S3Object s3Object;
	@Mock private S3ObjectInputStream s3InputStream;

	private S3DataTransferer dataTransferer;
	private File stagingDir;

	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);

		stagingDir = tempFolder.getRoot();
		dataTransferer = new S3DataTransferer(s3Client, S3_BUCKET_NAME, true);

		when(reference.getOrigReference()).thenReturn(ORGINAL_REF);
		when(reference.getDataStoreReference()).thenReturn(DATA_STORE_REF);		
		when(product.getProductReferences()).thenReturn(Lists.newArrayList(reference));
		when(s3Client.getObject(Mockito.<GetObjectRequest>any())).thenReturn(s3Object);
		when(s3Object.getObjectContent()).thenReturn(s3InputStream);
		when(s3InputStream.read(Mockito.<byte[]>any())).thenReturn(-1);
	}

	@Test
	public void testTransferProduct() throws DataTransferException, IOException {
		dataTransferer.transferProduct(product);

		ArgumentCaptor<PutObjectRequest> argument = ArgumentCaptor.forClass(PutObjectRequest.class);
		verify(s3Client).putObject(argument.capture());

		PutObjectRequest request = argument.getValue();
		assertThat(request.getBucketName(), is(S3_BUCKET_NAME));
		assertThat(request.getKey(), is(EXPECTED_DATA_STORE_REF));
		assertThat(request.getFile().getAbsolutePath(), is(EXPECTED_ORGINAL_REF));
	}

	@Test
	public void testRetrieveProduct() throws DataTransferException, IOException {
		dataTransferer.retrieveProduct(product, stagingDir);

		ArgumentCaptor<GetObjectRequest> argument = ArgumentCaptor.forClass(GetObjectRequest.class);
		verify(s3Client).getObject(argument.capture());

		GetObjectRequest request = argument.getValue();
		assertThat(request.getBucketName(), is(S3_BUCKET_NAME));
		assertThat(request.getKey(), is(EXPECTED_DATA_STORE_REF));
	}

	@Test
	public void testDeleteProduct() throws DataTransferException, IOException {
	  dataTransferer.deleteProduct(product);

    ArgumentCaptor<DeleteObjectRequest> argument = ArgumentCaptor
        .forClass(DeleteObjectRequest.class);
	  verify(s3Client).deleteObject(argument.capture());

	  DeleteObjectRequest request = argument.getValue();
	  assertThat(request.getBucketName(), is(S3_BUCKET_NAME));
	  assertThat(request.getKey(), is(EXPECTED_DATA_STORE_REF));
	}
}
