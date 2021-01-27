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

package org.apache.oodt.cas.filemgr.util;

//JDK imports
import org.apache.oodt.cas.filemgr.catalog.Catalog;
import org.apache.oodt.cas.filemgr.catalog.CatalogFactory;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransfer;
import org.apache.oodt.cas.filemgr.datatransfer.DataTransferFactory;
import org.apache.oodt.cas.filemgr.ingest.Cache;
import org.apache.oodt.cas.filemgr.ingest.CacheFactory;
import org.apache.oodt.cas.filemgr.metadata.extractors.FilemgrMetExtractor;
import org.apache.oodt.cas.filemgr.repository.RepositoryManager;
import org.apache.oodt.cas.filemgr.repository.RepositoryManagerFactory;
import org.apache.oodt.cas.filemgr.structs.query.conv.VersionConverter;
import org.apache.oodt.cas.filemgr.structs.query.filter.FilterAlgor;
import org.apache.oodt.cas.filemgr.structs.type.TypeHandler;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;
import org.apache.oodt.cas.filemgr.validation.ValidationLayerFactory;
import org.apache.oodt.cas.filemgr.versioning.Versioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//OODT imports

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Generic object creation utilities for FileManager objects from their
 * interface class names.
 * </p>
 * 
 */
public final class GenericFileManagerObjectFactory {

    /* our log stream */
    private static final Logger logger = LoggerFactory.getLogger(GenericFileManagerObjectFactory.class);

    private GenericFileManagerObjectFactory() throws InstantiationException {
        throw new InstantiationException(
                "Don't construct final factory classes!");
    }
    
