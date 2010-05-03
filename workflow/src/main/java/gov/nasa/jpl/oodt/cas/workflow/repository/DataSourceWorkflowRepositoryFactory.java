//Copyright (c) 2005, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.repository;

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
 * A {@link WorkflowRepositoryFactory} that creates
 * {@DataSourceWorkflowRepository} instances.
 * </p>
 * 
 */
public class DataSourceWorkflowRepositoryFactory implements
        WorkflowRepositoryFactory {

    /* our data source */
    private DataSource dataSource = null;

    /**
     * <p>
     * Default Constructor
     * </p>.
     */
    public DataSourceWorkflowRepositoryFactory() throws Exception {
        String jdbcUrl = null, user = null, pass = null, driver = null;

        jdbcUrl = System
                .getProperty("gov.nasa.jpl.oodt.cas.workflow.repo.datasource.jdbc.url");
        user = System
                .getProperty("gov.nasa.jpl.oodt.cas.workflow.repo.datasource.jdbc.user");
        pass = System
                .getProperty("gov.nasa.jpl.oodt.cas.workflow.repo.datasource.jdbc.pass");
        driver = System
                .getProperty("gov.nasa.jpl.oodt.cas.workflow.repo.datasource.jdbc.driver");

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
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.oodt.cas.workflow.repository.WorkflowRepositoryFactory#createRepository()
     */
    public WorkflowRepository createRepository() {
        return new DataSourceWorkflowRepository(dataSource);
    }

}
