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


package org.apache.oodt.profile.handlers.cas.util;

//CAS imports
import java.util.Iterator;
import java.util.List;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;

//OODT imports
import org.apache.oodt.profile.EnumeratedProfileElement;
import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.ProfileAttributes;
import org.apache.oodt.profile.ResourceAttributes;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A set of utility methods to aid in CAS Filemgr vocabulary conversion to OODT
 * {@link Profile} vocabulary.
 * </p>
 */
public final class ProfileUtils {

  private static final String PROF_ID_PRE = "urn:oodt:profile:";

  private static final String PROF_TYPE = "profile";

  private ProfileUtils() throws InstantiationException {
    throw new InstantiationException("Don't construct utility classes!");
  }

  /**
   * Builds a {@link Profile} from the given {@link Product}, and its
   * {@link Metadata}.
   * 
   * @param p
   *          The given {@link Product}.
   * @param met
   *          The given {@link Metadata}.
   * @param dataDelivBaseUrl
   *          A string representation of a {@link URL} to the data delivery
   *          service for this particular {@link Product}. This is used, in
   *          combination with the {@link Product}'s ID, to populate the
   *          {@link ResourceAttributes} <code>resLocation</code> field.
   * @return An OODT {@link Profile} representation pointing at the given
   *         {@link Product}.
   */
  public static Profile buildProfile(Product p, Metadata met,
      String dataDelivBaseUrl) {

    Profile prof = new Profile();

    ProfileAttributes profAttrs = new ProfileAttributes();
    profAttrs.setID(PROF_ID_PRE + p.getProductId());
    profAttrs.setRegAuthority("CAS");
    profAttrs.setType(PROF_TYPE);

    prof.setProfileAttributes(profAttrs);

    ResourceAttributes resAttrs = new ResourceAttributes();
    resAttrs.setDescription(p.getProductType().getDescription());
    resAttrs.setIdentifier(p.getProductId());
    resAttrs.setResClass(p.getProductType().getName());
    resAttrs.setTitle(p.getProductName());
    resAttrs.getPublishers().add("CAS");
    resAttrs.getResLocations().add(
        dataDelivBaseUrl + "?productID=" + p.getProductId());

    prof.setResourceAttributes(resAttrs);

    // build up profile elements
    for (String key : met.getHashtable().keySet()) {
      List vals = met.getAllMetadata(key);

      EnumeratedProfileElement elem = new EnumeratedProfileElement(prof);
      System.out.println("Adding [" + key + "]=>" + vals);
      elem.setName(key);
      elem.getValues().addAll(vals);
      prof.getProfileElements().put(key, elem);
    }
    
    
    return prof;

  }

}
