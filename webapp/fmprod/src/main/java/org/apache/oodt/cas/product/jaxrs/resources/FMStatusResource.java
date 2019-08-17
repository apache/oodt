package org.apache.oodt.cas.product.jaxrs.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="FMStatus")
@XmlType(propOrder = {"url","serverUp","message"})
@XmlAccessorType(XmlAccessType.NONE)
public class FMStatusResource {

  private String url;
  private boolean serverUp;
  private String message;

  public FMStatusResource(){}

  public FMStatusResource(String url, boolean serverUp,String message){
    this.url = url;
    this.serverUp = serverUp;
    this.message = message;
  }

  @XmlElement(name="url")
  public String getUrl() {
    return url;
  }

  @XmlElement(name="serverUp")
  public boolean isServerUp() {
    return serverUp;
  }

  @XmlElement(name="message")
  public String getMessage() {
    return message;
  }
}
