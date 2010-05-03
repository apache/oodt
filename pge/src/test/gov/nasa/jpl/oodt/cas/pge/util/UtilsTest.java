package gov.nasa.jpl.oodt.cas.pge.util;

import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.CatalogException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import gov.nasa.jpl.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;

import java.net.MalformedURLException;

import junit.framework.TestCase;

public class UtilsTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testEvalSQL() throws MalformedURLException, ConnectionException, CatalogException, RepositoryManagerException, QueryFormulationException {
		//Utils.evalSQL("SQL { SELECT Filename FROM IASI_L1C WHERE DataVersion >= '23.4' AND DataVersion <= '24.4' AND DataVersion != '24.0' OR Filename != 'SomeName' }");
	}

}
