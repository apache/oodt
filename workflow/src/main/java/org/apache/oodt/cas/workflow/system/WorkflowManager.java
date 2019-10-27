package org.apache.oodt.cas.workflow.system;

/**
 * @author radu
 *
 *  Workflow manager interface, used for RPC implementatino.
 */
public interface WorkflowManager {

    String PROPERTIES_FILE_PROPERTY = "org.apache.oodt.cas.workflow.properties";
    String DEFAULT_PROPERTIES_FILE = "workflow.properties";
    
    String WORKFLOW_ENGINE_FACTORY_PROPERTY = "workflow.engine.factory";
    String WORKFLOW_SERVER_FACTORY_PROPERTY = "workflow.server.factory";
    String WORKFLOW_CLIENT_FACTORY_PROPERTY = "workflow.client.factory";
    
    String ENGINE_RUNNER_FACTORY_PROPERTY = "workflow.engine.runner.factory";
    String WORKFLOW_REPOSITORY_FACTORY_PROPERTY = "workflow.repo.factory";
    int DEFAULT_WEB_SERVER_PORT = 9001;

    /**
     *
     * @return shutdown was successful.
     */
    boolean shutdown();

}
