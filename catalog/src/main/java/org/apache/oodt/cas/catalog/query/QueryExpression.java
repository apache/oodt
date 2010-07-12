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

package org.apache.oodt.cas.catalog.query;

//JDK imports
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * A Expression for querying against a CatalogServices Metadata
 * 
 */
public abstract class QueryExpression implements Cloneable {

  protected Set<String> bucketNames;

  public QueryExpression() {
  }

  public QueryExpression(Set<String> bucketNames) {
    this.bucketNames = new HashSet<String>(bucketNames);
  }

  public Set<String> getBucketNames() {
    return (this.bucketNames != null) ? new HashSet<String>(this.bucketNames)
        : null;
  }

  public void setBucketNames(Set<String> bucketNames) {
    this.bucketNames = bucketNames;
  }

  public abstract String toString();

  public abstract QueryExpression clone();

}
