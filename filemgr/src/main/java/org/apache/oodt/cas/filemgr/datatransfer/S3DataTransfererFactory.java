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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * {@link DataTransferFactory} which creates {@link S3DataTransferer}s.
 * 
 * @author bfoster@apache.org (Brian Foster)
 */
public class S3DataTransfererFactory implements DataTransferFactory {

	private static final String BUCKET_NAME_PROPERTY =
			"org.apache.oodt.cas.filemgr.datatransfer.s3.bucket.name";
	private static final String REGION_PROPERTY =
			"org.apache.oodt.cas.filemgr.datatransfer.s3.region";
	private static final String ACCESS_KEY_PROPERTY =
			"org.apache.oodt.cas.filemgr.datatransfer.s3.access.key";
	private static final String SECRET_KEY_PROPERTY =
			"org.apache.oodt.cas.filemgr.datatransfer.s3.secret.key";
  private static final String ENCRYPT_PROPERTY =
      "org.apache.oodt.cas.filemgr.datatransfer.s3.encrypt";

	@Override
  public S3DataTransferer createDataTransfer() {
		String bucketName = System.getProperty(BUCKET_NAME_PROPERTY);
		String region = System.getProperty(REGION_PROPERTY);		
		String accessKey = System.getProperty(ACCESS_KEY_PROPERTY);
    String secretKey = System.getProperty(SECRET_KEY_PROPERTY);
    boolean encrypt = Boolean.getBoolean(ENCRYPT_PROPERTY);

		AmazonS3Client s3 = (AmazonS3Client) AmazonS3ClientBuilder.standard()
		        .withRegion(region)
		        .withCredentials(
		                new AWSStaticCredentialsProvider(
		                        new BasicAWSCredentials(accessKey, secretKey)))
		        .build();

    return new S3DataTransferer(s3, bucketName, encrypt);
  }
}
