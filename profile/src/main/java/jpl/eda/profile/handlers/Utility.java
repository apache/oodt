// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: Utility.java,v 1.2 2005/08/01 17:49:06 kelly Exp $

package jpl.eda.profile.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import jpl.eda.profile.Profile;
import jpl.eda.profile.ProfileException;
import jpl.eda.xmlquery.XMLQuery;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;

import org.w3c.dom.Document;
import java.io.ByteArrayInputStream;
import org.xml.sax.SAXParseException;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility methods for profile handlers.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public class Utility {
	/**
	 * Load profile handlers.
	 *
	 * This method consults the system property <code>jpl.eda.profile.handlers</code>
	 * (or <code>handlers</code> if not defined, or
	 * <code>jpl.eda.profile.handler</code> if that's not defined, and finally
	 * <code>handler</code> if that's not defined either) for a comma-separated list
	 * of names of classes that implement the {@link ProfileHandler} interface.  It
	 * instantiates and returns a list of objects of those classes.  If the properties
	 * aren't set, that's an error.
	 *
	 * @return a List of <code>ProfileHandler</code>s.
	 * @throws ProfileException if an error occurs.
	 */
	public static List loadHandlers() throws ProfileException {
		String classNames = System.getProperty("jpl.eda.profile.handlers", System.getProperty("handlers",
                        System.getProperty("jpl.eda.profile.handler", System.getProperty("handler"))));
		if (classNames == null)
			throw new ProfileException("System property \"jpl.eda.profile.handlers\" not defined");
		String className = null;
		try {
			List handlers = new ArrayList();
			for (Iterator i = jpl.eda.util.Utility.parseCommaList(classNames); i.hasNext();) {
				className = (String) i.next();
				Class clazz = Class.forName(className);
				handlers.add(clazz.newInstance());
			}
			return handlers;
		} catch (ClassNotFoundException ex) {
			throw new ProfileException("Profile handler \"" + className + "\" not found", ex);
		} catch (InstantiationException ex) {
			throw new ProfileException("Profile handler \"" + className + "\" is abstract or has no null c'tor", ex);
		} catch (IllegalAccessException ex) {
			throw new ProfileException("Profile handler \"" + className + "\" is not public", ex);
		}
	}

	/**
	 * Concurrently query each of the profile handlers in the given list.
	 *
	 * This method takes a list of {@link ProfileHandler}s and passes the given {@link
	 * XMLQuery} onto each one, all at the same time.  It waits for each one to
	 * finish, and gathers the results of all of them.  Should any one of them fail
	 * with a {@link ProfileException}, the entire query is considred to have failed,
	 * even if there are some successful results.
	 *
	 * @param query Query to pass to each profile handler
	 * @param handlers A {@link List} of {@link ProfileHandler}s. 
	 * @return a {@link List} of matching {@link Profile}s.
	 * @throws ProfileException if any error occurs.
	 */
	public static List findProfiles(XMLQuery query, List handlers) throws ProfileException {
		if (handlers.isEmpty()) return Collections.EMPTY_LIST;		       // No handlers?  Easy!
		List finders = new ArrayList();					       // Start with no profile finders.
		for (Iterator i = handlers.iterator(); i.hasNext();) {		       // For each handler...
			ProfileHandler handler = (ProfileHandler) i.next();	       // ...get the handler...
			Finder finder = new Finder(query, handler);		       // ...and make a corresponding finder.
			finders.add(finder);					       // Track the finder
			finder.start();						       // Start the finder
		}

		ProfileException ex = null;					       // Assume no failure (optimist)
		List results = new ArrayList();					       // Start with no results
		for (Iterator i = finders.iterator(); i.hasNext();) {		       // For each finder...
			Finder finder = (Finder) i.next();			       // ...get the finder...
			for (;;) try {						       // ...and try...
				finder.join();					       // ...to join it to this thread.
				break;						       // If succesful, stop trying
			} catch (InterruptedException ignore) {}		       // Interrupted?  Heck, keep trying.
			ex = finder.getException();                                    // Any exception?  Note it.
                        if (ex == null)                                                // No exception?  Then...
                                results.addAll(finder.getResults());                   // ...add all the matching results.
		}
		if (ex != null)							       // Any exception?
			throw ex;						       // Yes.  Throw it.
		return results;							       // Otherwise, here are your results.
	}

	/**
	 * Get a matching profile by its ID from a list of profile handlers.
	 *
	 * This method asks each profile handler in turn if it has the given profile by
	 * its ID.  The first one to yield a match wins, so to speak.
	 *
	 * @param id ID of the profile to retrieve.
	 * @param handlers A {@link List} of {@link ProfileHandler}s to query.
	 * @return Matching {@link Profile}, or null if no handler has it.
	 * @throws ProfileException if any error occurs.
	 */
	public static Profile getProfile(String id, List handlers) throws ProfileException {
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			ProfileHandler handler = (ProfileHandler) i.next();
			Profile p = handler.get(id);
			if (p != null) return p;
		}
		return null;
	}


	public static void addAll(String profileStr, List handlers) throws ProfileException {
		ProfileHandler handler=null;
		for(Iterator i= handlers.iterator();i.hasNext();)
		{
			handler = (ProfileHandler) i.next();
			if (handler instanceof ProfileManager) break; 
                }
		if(handler !=null) 
			((ProfileManager) handler).addAll(getProfileCollection(profileStr));
		else
                        throw new ProfileException("\nNo propriate server is found to register the profiles.");
	}

	public static void replace(Profile profile, List handlers) throws ProfileException {
		ProfileHandler handler=null;
                for(Iterator i= handlers.iterator();i.hasNext();)
                {
                        handler = (ProfileHandler) i.next();                      
                        if (handler instanceof ProfileManager) break;                 
                }

		if (handlers != null)                                                       
                	((ProfileManager) handler).replace(profile);
		else
                        throw new ProfileException("\nNo propriate server is found to replace the profile.");
        }  

	public static boolean remove(String profId, String version, List handlers) throws ProfileException {
		ProfileHandler handler=null;
                for(Iterator i= handlers.iterator();i.hasNext();)
                {
                        handler = (ProfileHandler) i.next();
                        if (handler instanceof ProfileManager) break;
                }

		if (handlers != null)
			return  ((ProfileManager) handler).remove(profId, version);
		else 
			throw new ProfileException("\nNo propriate server is found to remove the profile.");
        }  

	/**
	 * Return a Collection of {@link Profile} objects from an XML string describing them.
	 *
	 * @param profileStr a <code>String</code> value.
	 * @return a <code>Collection</code> value.
	 * @throws ProfileException if an error occurs.
	 */
	public static Collection getProfileCollection(String profileStr) throws ProfileException {
		try {
			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
			fac.setCoalescing(true);
			fac.setIgnoringComments(true);
			DocumentBuilder builder = fac.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(profileStr.getBytes()));
			return Profile.createProfiles(doc.getDocumentElement());
		} catch (IOException shouldntHappen) {
			throw new IllegalStateException("Unexpected IOException: " + shouldntHappen.getMessage());
		} catch (SAXParseException ex) {
			throw new ProfileException("XML parse error at line " + ex.getLineNumber() + ", column "
				+ ex.getColumnNumber() + ": " + ex.getMessage() + " from source doc " + profileStr);
		} catch (SAXException ex) {
			throw new ProfileException("XML parse error: " + ex.getMessage());
		} catch (ParserConfigurationException ex) {
			throw new IllegalStateException("Unexpected ParserConfigurationException: " + ex.getMessage());
		}
	 }
		
	/**
	 * A Finder is a thread that finds profiles.
	 */
	private static class Finder extends Thread {
		/**
		 * Creates a new {@link Finder} instance.
		 *
		 * @param query a {@link XMLQuery} value.
		 * @param handler a {@link ProfileHandler} value.
		 */
		public Finder(XMLQuery query, ProfileHandler handler) {
			super("Profile finder for " + handler + " working on " + query);
			this.query = (XMLQuery) query.clone();
			this.query.getResults().clear();
			this.handler = handler;
		}

		public void run() {
			try {
				results = handler.findProfiles(query);
			} catch (ProfileException ex) {
				this.ex = ex;
			}
		}

		/**
		 * Return the list of matching results.
		 *
		 * N.B.: this method is only useful <em>after</em> calling
		 * <code>join</code> on this thread.
		 *
		 * @return a {@link List} value.
		 */
		public List getResults() {
			return results;
		}

		/**
		 * Return any exception that might've occurred.
		 *
		 * N.B.: this method is only useful <em>after</em> calling
		 * <code>join</code> on this thread.
		 *
		 * @return a {@link ProfileException} value, or null if no exception occurred.
		 */
		public ProfileException getException() {
			return ex;
		}

		/** Query to pass to our handler. */
		private XMLQuery query;

		/** Our handler. */
		private ProfileHandler handler;

		/** List of matching profiles. */
		private List results;

		/** Exception, if any. */
		private ProfileException ex;
	}
}
