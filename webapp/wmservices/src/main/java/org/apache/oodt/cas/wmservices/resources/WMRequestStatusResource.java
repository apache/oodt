package org.apache.oodt.cas.wmservices.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A JAX-RS resource representing a WMRequestStatus from Workflow manager
 *
 * @author ngimhana (Nadeeshan Gimhana)
 */
@XmlRootElement(name = "WMStatus")
@XmlType(propOrder = {"url", "message"})
@XmlAccessorType(XmlAccessType.NONE)
public class WMRequestStatusResource {

  private String url;
  private String message;

  public WMRequestStatusResource() {}

  public WMRequestStatusResource(String url, String message) {
    this.url = url;
    this.message = message;
  }

  @XmlElement(name = "url")
  public String getUrl() {
    return url;
  }

  @XmlElement(name = "message")
  public String getMessage() {
    return message;
  }
}
