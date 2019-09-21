package org.apache.oodt.cas.wmservices.resources;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowLifecycleStage;
import org.apache.oodt.cas.workflow.lifecycle.WorkflowState;

/**
 * A JAX-RS resource representing a {@link WorkflowState}.
 *
 * @author ngimhana (Nadeeshan Gimhana)
 */
@XmlRootElement(name = "workflowState")
@XmlType(propOrder = {"name", "description", "message", "startTime", "category", "prevState"})
@XmlAccessorType(XmlAccessType.NONE)
public class WorkflowStateResource {

  private String name;
  private String description;
  private String message;
  private Date startTime;
  private WorkflowLifecycleStage category;
  private WorkflowState prevState;

  public WorkflowStateResource() {}

  /**
   * Constructor that sets the workflowState to JAXRS resource.
   *
   * @param workflowState the workflowState associated with the resource
   */
  public WorkflowStateResource(WorkflowState workflowState) {
    this.name = workflowState.getName();
    this.description = workflowState.getDescription();
    this.message = workflowState.getMessage();
    this.startTime = workflowState.getStartTime();
    this.category = workflowState.getCategory();
    this.prevState = workflowState.getPrevState();
  }

  @XmlElement(name = "name")
  public String getName() {
    return name;
  }

  @XmlElement(name = "description")
  public String getDescription() {
    return description;
  }

  @XmlElement(name = "message")
  public String getMessage() {
    return message;
  }

  @XmlElement(name = "startTime")
  public Date getStartTime() {
    return startTime;
  }

  @XmlElement(name = "category")
  public WorkflowLifecycleStage getCategory() {
    return category;
  }

  @XmlElement(name = "prevState")
  public WorkflowState getPrevState() {
    return prevState;
  }
}
