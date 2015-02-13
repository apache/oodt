/**
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.apache.oodt.cas.filemgr.datatransfer;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test class for {@link S3DataTransfererFactory}.
 * 
 * @author bfoster@apache.org (Brian Foster)
 */
@RunWith(JUnit4.class)
public class TestS3DataTransfererFactory {

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

  @Test
  public void testCreateDataTransferer() {
    System.setProperty(BUCKET_NAME_PROPERTY, "test-bucket");
    System.setProperty(REGION_PROPERTY, "US_WEST_1");
    System.setProperty(ACCESS_KEY_PROPERTY, "23123123123");
    System.setProperty(SECRET_KEY_PROPERTY, "00101010101");
    System.setProperty(ENCRYPT_PROPERTY, "true");

    S3DataTransfererFactory factory = new S3DataTransfererFactory();
    S3DataTransferer transferer = factory.createDataTransfer();

    assertThat(transferer, is(not(nullValue())));
  }
}
