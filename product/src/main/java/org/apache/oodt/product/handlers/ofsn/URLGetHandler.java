/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oodt.product.handlers.ofsn;

//JDK imports
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.product.ProductException;
import org.apache.oodt.product.handlers.ofsn.AbstractCrawlLister;
import org.apache.oodt.product.handlers.ofsn.OFSNGetHandler;

/**
  * A {@link OFSNGetHandler} for returning a URL listing pointing to files within an OFSN
 * 
 * NOTE: Configuration parameters for this class include:
 * 1. Server hostname
 * 2. Server port
 * 3. Webapp context-root
 * 4. Path to product-root
 * 5. Return type desired for URLs
 * 
 * @author rverma
 * @version $Revision$
 *
 */
public class URLGetHandler extends AbstractCrawlLister implements OFSNGetHandler {

	Logger LOG = Logger.getLogger(URLGetHandler.class.getName());
	
	// Constants
	private static final String PROD_SERVER_HOSTNAME = "prodServerHostname";
	private static final String PROD_SERVER_PORT = "prodServerPort";
	private static final String PROD_SERVER_CONTEXT = "prodServerContextRoot";
	private static final String PRODUCT_ROOT = "productRoot";
	private static final String RETURN_TYPE = "returnType";
	
	protected static final String DEFAULT_RETURN_VALUE="";
	protected static final String DEFAULT_PROD_SERVER_HOSTNAME = "localhost";
	protected static final String DEFAULT_PROD_SERVER_PORT = "8080";
	protected static final String DEFAULT_PROD_SERVER_CONTEXT = "web-grid";
	protected static final String DEFAULT_PRODUCT_ROOT = "/some/path";
	protected static final String DEFAULT_RETURN_TYPE = "RAW";
	
