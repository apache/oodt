// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProfileClient.java,v 1.2 2005/06/10 21:16:04 kelly Exp $

package jpl.eda.profile;

import java.rmi.RemoteException;
import java.util.List;
import javax.naming.Context;
import jpl.eda.Configuration;
import jpl.eda.profile.corba.ProfileServiceAdaptor;
import jpl.eda.profile.corba.ServerPackage.*;
import jpl.eda.xmlquery.XMLQuery;
import java.net.URL;

/**
 * Client to access a profile server.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public class ProfileClient {
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
	 * Creates a new <code>ProfileClient</code> instance.
	 *
	 * @param serviceID URI of the profile server to query.
	 * @throws ProfileException if an error occurs.
	 */
	public ProfileClient(String serviceID) throws ProfileException {
		try {
			Configuration configuration = Configuration.getConfiguration();
			Context ctx = configuration.getObjectContext();
			Object result = ctx.lookup(serviceID);
			if (result instanceof ProfileService)
				profileService = (ProfileService) result;
			else if (result instanceof URL)
				profileService = new HTTPAdaptor((URL) result);
			else // it's corba, which sucks
				profileService = new ProfileServiceAdaptor((jpl.eda.profile.corba.ProfileService) result);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new ProfileException(ex);
		}
	}

	/**
	 * Query the server for profiles.
	 *
	 * @param q The query.
	 * @return A list of matching {@link Profile}s.
	 * @throws ProfileException if an error occurs.
	 */
	public List query(XMLQuery q) throws ProfileException {
		try {
			Server server = profileService.createServer();
			return server.query(q);
		} catch (RemoteException ex) {
			throw new ProfileException(ex);
                } catch (ProfileException ex) {
                        throw ex;
                }
	}

	/**
	 * Retrieve a profile by its ID.
	 *
	 * @param profileID ID of profile to retrieve.
	 * @return a <code>Profile</code> value.
	 * @throws ProfileException if an error occurs.
	 */
	public Profile getProfile(String profileID) throws ProfileException {
		try {
			Server server = profileService.createServer();
			return server.getProfile(profileID);
		} catch (RemoteException ex) {
			throw new ProfileException(ex);
                }
		catch (ProfileException ex) {
                        throw ex;
                }
	}

	/**      
         * Add profiles.
         * 
         * @param profile The profile string that may contains multiple profiles.
         * @throws ProfileException if an error occurs.
         */
	public void add(String profile) throws ProfileException
        {
                try {
			Server server = profileService.createServer();
                        server.add(profile);
                } catch (RemoteException ex) {
                        throw new ProfileException(ex);
		} catch (ProfileException ex) {
			throw ex;
		} 
       	}
         
	/**
         * Replace a profile.                 
         * 
         * @param profile The updated profile string.
         * @throws ProfileException if an error occurs.
         */
        public void replace(String profile) throws ProfileException
        {
                try {
			Server server = profileService.createServer();
                        server.replace(profile);
                } catch (RemoteException ex) {
                        throw new ProfileException(ex);
                }
		catch (ProfileException ex) {
                        throw ex;
                }
        }
                        
	/**
         * Remove a profile.                 
         * 
         * @param profId The profile ID.
	 * @param version The version.
         * @throws ProfileException if an error occurs.
         */
        public boolean remove(String profId, String version) throws ProfileException
        {
                try {
			Server server = profileService.createServer();
                        return server.remove(profId, version);
                } catch (RemoteException ex) {
                        throw new ProfileException(ex);
                } catch (ProfileException ex) {
                      	throw ex;
                }
        }

	/**
         * Command-line driver.
         *
         * @param argv a <code>String[]</code> value.
         * @throws Throwable if an error occurs.
         */
        public static void main(String[] argv) throws Throwable {
                if (argv.length != 2) {
                        System.err.println("Usage: <object-name> <query-expr>");
                        System.exit(1);
                }
                ProfileClient pc = new ProfileClient(argv[0]);
                XMLQuery q = new XMLQuery(argv[1], null, null, null, null, null, null, null, 9999);
                System.out.println(pc.query(q));
        }

	/** Profile service we're using. */
	private ProfileService profileService;
}
