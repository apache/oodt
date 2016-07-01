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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * A JAX-RS resource representing currently active {@link org.apache.oodt.cas.filemgr.structs.FileTransferStatus
 * file transfers} as {@link TransferResource transfer resources}.
 * @author rlaidlaw
 * @version $Revision$
 */
@XmlRootElement(name = "transfers")
@XmlAccessorType(XmlAccessType.NONE)
public class TransfersResource
{
  private String productId;

  private List<TransferResource> transferResources =
    new ArrayList<TransferResource>();



  /**
   * Default constructor required by JAXB.
   */
  public TransfersResource()
  {
  }



  /**
   * Constructor that sets the product ID and list of TransferResource
   * instances related to the product.
   * @param productId the product ID
   * @param transferResources the resources representing currently active file
   * transfers for the product
   */
  public TransfersResource(String productId,
    List<TransferResource> transferResources)
  {
    this.productId = productId;
    this.transferResources = transferResources;
  }



  /**
   * Gets the product ID.
   * @return the product ID
   */
  @XmlAttribute
  public String getProductId()
  {
    return productId;
  }



  /**
   * Gets the transfer resources.
   * @return the transfer resources
   */
  @XmlElement(name = "transfer")
  public List<TransferResource> getTransferResources()
  {
    return transferResources;
  }
}