    /**
     * <p>
     * Constructs a new {@link DataTransfer} from the specified
     * <code>serviceFactory</code>.
     * </p>
     * 
     * @param serviceFactory
     *            The Service Factory class name that will be instantiated to
     *            provide DataTransfer objects.
     * @return A newly instantiated {@link DataTransfer} object.
     */
    @SuppressWarnings("unchecked")
    public static DataTransfer getDataTransferServiceFromFactory(
            String serviceFactory) {
        DataTransferFactory dataTransferFactory;
        Class<DataTransferFactory> dataTransferFactoryClass;

        try {
            dataTransferFactoryClass = (Class<DataTransferFactory>) Class.forName(serviceFactory);
            dataTransferFactory = dataTransferFactoryClass.newInstance();
            return dataTransferFactory.createDataTransfer();
        } catch (ClassNotFoundException e) {
            logger.warn(
                    "ClassNotFoundException when loading data transfer factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        } catch (InstantiationException e) {
            logger.warn(
                    "InstantiationException when loading data transfer factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.warn(
                    "IllegalAccessException when loading data transfer factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * <p>
     * Constructs a new {@link RepositoryManager} from the specified
     * <code>serviceFactory</code>.
     * </p>
     * 
     * @param serviceFactory
     *            The class name of the service factory used to create new
     *            RepositoryManager objects.
     * @return A newly constructed {@link RepositoryManager} object.
     */
    @SuppressWarnings("unchecked")
    public static RepositoryManager getRepositoryManagerServiceFromFactory(
            String serviceFactory) {
        RepositoryManagerFactory factory;
        Class<RepositoryManagerFactory> clazz;

        try {
            clazz = (Class<RepositoryManagerFactory>) Class.forName(serviceFactory);
            factory = clazz.newInstance();
            return factory.createRepositoryManager();
        } catch (ClassNotFoundException e) {
            logger.warn(
                    "ClassNotFoundException when loading data store factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        } catch (InstantiationException e) {
            logger.warn(
                    "InstantiationException when loading data store factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.warn(
                    "IllegalAccessException when loading data store factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * <p>
     * Constructs a new {@link Catalog} from the specified
     * <code>serviceFactory</code>.
     * </p>
     * 
     * @param serviceFactory
     *            The class name of the service factory used to create new
     *            Catalog objects.
     * @return A newly constructed {@link Catalog} object.
     */
    @SuppressWarnings("unchecked")
    public static Catalog getCatalogServiceFromFactory(String serviceFactory) {
        CatalogFactory factory;
        Class<CatalogFactory> clazz;

        try {
            clazz = (Class<CatalogFactory>) Class.forName(serviceFactory);
            factory = clazz.newInstance();
            return factory.createCatalog();
        } catch (ClassNotFoundException e) {
            logger.warn(
                    "ClassNotFoundException when loading metadata store factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        } catch (InstantiationException e) {
            logger.warn(
                    "InstantiationException when loading metadata store factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.warn(
                    "IllegalAccessException when loading metadata store factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * <p>
     * Creates a {@link ValidationLayer} from the specified
     * <code>serviceFactory</code>.
     * </p>
     * 
     * @param serviceFactory
     *            The classname of the ValidationLayerFactory to use to create
     *            the ValidationLayer.
     * @return A new {@link ValidationLayer}, created from the specified
     *         ValidationLayerFactory.
     */
    @SuppressWarnings("unchecked")
    public static ValidationLayer getValidationLayerFromFactory(
            String serviceFactory) {
        ValidationLayerFactory factory;
        Class<ValidationLayerFactory> clazz;

        try {
            clazz = (Class<ValidationLayerFactory>) Class.forName(serviceFactory);
            factory = clazz.newInstance();
            return factory.createValidationLayer();
        } catch (ClassNotFoundException e) {
            logger.warn(
                    "ClassNotFoundException when loading validation layer factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        } catch (InstantiationException e) {
            logger.warn(
                    "InstantiationException when loading validation layer factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.warn(
                    "IllegalAccessException when loading validation layer factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        }

        return null;
    }
    
    @SuppressWarnings("unchecked")
    public static Cache getCacheFromFactory(String serviceFactory){
        CacheFactory factory;
        Class<CacheFactory> clazz;

        try {
            clazz = (Class<CacheFactory>) Class.forName(serviceFactory);
            factory = clazz.newInstance();
            return factory.createCache();
        } catch (ClassNotFoundException e) {
            logger.warn(
                    "ClassNotFoundException when loading cache factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        } catch (InstantiationException e) {
            logger.warn(
                    "InstantiationException when loading cache factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.warn(
                    "IllegalAccessException when loading cache factory class "
                            + serviceFactory + " Message: " + e.getMessage(), e);
        }

        return null;        
    }

    /**
     * <p>
     * Constructs a new {@link Versioner} from the specified
     * <code>className</code>.
     * </p>
     * 
     * @param className
     *            The class name of the Versioner object to create.
     * @return A newly constructed {@link Versioner} object.
     */
    public static Versioner getVersionerFromClassName(String className) {
        try {
            @SuppressWarnings("unchecked")
            Class<Versioner> versionerClass = (Class<Versioner>) Class.forName(className);
            return versionerClass.newInstance();
        } catch (ClassNotFoundException e) {
            logger.warn(
                    "ClassNotFoundException when loading versioner class "
                            + className + " Message: " + e.getMessage(), e);
        } catch (InstantiationException e) {
            logger.warn(
                    "InstantiationException when loading versioner class "
                            + className + " Message: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.warn(
                    "IllegalAccessException when loading versioner class "
                            + className + " Message: " + e.getMessage(), e);
        }

        return null;
    }

    public static FilemgrMetExtractor getExtractorFromClassName(String className) {
        try {
            @SuppressWarnings("unchecked")
            Class<FilemgrMetExtractor> extractorClass = (Class<FilemgrMetExtractor>) Class.forName(className);
            return extractorClass.newInstance();
        } catch (ClassNotFoundException e) {
            logger.warn(
                    "ClassNotFoundException when loading extractor class "
                            + className + " Message: " + e.getMessage(), e);
        } catch (InstantiationException e) {
            logger.warn(
                    "InstantiationException when loading extractor class "
                            + className + " Message: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.warn(
                    "IllegalAccessException when loading extractor class "
                            + className + " Message: " + e.getMessage(), e);
        }
        return null;
    }
    
    public static TypeHandler getTypeHandlerFromClassName(String className) {
        try {
            return (TypeHandler) Class.forName(className).newInstance();
        }catch (Exception e) {
            logger.warn("Failed to load TypeHandler class '" + className + "' : " + e.getMessage(), e);
        }
        return null;
    }
    
    public static FilterAlgor getFilterAlgorFromClassName(String className) {
        try {
            return (FilterAlgor) Class.forName(className).newInstance();
        }catch (Exception e) {
            logger.warn("Failed to load TypeHandler class '" + className + "' : " + e.getMessage(), e);
        }
        return null;
    }
    
    public static VersionConverter getVersionConverterFromClassName(String className) {
        try {
            return (VersionConverter) Class.forName(className).newInstance();
        }catch (Exception e) {
            logger.warn("Failed to load TypeHandler class '" + className + "' : " + e.getMessage(), e);
        }
        return null;
    }

}
