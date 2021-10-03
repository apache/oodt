package org.apache.oodt.cas.wmservices.resources;

import org.apache.oodt.cas.workflow.structs.WorkflowInstancePage;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * A JAX-RS resource representing a list of workflow events {@link List <String>}.
 *
 * @author pavinduLakshan (Pavindu Lakshan)
 */
@XmlRootElement(name = "workflowEvents")
@XmlType(propOrder = {"eventList"})
@XmlAccessorType(XmlAccessType.NONE)
public class WorkflowEventListResource {
    private List<String> eventList;

    public WorkflowEventListResource(){

    }

    public WorkflowEventListResource(List<String> regEvents){
        this.eventList = regEvents;
    }

    @XmlElement(name = "events")
    public List<String> getEventList() {
        return this.eventList;
    }
}
