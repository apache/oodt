/**
 * 
 */
package org.apache.oodt.cas.curation.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.oodt.cas.curation.configuration.Configuration;


/**
 * Handles basic servlet features like config
 * 
 * @author starchmd
 */
public class CuratorServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1498427942585673418L;

    @Override
    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);
        //Load configuration from context
        Configuration.loadConfiguration(conf.getServletContext());
    }
}
