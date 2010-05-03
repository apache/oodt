// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: ProfileServiceAdaptor.java,v 1.1.1.1 2004/03/02 20:53:20 kelly Exp $

package jpl.eda.profile.corba;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.List;
import jpl.eda.profile.Profile;
import jpl.eda.profile.ProfileException;
import jpl.eda.profile.corba.ServerPackage.ProfileServiceException;
import jpl.eda.xmlquery.XMLQuery;

/**
 * Adapt a CORBA profile service into a Java-compatible interface.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class ProfileServiceAdaptor implements jpl.eda.profile.ProfileService {
	/**
	 * Creates a new <code>ProfileServiceAdaptor</code> instance.
	 *
	 * @param corbaProfileService The CORBA profile service to adapt.
	 */
	public ProfileServiceAdaptor(ProfileService corbaProfileService) {
		svc = corbaProfileService;
	}

	public jpl.eda.profile.Server createServer() {
		return new ServerAdaptor(svc.createServer());
	}

	/**
	 * Adapter for the profile server instance.
	 */
	private static class ServerAdaptor implements jpl.eda.profile.Server {
		/**
		 * Creates a new <code>ServerAdaptor</code> instance.
		 *
		 * @param corbaServer CORBA server to adapt.
		 */
		public ServerAdaptor(Server corbaServer) {
			svr = corbaServer;
		}

		public List query(XMLQuery q) throws ProfileException, RemoteException {
			try {
				byte[] rc = svr.query(q.getXMLDocString());
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rc));
				List result = (List) in.readObject();
				in.close();
				return result;
			} catch (ProfileServiceException ex) {
				throw new ProfileException(ex.errorInfo);
			} catch (org.omg.CORBA.SystemException ex) {
				throw new RemoteException(ex.getMessage());
			} catch (IOException ex) {
				throw new IllegalStateException("Unexpected IOException: " + ex.getMessage());
			} catch (ClassNotFoundException ex) {
				throw new IllegalStateException("Unexpected ClassNotFoundException: " + ex.getMessage());
			}
		}

		public Profile getProfile(String id) throws ProfileException, RemoteException {
			try {
				byte[] rc = svr.getProfile(id);
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rc));
				Profile result = (Profile) in.readObject();
				in.close();
				return result;
			} catch (ProfileServiceException ex) {
				throw new ProfileException(ex.errorInfo);
			} catch (org.omg.CORBA.SystemException ex) {
				throw new RemoteException(ex.getMessage());
			} catch (IOException ex) {
				throw new IllegalStateException("Unexpected IOException: " + ex.getMessage());
			} catch (ClassNotFoundException ex) {
				throw new IllegalStateException("Unexpected ClassNotFoundException: " + ex.getMessage());
			}

		}

		public void add(String profileStr) throws ProfileException, RemoteException 
		{
			try {
                                svr.add(profileStr);
                        } catch (ProfileServiceException ex) {
  				throw new ProfileException(ex.errorInfo);
                        } catch (org.omg.CORBA.SystemException ex) {
                                throw new RemoteException(ex.getMessage());
			}                        
                }

		public void replace(String profileStr) throws ProfileException, RemoteException {
                        try {
                                svr.replace(profileStr);
                        } catch (ProfileServiceException ex) {
                                throw new ProfileException(ex.errorInfo);
                        } catch (org.omg.CORBA.SystemException ex) {
                                throw new RemoteException(ex.getMessage());
                        }
                }

		public boolean remove(String profId, String version) throws ProfileException,RemoteException {
                        try {
                                return svr.remove(profId, version);
                        } catch (ProfileServiceException ex) { 
                                throw new ProfileException(ex.errorInfo);
                        } catch (org.omg.CORBA.SystemException ex) {
                                throw new RemoteException(ex.getMessage());
                        }
                }


		public void finalize() throws Throwable {
			try {
				svr.destroy();
			} catch (Throwable ignore) {}
			super.finalize();
		}

		/** CORBA instance server we're adapting. */
		private Server svr;
	}

	/** CORBA profile service we're adapting. */
	private ProfileService svc;
}