	// Instance
	private String prodServerHostname = "";
	private String prodServerPort = "";
	private String prodServerContext = "";
	private String productRoot = "";
	private String returnType = "";
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.product.handlers.ofsn.AbstractCrawlLister#configure(java.util.Properties)
	 */
	public void configure(Properties prop) {
		
		if (prop != null) {
			if (prop.getProperty(PROD_SERVER_HOSTNAME) != null) {
			  this.prodServerHostname = prop.getProperty(PROD_SERVER_HOSTNAME);
			} else {
				LOG.warning("Configuration property ["+PROD_SERVER_HOSTNAME+"] not specified, using default");
				this.prodServerHostname = DEFAULT_PROD_SERVER_HOSTNAME;
			}
			LOG.info("Property ["+PROD_SERVER_HOSTNAME+"] set with value ["+this.prodServerHostname+"]");
			
			if (prop.getProperty(PROD_SERVER_PORT) != null) {
			  this.prodServerPort = prop.getProperty(PROD_SERVER_PORT);
			} else {
				LOG.warning("Configuration property ["+PROD_SERVER_PORT+"] not specified, using default");
				this.prodServerPort = DEFAULT_PROD_SERVER_PORT;
			}
			LOG.info("Property ["+PROD_SERVER_PORT+"] set with value ["+this.prodServerPort+"]");
			
			if (prop.getProperty(PROD_SERVER_CONTEXT) != null) {
			  this.prodServerContext = prop.getProperty(PROD_SERVER_CONTEXT);
			} else {
				LOG.warning("Configuration property ["+PROD_SERVER_CONTEXT+"] not specified, using default");
				this.prodServerContext = DEFAULT_PROD_SERVER_CONTEXT;
			}
			LOG.info("Property ["+PROD_SERVER_CONTEXT+"] set with value ["+this.prodServerContext+"]");
			
			if (prop.getProperty(PRODUCT_ROOT) != null) {
			  this.productRoot = prop.getProperty(PRODUCT_ROOT);
			} else {
				LOG.warning("Configuration property ["+PRODUCT_ROOT+"] not specified, using default");
				this.productRoot = DEFAULT_PRODUCT_ROOT;
			}
			LOG.info("Property ["+PRODUCT_ROOT+"] set with value ["+this.productRoot+"]");
			
			if (prop.getProperty(RETURN_TYPE) != null) {
			  this.returnType = prop.getProperty(RETURN_TYPE);
			} else {
				LOG.warning("Configuration property ["+RETURN_TYPE+"] not specified, using default");
				this.returnType = DEFAULT_RETURN_TYPE;
			}
			LOG.info("Property ["+RETURN_TYPE+"] set with value ["+this.returnType+"]");
			
		} else {
			LOG.warning("Configuration properties could not be loaded");
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.product.handlers.ofsn.OFSNGetHandler#retrieveChunk(java.lang.String, long, int)
	 */
	public byte[] retrieveChunk(String filepath, long offset, int length)
			throws ProductException {
	  
		LOG.info("Retrieving chunk of URL listing for path: ["+filepath+"] at offset "
	    			+ offset+" for "+length+" bytes");

		String urlListing = DEFAULT_RETURN_VALUE;
		try {
			urlListing = getURLListing(filepath);
		} catch (ProductException e) {
			LOG.warning("Unable to obtain byte chunk ("+offset+" - "+(offset+length)+") " 
					+ "for filepath listing ["+filepath+"]");
			LOG.warning(e.getMessage());
		} catch (IllegalArgumentException e) {
			LOG.warning("Unable to obtain byte chunk ("+offset+" - "+(offset+length)+") " 
					+ "for filepath listing ["+filepath+"]");
			LOG.warning(e.getMessage());
		}
		
        // Convert listing to bytes
        byte[] retBytes = new byte[length];
        byte[] metBytes = urlListing.getBytes();      
        ByteArrayInputStream is = new ByteArrayInputStream(metBytes);
        is.skip(offset);
        is.read(retBytes, 0, length);
        
        return retBytes;
	}

	/**
	 * Returns a new-line separated list of URLs for all files located under the given filepath
	 * 
	 * @param filePath the absolute path to a root-directory to get a product listing for
	 * @return a string containing a new-line separated list of URLs
	 * @throws ProductException
	 */
	private String getURLListing(String filePath) throws ProductException, 
			IllegalArgumentException {     
		
		// crawl and collect all files (including within subdirs) under filepath
		File[] fileListing = getListing(filePath);
		 
		// convert each crawled file's path into an OFSN download link
		StringBuilder stringBuilder = new StringBuilder();
	  for (File aFileListing : fileListing) {
		File file = (File) aFileListing;
		stringBuilder.append(buildOFSNURL(file).toString());
		stringBuilder.append("\n");
	  }
		
    	return stringBuilder.toString();
	}
	
	/**
	 * Returns a URL object representing the URL associated with this particular product file.
	 * 
	 * The URL is defined to have items such as: hostname, server port, server context root,
	 * and return type configured based upon a product server configuration file
	 * 
	 * @param file the product file
	 * @return a URL
	 */
	private URL buildOFSNURL(File file) {
		URL url = null;
		
		String fileRelativePath = file.getAbsolutePath().substring(this.productRoot.length());
		
		// construct a URL for the file, optionally using a port if available
		String ofsnPath;
		if (this.prodServerPort != null) {
			if (!this.prodServerPort.isEmpty()) {
				ofsnPath = "http://" + this.prodServerHostname 
				+ ":" + this.prodServerPort + "/" + this.prodServerContext 
				+ "/prod?q=OFSN=" + fileRelativePath + "+AND+RT%3D" + this.returnType;
			} else {
				ofsnPath = "http://" + this.prodServerHostname 
				+ "/" + this.prodServerContext + "/prod?q=OFSN=" + fileRelativePath 
				+ "+AND+RT%3D" + this.returnType;
			}
		} else {
			ofsnPath = "http://" + this.prodServerHostname 
			+ "/" + this.prodServerContext + "/prod?q=OFSN=" + fileRelativePath 
			+ "+AND+RT%3D" + this.returnType;
		}
		
		try {
			url = new URL(ofsnPath);
		} catch (MalformedURLException e) {
			LOG.warning(e.getMessage());
		}
		
		return url;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.product.handlers.ofsn.OFSNGetHandler#sizeOf(java.lang.String)
	 */
	public long sizeOf(String filepath) {
		String urlListing = DEFAULT_RETURN_VALUE;
		try {
			urlListing = getURLListing(filepath);
		} catch (ProductException e) {
			LOG.warning("Unable to obtain size information for filepath listing ["+filepath+"]");
			LOG.warning(e.getMessage());
		} catch (IllegalArgumentException e) {
			LOG.warning("Unable to obtain size information for filepath listing ["+filepath+"]");
			LOG.warning(e.getMessage());
		}
		
		return urlListing.getBytes().length;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.oodt.product.handlers.ofsn.AbstractCrawlLister#getListing(java.lang.String)
	 */
	public File[] getListing(String filePath) throws ProductException, 
			IllegalArgumentException {
		return crawlFiles(new File(filePath), true, false);
	} 

}