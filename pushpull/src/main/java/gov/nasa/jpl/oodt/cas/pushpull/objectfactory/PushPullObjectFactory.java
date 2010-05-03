//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.objectfactory;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.ingest.Cache;
import gov.nasa.jpl.oodt.cas.filemgr.ingest.CacheFactory;
import gov.nasa.jpl.oodt.cas.filemgr.ingest.Ingester;

//JDK imports
import java.lang.reflect.InvocationTargetException;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class PushPullObjectFactory {

    private PushPullObjectFactory() throws InstantiationException {
        throw new InstantiationException("Don't construct factory classes!");
    }

    public static <T> T createNewInstance(Class<T> clazz) throws InstantiationException {
    	try {
			return clazz.newInstance();
		} catch (Exception e) {
            throw new InstantiationException(
                    "Failed to create new object : "
                            + e.getMessage());
		}
    }
    
    public static Ingester createIngester(String ingesterClass,
            String cacheFactoryClass) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException,
            IllegalArgumentException, SecurityException,
            InvocationTargetException, NoSuchMethodException {
        String dataTransferFactory = System
                .getProperty("gov.nasa.jpl.oodt.cas.filemgr.datatransfer.factory");
        System.out.println("TRANSFER: " + dataTransferFactory);
        if (cacheFactoryClass == null || cacheFactoryClass.equals("")) {
            return (Ingester) Class.forName(ingesterClass).getConstructor(
                    dataTransferFactory.getClass()).newInstance(
                    dataTransferFactory);
        } else {
            Class<CacheFactory> cacheFactory = (Class<CacheFactory>) Class
                    .forName(cacheFactoryClass);
            Cache cache = cacheFactory.newInstance().createCache();
            return (Ingester) Class.forName(ingesterClass).getConstructor(
                    dataTransferFactory.getClass(), cache.getClass())
                    .newInstance(dataTransferFactory, cache);
        }
    }

}
