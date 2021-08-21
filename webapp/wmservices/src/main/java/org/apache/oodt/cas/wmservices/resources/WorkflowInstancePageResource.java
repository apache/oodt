package org.apache.oodt.cas.wmservices.resources;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.apache.oodt.cas.workflow.structs.WorkflowInstance;
import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;

/**
 * A JAX-RS resource representing a {@link WorkflowInstancePage}.
 *
 * @author ngimhana (Nadeeshan Gimhana)
 */
@XmlRootElement(name = "workflowPageInstance")
@XmlType(propOrder = {"pageNum", "totalPages","totalWorkflowCount", "pageSize", "pageWorkflows"})
@XmlAccessorType(XmlAccessType.NONE)
public class WorkflowInstancePageResource {

  private int pageNum;
  private int totalPages;
  private int pageSize;
  private List pageWorkflows;
  private int totalWorkflowCount;

  /** Default constructor required by JAXB. */
  public WorkflowInstancePageResource() {}

  /**
   * Constructor that sets the workflowInstancePage to JAXRS resource.
   *
   * @param workflowInstancePage the workflowInstancePage associated with the resource
   */
  public WorkflowInstancePageResource(WorkflowInstancePage workflowInstancePage,int totalWorkflowCount) {
    this.pageNum = workflowInstancePage.getPageNum();
    this.totalPages = workflowInstancePage.getTotalPages();
    this.pageSize = workflowInstancePage.getPageSize();
    this.pageWorkflows = workflowInstancePage.getPageWorkflows();
    this.totalWorkflowCount = totalWorkflowCount;
  }

  @XmlElement(name = "pageNum")
  public int getPageNum() {
    return pageNum;
  }

  @XmlElement(name = "totalPages")
  public int getTotalPages() {
    return totalPages;
  }

  @XmlElement(name = "totalCount")
  public int getTotalWorkflowCount() {
    return totalWorkflowCount;
  }

  @XmlElement(name = "pageSize")
  public int getPageSize() {
    return pageSize;
  }

  @XmlElement(name = "pageWorkflows")
  public List<WorkflowInstanceResource> getPageWorkflows() {
    List<WorkflowInstance> workflowslist = this.pageWorkflows;
    List<WorkflowInstanceResource> workflowInstanceResourceList = new ArrayList<>();
    for (WorkflowInstance workflowInstance : workflowslist) {
      workflowInstanceResourceList.add(new WorkflowInstanceResource(workflowInstance));
    }
    return workflowInstanceResourceList;
  }
}
