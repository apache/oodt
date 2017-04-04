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
package org.apache.oodt.cas.curation.util;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;

public class AmazonS3Utils {

  private static AWSCredentials credentials = null;
  private static AmazonS3Client client;

  private static void setup() {
    if (client == null) {
      credentials = new ProfileCredentialsProvider().getCredentials();
      client = new AmazonS3Client(credentials);
    }
  }

  private static void setupFromInstanceCredentials() {
    credentials = new InstanceProfileCredentialsProvider().getCredentials();
    client = new AmazonS3Client(credentials);
  }

  public static AmazonS3Client getClient() {
    if (client == null) {
      setup();
    }
    return client;
  }

  public static AmazonS3Client getClientUsingInstanceCreds() {
    if (client == null) {
      setupFromInstanceCredentials();
    }
    else {
      try {
        client.getRegionName();
      } catch (AmazonS3Exception AwsException) {
        setupFromInstanceCredentials();
      }
    }
    return client;
  }

}
