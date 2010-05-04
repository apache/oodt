//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.profile.util;

//CAS imports
import java.util.Iterator;
import java.util.List;

import gov.nasa.jpl.oodt.cas.filemgr.structs.Product;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

//OODT imports
import jpl.eda.profile.EnumeratedProfileElement;
import jpl.eda.profile.Profile;
import jpl.eda.profile.ProfileAttributes;
import jpl.eda.profile.ResourceAttributes;

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
    for(Iterator i = met.getHashtable().keySet().iterator(); i.hasNext(); ){
      String key = (String)i.next();
      List vals = met.getAllMetadata(key);
      
      EnumeratedProfileElement elem = new EnumeratedProfileElement(prof);
      System.out.println("Adding ["+key+"]=>"+vals);
      elem.setName(key);
      elem.getValues().addAll(vals);
      prof.getProfileElements().put(key, elem);
    }
    
    
    return prof;

  }

}
