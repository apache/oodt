package org.apache.oodt.cas.curation.metadata;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.cas.curation.metadata.FlatDirMetadataHandler;
import org.apache.oodt.cas.curation.metadata.MetadataHandler;
import org.apache.oodt.cas.curation.structs.ExtractorConfig;
import org.apache.oodt.cas.curation.util.ExtractorConfigReader;
import org.apache.oodt.cas.metadata.MetExtractor;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.extractors.MetadataProvidedMetExtractor;
import org.apache.oodt.cas.metadata.util.GenericMetadataObjectFactory;

/**
 * A backend that persists the working metadata entry from the front end.
 * Also handles metadata extraction, and extractor listing.
 *  
 * @author starchmd
 */
public class MetadataBackend {

    private static final Logger LOG = Logger.getLogger(MetadataBackend.class.getName());
    private MetadataHandler handler;
    private final Map<String,ExtractorConfig> extractors = new HashMap<String,ExtractorConfig>();
    /**
     * Construct a directory backend with hard-coded directories
     */
    public MetadataBackend() {
        handler = new FlatDirMetadataHandler();
    }
    /**
     * Gets persisted metadata object, and runs extractor if extractor is specified
     * @param file - file to get metadata from
     * @param user - user used to prevent cross-talk
     * @param extractor - if specified, this extractor will be run and replace existing metadata
     * @return newly constructed metadat object
     */
    public Metadata getMetadata(String file, String user, String extractor) throws Exception {
        LOG.info("Getting metadata for: "+file+" and extractor: "+extractor);
        Metadata met = null;
        try {
            System.out.println("Getting metadata for file: "+file +" with extractor: "+extractor);
            met = handler.get(file,user);
        } catch(Exception e) {
            met = new Metadata();
        }
        //If extractor is specified, then its metadata is considered "correct" and previous metadata is used only to fill filler
        if (extractors.containsKey(extractor)) {
            System.out.println("Merging");
            met = extractMergeAndPresist(file,user,extractor,met);
        }
        return met;
    }
    /**
     * Persists the metadata for a given file and runs extractor, if specified
     * @param file - file to specify metadata for
     * @param user - user used to prevent cross-talk
     * @param extractor - optional extractor to run
     * @param metadata - new metadata for file
     * @return metadata after optional extraction
     */
    public Metadata putMetadata(String file, String user, String extractor,Metadata metadata) throws Exception {
        LOG.info("Putting metadata for: "+file+" and extractor: "+extractor);
        fineLogMetadata("Put metadata:",metadata);
        handler.set(file, user, metadata);
        if (extractors.containsKey(extractor)) {
            metadata = extractMergeAndPresist(file,user,extractor,metadata);
        }
        return metadata;
    }
    /**
     * Deletes the metadata for a given file
     * @param file - file to specify metadata for
     * @param user - user used to prevent cross-talk
     */
    public void deleteMetadata(String file,String user) throws Exception {
        LOG.info("Deleting metadata for: "+file);
        handler.remove(file,user);
    }

    /**
     * Returns the list of extractors
     * @return - list of extractors
     */
    public Collection<String> getExtractors() throws Exception {
            loadMetadataExtractors();
            return this.extractors.keySet();
    }
    /**
     * Loads the metadata extractors
     */
    protected void loadMetadataExtractors() {
        File directory = new File(Configuration.getWithReplacement(Configuration.EXTRACTOR_AREA_CONFIG));
        //Load only sub-directories of the extractor config area 
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
              return new File(current, name).isDirectory();
            }
        };
        String[] subdirs = directory.list(filter);
        for (String id : subdirs != null?subdirs:new String[]{}) {
            try {
                extractors.put(id,ExtractorConfigReader.readFromDirectory(directory, id));
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to load extractor confugred at:"+new File(directory,id).toString()+" Exception:", e);
            }
        }
    }
    /**
     * In both set and get the extractor must be run, results merged, and whole metadata persisted
     * @param file - file associated with this run
     * @param user - user used to prevent cross-talk
     * @param extractor - extractor to run
     * @param met - metadata object
     * @return metadata object after merging
     * @throws Exception
     */
    protected Metadata extractMergeAndPresist(String file,String user,String extractor, Metadata met) throws Exception {
        LOG.info("Running "+extractor+" extractoe and presisting metadat for:"+file);
        System.out.println("Running Extractor");
        Metadata extracted = this.runExtractor(file, extractor,met);
        System.out.println("Replace Metadata");
        met.replaceMetadata(extracted);
        fineLogMetadata("Merged metadata:",met);
        //Persist newly extracted metadata
        System.out.println("Persisting metadata");
        handler.set(file, user, met);
        return met;
    }
    /**
     * If possible, logs metadata values finely
     */
    private void fineLogMetadata(String message, Metadata metadata) {
        if (LOG.isLoggable(Level.FINE)) {
            for (String key : metadata.getAllKeys()) {
                message += "\n\t"+key+": "+metadata.getMetadata(key);
            }
            LOG.fine(message);
        }
    }
    /**
     * Runs a metadata extractor on given file
     * @param file - file to extract metadata from
     * @param id - id of the extractor to use
     * @param metadata - metadata to attach to extraction, if possible
     * @return metadata extracted
     * @throws MetExtractionException - exception thrown when failure to extract metadata
     */
    protected Metadata runExtractor(String file, String id, Metadata metadata) throws MetExtractionException {
        System.out.println("Here1");
        String parent = new File(Configuration.getWithReplacement(Configuration.STAGING_AREA_CONFIG)).getParent();
        File full = new File(parent,file);
        //Get extractor
        System.out.println("Here2");
        ExtractorConfig config = extractors.get(id);
        System.out.println("Here3");
        MetExtractor metExtractor = GenericMetadataObjectFactory.getMetExtractorFromClassName(config.getClassName());
        System.out.println("Here4");
        metExtractor.setConfigFile(config.getConfigFiles().get(0));
        System.out.println("Here5");
        if (metExtractor instanceof MetadataProvidedMetExtractor) {
            ((MetadataProvidedMetExtractor)metExtractor).attachMetadata(metadata);
        }
        System.out.println("Here6");
        return metExtractor.extractMetadata(full.getAbsolutePath());
    }
}
