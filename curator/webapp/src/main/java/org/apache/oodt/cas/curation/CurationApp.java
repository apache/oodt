package org.apache.oodt.cas.curation;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.curation.workbench.FileTree;
import org.apache.wicket.Page;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.file.File;

public class CurationApp extends WebApplication {
  
  private static final Logger LOG = Logger.getLogger(CurationApp.class.getName());

	/* (non-Javadoc)
   * @see org.apache.wicket.protocol.http.WebApplication#init()
   */
  @Override
  protected void init() {
    super.init();
    Set<String> resources = FileTree.getImageFiles();
    if (resources != null){
      for (String resource: resources){
        String resName = new File(resource).getName();
        String resPath = "/images/"+resName;
        LOG.log(Level.INFO, "Mounting: ["+resPath+"]");
        mountSharedResource(resPath,
            new ResourceReference(FileTree.class,
                resName).getSharedResourceKey());
      }
    }
  }

  @Override
	public Class<? extends Page> getHomePage() {
		try {
			return (Class<? extends Page>) Class.forName(getHomePageClass());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return HomePage.class;
		}
	}

	public String getHomePageClass() {
		return getServletContext().getInitParameter("curator.homepage");
	}

}