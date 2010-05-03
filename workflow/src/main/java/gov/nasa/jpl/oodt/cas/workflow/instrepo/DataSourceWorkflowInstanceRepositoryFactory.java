//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.instrepo;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;
import gov.nasa.jpl.oodt.cas.workflow.instrepo.WorkflowInstanceRepository;
import gov.nasa.jpl.oodt.cas.workflow.instrepo.WorkflowInstanceRepositoryFactory;

//APACHE imports
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

//JDK imports
import javax.sql.DataSource;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A factory for creating {@link DataSource} based
 * {@link WorkflowInstanceRepository}s.
 * </p>
 * 
 */
public class DataSourceWorkflowInstanceRepositoryFactory implements
        WorkflowInstanceRepositoryFactory {

    /* our data source */
    private DataSource dataSource = null;

    /* whether or not we are quoting the task_id and workflow_id fields */
    private boolean quoteFields = false;

    /* the number of workflow instances returned per page when paginating */
    private int pageSize = -1;

    /**
     * <p>
     * Default constructor
     * </p>
     */
    public DataSourceWorkflowInstanceRepositoryFactory() throws Exception {
        String jdbcUrl = null, user = null, pass = null, driver = null;

        jdbcUrl = PathUtils
                .replaceEnvVariables(System
                        .getProperty("gov.nasa.jpl.oodt.cas.workflow.instanceRep.datasource.jdbc.url"));
        user = PathUtils
                .replaceEnvVariables(System
                        .getProperty("gov.nasa.jpl.oodt.cas.workflow.instanceRep.datasource.jdbc.user"));
        pass = PathUtils
                .replaceEnvVariables(System
                        .getProperty("gov.nasa.jpl.oodt.cas.workflow.instanceRep.datasource.jdbc.pass"));
        driver = PathUtils
                .replaceEnvVariables(System
                        .getProperty("gov.nasa.jpl.oodt.cas.workflow.instanceRep.datasource.jdbc.driver"));

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new Exception("Cannot load driver: " + driver);
        }

        GenericObjectPool connectionPool = new GenericObjectPool(null);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
                jdbcUrl, user, pass);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
                connectionFactory, connectionPool, null, null, false, true);

        dataSource = new PoolingDataSource(connectionPool);
        quoteFields = Boolean
                .getBoolean("gov.nasa.jpl.oodt.cas.workflow.instanceRep.datasource.quoteFields");
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
        return new DataSourceWorkflowInstanceRepository(dataSource,
                quoteFields, pageSize);
    }

}
