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


package org.apache.oodt.cas.metadata.util;

//OODT imports
import org.apache.oodt.cas.metadata.MetExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A generic object factory for Metadata framework objects.
 * </p>.
 */
public final class GenericMetadataObjectFactory {

  /* our log stream */
  private final static Logger LOG = LoggerFactory.getLogger(GenericMetadataObjectFactory.class);

  public static MetExtractor getMetExtractorFromClassName(String className) {
    Class metExtractorClass;
    MetExtractor extractor;

    try {
      metExtractorClass = Class.forName(className);
      extractor = (MetExtractor) metExtractorClass.newInstance();
      return extractor;
    } catch (ClassNotFoundException e) {
      LOG.warn("ClassNotFoundException when loading met extractor class [{}]: {}", className, e.getMessage(), e);
    } catch (InstantiationException e) {
      LOG.warn("InstantiationException when loading met extractor class [{}]: {}", className, e.getMessage(), e);
    } catch (IllegalAccessException e) {
      LOG.warn("IllegalAccessException when loading met extractor class [{}]: {}", className, e.getMessage(), e);
    }

    return null;
  }

}
