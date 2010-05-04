//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.product.rdf;

/**
 * 
 * Met keys for the {@link RDFConfigReader}.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public interface RDFConfigReaderMetKeys {

  public static final String NS_OUTER_TAG = "namespaces";

  public static final String NS_TAG = "ns";

  public static final String NS_NAME_ATTR = "name";

  public static final String NS_VALUE_ATTR = "value";

  public static final String REWRITE_OUTER_TAG = "rewrite";

  public static final String REWRITE_KEY_TAG = "key";

  public static final String REWRITE_FROM_ATTR = "from";

  public static final String REWRITE_TO_ATTR = "to";

  public static final String RESOURCE_LINK_TAG = "resourcelinks";

  public static final String RESLINK_KEY_TAG = "key";

  public static final String RESLINK_KEY_TAG_NAME_ATTR = "name";

  public static final String RESLINK_KEY_TAG_LINK_ATTR = "link";

  public static final String KEY_NSMAP_TAG = "keynsmap";

  public static final String KEY_NSMAP_DEFAULT_ATTR = "default";

  public static final String KEY_NSMAP_KEY_TAG = "key";

  public static final String KEY_NSMAP_KEY_TAG_NAME_ATTR = "name";

  public static final String KEY_NSMAP_KEY_TAG_NS_ATTR = "ns";

  public static final String TYPE_NSMAP_TAG = "typesnsmap";

  public static final String TYPE_NSMAP_DEFAULT_ATTR = "default";

  public static final String TYPE_NSMAP_TYPE_TAG = "type";

  public static final String TYPE_NSMAP_TYPE_NAME_ATTR = "name";

  public static final String TYPE_NSMAP_TYPE_NS_ATTR = "ns";
  
}
