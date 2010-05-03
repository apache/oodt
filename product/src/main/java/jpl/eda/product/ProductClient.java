// Copyright 2002-2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProductClient.java,v 1.5 2005-06-22 21:41:09 kelly Exp $

package jpl.eda.product;

import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import javax.naming.Context;
import javax.naming.NamingException;
import jpl.eda.Configuration;
import jpl.eda.object.jndi.RMIContext;
import jpl.eda.product.corba.ProductServiceAdaptor;
import jpl.eda.xmlquery.Result;
import jpl.eda.xmlquery.XMLQuery;
import jpl.oodt.product.rmi.OldProductServiceAdaptor;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.util.ArrayList;
import jpl.eda.product.test.TestRunner;
import jpl.eda.product.test.TestConfig;

/**
 * Client access to a product service.
 *
 * @author Kelly
 * @version $Revision: 1.5 $
 */
public class ProductClient implements Retriever {
	/**
	 * Initialize this class by running any enterprise initializers.
	 */
	static {
		try {
			jpl.eda.ExecServer.runInitializers();
		} catch (jpl.eda.EDAException ex) {
			System.err.println("\bFatal error:"); 
			ex.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Creates a new <code>ProductClient</code> instance.
	 *
	 * @param serviceID Name of the product server to contact.
	 * @throws ProductException if an error occurs.
	 */
	public ProductClient(String serviceID) throws ProductException {
		try {
			 getProductService(Configuration.getConfiguration().getObjectContext(), serviceID);
  		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ProductException(ex);
		}
	}


	private void getProductService(Context context,String serviceID) throws ProductException{
		try {
			Object result = context.lookup(serviceID);
			if (result instanceof ProductService)
				productService = (ProductService) result;
			else if (result instanceof jpl.oodt.product.ProductService)
				productService = new OldProductServiceAdaptor((jpl.oodt.product.ProductService) result);
			else if (result instanceof URL)
				productService = new HTTPAdaptor((URL) result);
			else // assume corba (which sucks)
				productService = new ProductServiceAdaptor((jpl.eda.product.corba.ProductService) result);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ProductException(ex);
		}
	}


	 /**
	  * Creates a new <code>ProductClient</code> instance, given an Object Context.
	  *
	  * @param context Object context of the remote registry.
	  * @param serviceID Name of the product server to contact.
	  * @throws ProductException if an error occurs.
	  */
	public ProductClient(Context context, String serviceID) throws ProductException {
		try {
			getProductService(context,serviceID);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ProductException(ex);
		}

	}

	/**
	 * Query the product server.
	 *
	 * @param q Query
	 * @return Response.
	 * @throws ProductException if an error occurs.
	 */
	public XMLQuery query(XMLQuery q) throws ProductException {
		try {
			Server server = productService.createServer();
			XMLQuery response = server.query(q);
			response.setRetriever(this);
			return response;
		} catch (RemoteException ex) {
			throw new ProductException(ex);
		}
	}

	/**
	 * Retrieve a chunk from a large product.
	 *
	 * @param productID Product ID.
	 * @param offset Where in the product to retrieve the data.
	 * @param length How much data to get.
	 * @return The data.
	 * @throws ProductException if an error occurs.
	 */
	public byte[] retrieveChunk(String productID, long offset, int length) throws ProductException {
		try {
			Server server = productService.createServer();
			return server.retrieveChunk(productID, offset, length);
		} catch (RemoteException ex) {
			throw new ProductException(ex);
		}
	}

	/**
	 * Close off a large product.
	 *
	 * @param productID Product ID.
	 * @throws ProductException if an error occurs.
	 */
	public void close(String productID) throws ProductException {
		try {
			Server server = productService.createServer();
			server.close(productID);
		} catch (RemoteException ex) {
			throw new ProductException(ex);
		}
	}

	/** Product service we're using. */
	private ProductService productService;

	private static void test(File file) throws Throwable {
		TestRunner.runQueries(TestConfig.parseQueries(file));
	}

	/**
	 * Command-line exerciser.
	 *
	 * @param argv Command-line arguments, of which there should be two: service ID and query expression.
	 * @throws Throwable if an error occurs.
	 */
	public static void main(String[] argv) throws Throwable {
		if (argv.length < 2 || argv.length > 5)
			showUsage();
		boolean outputProduct = false;
		boolean testMode = false;
		if ("-out".equals(argv[0]))
			outputProduct = true;
		else if ("-xml".equals(argv[0]))
			outputProduct = false;
		else if ("-test".equals(argv[0]))
			testMode = true;
		else {
			System.err.println("Please specify either -out or -xml");
			System.exit(1);
		}

		if (testMode) {
			if (argv.length != 2) showUsage();
			test(new File(argv[1]));
			System.exit(0);
		}

		XMLQuery q = new XMLQuery(argv[2], null, null, null, null, null, null, null, 100);
		ProductClient c = null;
		if (argv.length == 3)
			c = new ProductClient(argv[1]);
		else if (argv.length == 5)
			c = new ProductClient(new RMIContext(argv[3], Integer.parseInt(argv[4])),argv[1]);
		else 
			showUsage();


		XMLQuery r = c.query(q);
		
		if (outputProduct) {
			List results = r.getResults();
			if (results.isEmpty())
				System.err.println("No matching results");
			else {
				Result result = (Result) results.get(0);
				InputStream in = result.getInputStream();
				byte[] buf = new byte[512];
				int numRead;
				while ((numRead = in.read(buf)) != -1)
					System.out.write(buf, 0, numRead);
				in.close();
				System.out.close();
			}
		} else
			System.out.println(r.getXMLDocString());

		System.exit(0);
	}

	/*
	* Show the command line usage
	*/
	private static void showUsage() {
		System.err.println("Usage: {-out|-xml} serviceID query-expression [host] [port]");
                System.err.println("-out writes the first result of the query to the standard output");
                System.err.println("-xml shows the XMLQuery response only");
		System.err.println("-test <file> runs product server stress test using the given config <file>");
		System.err.println("[host]--the hostname of the Remote RMI Registry");
		System.err.println("[port]--the port of the Remote RMI Registry");
                System.exit(1);
	}

	      
}
