package org.apache.oodt.cas.workflow.system;

import org.apache.oodt.cas.workflow.structs.exceptions.EngineException;
import org.apache.oodt.cas.workflow.structs.exceptions.InstanceRepositoryException;
import org.apache.oodt.cas.workflow.structs.exceptions.RepositoryException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author radu
 *
 *  Workflow manager interface, used for RPC implementatino.
 */
public interface WorkflowManager{

    public static final String PROPERTIES_FILE_PROPERTY = "org.apache.oodt.cas.workflow.properties";
    public static final String WORKFLOW_ENGINE_FACTORY_PROPERTY = "workflow.engine.factory";
    public static final String ENGINE_RUNNER_FACTORY_PROPERTY = "workflow.engine.runner.factory";
    public static final String WORKFLOW_REPOSITORY_FACTORY_PROPERTY = "workflow.repo.factory";
    public static final int DEFAULT_WEB_SERVER_PORT = 9001;

    /**
     *
     * @return shutdown was successful.
     */
    boolean shutdown();

}
