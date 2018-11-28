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


package org.apache.oodt.cas.metadata.extractors;

/**
 * 
 * Met keys for the {@link FilenameTokenMetExtractor}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface FilenameTokenExtractorMetKeys {
  
  public static final String TIME_FORMAT_STRING_SCALAR = "TimeFormatString";
  
  public static final String SUBSTRING_OFFSET_GROUP = "SubstringOffsetGroup";
    
  public static final String TOKEN_LIST_GROUP = "TokenNameListGroup";
  
  public static final String TOKEN_DELIMETER_SCALAR = "Delimeter";

  public static final String TOKEN_MET_KEYS_VECTOR = "TokenMetKeys";

  public static final String PRODUCTION_DATE_TIME_GROUP = "ProductionDateTimeGroup";

  public static final String DATETIME_SCALAR = "DateTimeFormat";

  public static final String COMMON_METADATA_GROUP = "CommonMetadata";  

}
