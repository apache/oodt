//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.instrepo;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Factory class for creating {@link LuceneWorkflowEngine}s.
 * </p>.
 */
public class LuceneWorkflowInstanceRepositoryFactory implements
        WorkflowInstanceRepositoryFactory {

    /* the path to the lucene index directory */
    private String indexFilePath = null;

    private int pageSize = -1;

    public LuceneWorkflowInstanceRepositoryFactory()
            throws InstantiationException {
        indexFilePath = System
                .getProperty("gov.nasa.jpl.oodt.cas.workflow.instanceRep.lucene.idxPath");

        if (indexFilePath == null) {
            throw new InstantiationException(
                    "Index File property: [gov.nasa.jpl.oodt.cas.workflow."
                            + "engine.lucene.idxPath] not set!");
        }

        // do env variable replacement
        indexFilePath = PathUtils.replaceEnvVariables(indexFilePath);
        pageSize = Integer.getInteger(
                "gov.nasa.jpl.oodt.cas.workflow.instanceRep.pageSize", 20)
                .intValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.workflow.instrepo.WorkflowInstanceRepositoryFactory#createInstanceRepository()
     */
    public WorkflowInstanceRepository createInstanceRepository() {
        return new LuceneWorkflowInstanceRepository(indexFilePath, pageSize);
    }

}
