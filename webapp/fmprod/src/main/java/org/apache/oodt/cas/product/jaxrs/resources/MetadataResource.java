/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.apache.oodt.cas.product.jaxrs.resources;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.oodt.cas.metadata.Metadata;

/**
 * A JAX-RS resource representing a {@link Metadata} object.
 * @author rlaidlaw
 * @version $Revision$
 */
@XmlRootElement(name = "metadata")
@XmlAccessorType(XmlAccessType.NONE)
public class MetadataResource
{
  private Metadata metadata;

  /**
   * Default constructor required by JAXB.
   */
  public MetadataResource()
  {
  }



  /**
   * Constructor that sets the metadata for the resource.
   * @param metadata the metadata for the resource
   */
  public MetadataResource(Metadata metadata)
  {
    this.metadata = metadata;
  }



  /**
   * Gets the metadata.
   * @return the metadata
   */
  public Metadata getMetadata()
  {
    return metadata;
  }



  /**
   * Gets a map of metadata keys and values.
   * @return a map of metadata keys and values
   */
  @XmlElement(name = "keyval")
  public List<MetadataEntry> getMetadataEntries()
  {
     List<MetadataEntry> entries = new ArrayList<MetadataEntry>();
     for (String key : metadata.getAllKeys())
     {
       entries.add(new MetadataEntry(key, metadata.getAllMetadata(key)));
     }
     return entries;
  }



  /**
   * Represents a metadata keyval entry.
   * @author rlaidlaw
   * @version $Revision$
   */
  @XmlRootElement
  @XmlType(propOrder = {"key", "values"})
  @XmlAccessorType(XmlAccessType.NONE)
  public static class MetadataEntry
  {
    private String key;
    private List<String> values;



    /**
     * Default constructor required by JAXB.
     */
    public MetadataEntry()
    {
    }



    /**
     * Constructor that creates a metadata entry using the supplied key and list
     * of values.
     * @param key the key for the metadata entry
     * @param values the values for the metadata entry
     */
    public MetadataEntry(String key, List<String> values)
    {
      this.key = key;
      this.values = values;
    }



    /**
     * Gets the key for the metadata entry.
     * @return the key for the metadata entry
     */
    @XmlElement(name = "key")
    public String getKey()
    {
      return key;
    }



    /**
     * Gets the value(s) for the metadata entry
     * @return the value(s) for the metadata entry
     */
    @XmlElement(name = "val")
    public List<String> getValues()
    {
      return values;
    }
  }
}
