//Copyright (c) 2010, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.product.rss;

//JDK imports
import java.util.List;
import java.util.Vector;

/**
 * 
 * Configures the {@link RSSProductServlet}, with the information
 * defined in an rssconf.xml file.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class RSSConfig {

  private List<RSSTag> tags;
  
  private String channelLink;

  /**
   * Default constructor.
   */
  public RSSConfig() {
    this.channelLink = null;
    this.tags = new Vector<RSSTag>();
  }

  /**
   * @return the tags
   */
  public List<RSSTag> getTags() {
    return tags;
  }

  /**
   * @param tags
   *          the tags to set
   */
  public void setTags(List<RSSTag> tags) {
    this.tags = tags;
  }

  /**
   * @return the channelLink
   */
  public String getChannelLink() {
    return channelLink;
  }

  /**
   * @param channelLink the channelLink to set
   */
  public void setChannelLink(String channelLink) {
    this.channelLink = channelLink;
  }

}
