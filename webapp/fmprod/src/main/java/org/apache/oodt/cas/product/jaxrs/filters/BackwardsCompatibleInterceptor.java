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

package org.apache.oodt.cas.product.jaxrs.filters;

import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

/**
 * An Apache CXF interceptor for the incoming chain that monitors request URIs
 * and maps any URIs from the original scheme to the new scheme.
 * @author rlaidlaw
 * @version $Revision$
 */
public class BackwardsCompatibleInterceptor extends
  AbstractPhaseInterceptor<Message>
{
  /**
   * Constructor that inserts the interceptor at the USER_STREAM phase of the
   * incoming interceptor chain.
   */
  public BackwardsCompatibleInterceptor()
  {
    super(Phase.USER_STREAM);
  }

  @Override
  public void handleMessage(Message message) throws Fault
  {
    String base = (String) message.get(Message.BASE_PATH);
    String uri = (String) message.get(Message.REQUEST_URI);
    String query = (String) message.get(Message.QUERY_STRING);

    base += base.endsWith("/") ? "" : "/";
    String request = uri.replaceAll("^" + base, "");

    // Parse the query string into a map of parameters.
    // [Note: this will overwrite multiple parameters that have the same name.]
    List<NameValuePair> params = URLEncodedUtils.parse(query, Charset.forName("UTF-8"));
    Map<String, String> map = new ConcurrentHashMap<String, String>();
    for (NameValuePair pair : params)
    {
      map.put(pair.getName(), pair.getValue());
    }

    // Maps "data?productID=<product ID>" URI to either
    // "reference.file?productId=<product ID>" or
    // "product.zip?productId=<product ID>"
    if(request.equals("data"))
    {
      String format = map.get("format");
      request =
        "application/x-zip".equals(format) || "application/zip".equals(format)
        ? "product.zip" : "reference.file";

      query = "productId=" + map.get("productID");
      query += map.containsKey("refIndex")
        ? "&refIndex=" + map.get("refIndex") : "";
    }

    // Maps "dataset?typeID=<product type ID>" to
    // "dataset.zip?productTypeId=<product type ID>"
    else if(request.equals("dataset") && map.containsKey("typeID"))
    {
      request = "dataset.zip";
      query = "productTypeId=" + map.get("typeID");
    }

    // Maps "rdf?type=ALL" or "rdf?id=<product type ID>" to
    // "dataset.rdf?productTypeId=<ALL or product type ID>"
    else if (request.equals("rdf"))
    {
      request = "dataset.rdf";
      String type = map.get("type");
      query = "productTypeId=";
      query += "ALL".equals(type) ? type : map.get("id");
    }

    // Maps "rdf/dataset?type=ALL" or "rdf/dataset?typeID=<product type ID>" to
    // "dataset.rdf?productTypeId=<ALL or product type ID>"
    else if (request.equals("rdf/dataset"))
    {
      request = "dataset.rdf";
      String type = map.get("type");
      query = "productTypeId=";
      query += "ALL".equals(type) ? type : map.get("typeID");
      query += map.containsKey("filter") ? "&filter=" + map.get("filter") : "";
    }

    // Maps "viewRecent?channel=ALL" or "viewRecent?id=<product type ID>" to
    // "dataset.rss?productTypeId=<ALL or product type ID>"
    else if (request.equals("viewRecent"))
    {
      request = "dataset.rss";
      String channel = map.get("channel");
      query = "productTypeId=";
      query += "ALL".equals(channel) ? channel : map.get("id");
      query += map.containsKey("topn") ? "&limit=" + map.get("topn") : "";
    }

    // Maps "viewTransfers" to "transfers.rss?productId=ALL"
    else if (request.equals("viewTransfers"))
    {
      request = "transfers.rss";
      query = "productId=ALL";
    }

    // Store the new URI and query in the message map.
    uri = base + request;
    message.put(Message.REQUEST_URI, uri);
    message.put(Message.QUERY_STRING, query);
  }
}
