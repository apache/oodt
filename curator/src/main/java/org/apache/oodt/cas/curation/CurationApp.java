package org.apache.oodt.cas.curation;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

public class CurationApp extends WebApplication {

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