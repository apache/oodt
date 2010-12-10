/**
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

package org.apache.oodt.opendapps.util;

//JDK imports
import java.io.IOException;

//APACHE imports
import org.apache.oodt.opendapps.DatasetExtractor;
import org.apache.oodt.xmlquery.XMLQuery;

/**
 * 
 * Evaluates the resultant OPeNDAP URLs returned from a THREDDS catalog by the
 * {@link DatasetExtractor}.
 * 
 */
public class OpendapURLEvaluator {

  public static void main(String[] args) throws IOException {
    String datasetUrl = args[0];
    String catalogUrl = args[1];

    DatasetExtractor gen = new DatasetExtractor(getQuery(), catalogUrl,
        datasetUrl);
    System.out.println(gen.getDapUrls());
  }

  private static XMLQuery getQuery() {
    return new XMLQuery("PFunction=findall", "cmdline", "cmdline", null, null,
        null, null, null, XMLQuery.DEFAULT_MAX_RESULTS, true);
  }

}
