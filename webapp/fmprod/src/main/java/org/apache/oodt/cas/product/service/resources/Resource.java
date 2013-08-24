package org.apache.oodt.cas.product.service.resources;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.product.service.servlets.CasProductJaxrsServlet;

/**
 * Specifies default behaviour for resource types.
 * @author rlaidlaw
 * @version $Revision$
 */
public abstract class Resource
{
  private static final Logger LOGGER = Logger.getLogger(Resource.class
    .getName());

  // The servlet context, which is used to retrieve context parameters.
  @Context
  private ServletContext context;

  // The file manager's working directory for this resource.
  private File workingDir;

  /**
   * Gets the file manager's working directory for this resource.
   * @return the working directory
   */
  public File getWorkingDir()
  {
    return workingDir;
  }



  /**
   * Sets the file manager's working directory for this resource.
   * @param workingDir the working directory to set
   */
  public void setWorkingDir(File workingDir)
  {
    this.workingDir = workingDir;
  }



  /**
   * Gets the file manager's working directory from the servlet context.
   * @return the file manager working directory
   * @throws Exception if an object cannot be retrieved from the context
   * attribute
   */
  public File getContextWorkingDir() throws Exception
  {
    Object workingDirObject = context
      .getAttribute(CasProductJaxrsServlet.ATTR_NAME_WORKINGDIR);
    if (workingDirObject != null && workingDirObject instanceof File)
    {
      return (File) workingDirObject;
    }

    String message = "Unable to retrieve the file manager's working directory "
      + "from the servlet context.";
    LOGGER.log(Level.WARNING, message);
    throw new Exception(message);
  }



  /**
   * Gets the servlet's file manager client instance from the servlet context.
   * @return the file manager client instance from the servlet context attribute
   * @throws Exception if an object cannot be retrieved from the context
   * attribute
   */
  public XmlRpcFileManagerClient getContextClient()
    throws Exception
  {
    // Get the file manager client from the servlet context.
    Object clientObject = context
      .getAttribute(CasProductJaxrsServlet.ATTR_NAME_CLIENT);
    if (clientObject != null &&
        clientObject instanceof XmlRpcFileManagerClient)
    {
      return (XmlRpcFileManagerClient) clientObject;
    }

    String message = "Unable to retrieve the file manager client from the "
      + "servlet context.";
    LOGGER.log(Level.WARNING, message);
    throw new Exception(message);
  }



  /**
   * Sets the servlet context.
   * @param context the servlet context to set.
   */
  public void setServletContext(ServletContext context)
  {
    this.context = context;
  }
}
