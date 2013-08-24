package org.apache.oodt.cas.product.service.servlets;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.util.PathUtils;

/**
 * Provides a single place to initialize items such as the file manager client,
 * working directory and responder configurations when the web application is
 * started up.
 * @author rlaidlaw
 * @version $Revision$
 */
public class CasProductJaxrsServlet extends CXFNonSpringJaxrsServlet
{
  private static final Logger LOGGER = Logger.getLogger(CasProductJaxrsServlet
    .class.getName());

  // Auto-generated ID for serialization.
  private static final long serialVersionUID = -1835790185000773396L;

  // Default URL for the file manager
  private static final String DEFAULT_FM_URL = "http://localhost:9000";

  // Servlet context parameter names.
  private static final String PARAM_NAME_URL = "filemgr.url";
  private static final String PARAM_NAME_WORKINGDIR = "filemgr.working.dir";

  /**
   * The name of the servlet context attribute that holds a client for the file
   * manager, a {@link XmlRpcFileManagerClient} object.
   */
  public static final String ATTR_NAME_CLIENT = "client";

  /**
   * The name of the servlet context attribute that holds the file manager's
   * working directory, a {@link File} object.
   */
  public static final String ATTR_NAME_WORKINGDIR = "workingDir";

  @Override
  public void init(ServletConfig configuration) throws ServletException
  {
    super.init(configuration);
    ServletContext context = configuration.getServletContext();

    // Initialize the file manager client.
    try
    {
      URL url = null;
      String urlParameter = context.getInitParameter(PARAM_NAME_URL);
      if (urlParameter != null)
      {
        // Get the file manager URL from the context parameter.
        url = new URL(PathUtils.replaceEnvVariables(urlParameter));
      }
      else
      {
        // Try the default URL for the file manager.
        LOGGER.log(Level.WARNING, "Unable to find the servlet context parameter"
          + " (\"" + PARAM_NAME_URL + "\") for the file manager's URL.");
        url = new URL(DEFAULT_FM_URL);
      }

      // Attempt to connect the client to the file manager and if successful
      // store the client as a context attribute for other objects to access.
      XmlRpcFileManagerClient client = new XmlRpcFileManagerClient(url);
      context.setAttribute(ATTR_NAME_CLIENT, client);
    }
    catch (MalformedURLException e)
    {
      LOGGER.log(Level.SEVERE,
        "Encountered a malformed URL for the file manager.", e);
      throw new ServletException(e);
    }
    catch (ConnectionException e)
    {
      LOGGER.log(Level.SEVERE, "Client unable to connect to the file manager.",
        e);
      throw new ServletException(e);
    }

    // Initialize the file manager working directory.
    String workingDirPath = context.getInitParameter(PARAM_NAME_WORKINGDIR);
    if (workingDirPath != null)
    {
      // Validate the path.
      File workingDir = new File(PathUtils.replaceEnvVariables(workingDirPath));
      if (workingDir.exists() && workingDir.isDirectory())
      {
        context.setAttribute(ATTR_NAME_WORKINGDIR, workingDir);
      }
      else
      {
        LOGGER.log(Level.SEVERE, "Unable to locate the working directory ("
          + workingDir.getAbsolutePath() + ") for the file manager.");
      }
    }
    else
    {
      String message = "Unable to find the servlet context parameter "
        + "(\"" + PARAM_NAME_WORKINGDIR
        + "\") for the file manager's working directory.";
      LOGGER.log(Level.SEVERE, message);
      throw new ServletException(message);
    }
  }
}
