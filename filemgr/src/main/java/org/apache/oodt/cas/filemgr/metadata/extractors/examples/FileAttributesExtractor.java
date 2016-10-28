package org.apache.oodt.cas.filemgr.metadata.extractors.examples;

import org.apache.oodt.cas.filemgr.metadata.extractors.AbstractFilemgrMetExtractor;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author rverma
 * @author adhulipala
 */
public class FileAttributesExtractor extends AbstractFilemgrMetExtractor {

    public static final String BASIC_FILE_ATTRIBUTES = "*";
    public static final String POSIX_FILE_ATTRIBUTES = "posix:*";

    private String attributes;

    Logger LOG = Logger.getLogger(FileAttributesExtractor.class.getName());

    public FileAttributesExtractor() {
        attributes = BASIC_FILE_ATTRIBUTES;
    }

    public FileAttributesExtractor(String attributes) {
        this.attributes = attributes;
    }

    @Override
    public Metadata doExtract(Product product, Metadata metadata) throws MetExtractionException {
        Metadata outMetadata = new Metadata();

        merge(metadata, outMetadata);
        Metadata fileAttributesMetadata = getMetadataFromFileAttributes(product);

        LOG.fine(fileAttributesMetadata.toString());

        merge(fileAttributesMetadata, outMetadata);

        return outMetadata;
    }

    @Override
    public void doConfigure() {
        if (this.configuration != null) {
            this.attributes = this.configuration.getProperty("attributes");
        }
    }


    private Metadata getMetadataFromFileAttributes(Product product) {
        Metadata met = new Metadata();

        File file = null;
        try {
            file = getProductFile(product);
        } catch (MetExtractionException e) {
            LOG.severe(e.getMessage());
        }

        if (file != null) {
            Map<String, Object> attrMap = new HashMap<String, Object>();
            try {
                attrMap = Files.readAttributes(file.toPath(), attributes);
            } catch (IOException e) {
                LOG.severe(e.getMessage());
            }

            for (Map.Entry<String, Object> entry : attrMap.entrySet()) {
                met.addMetadata(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return met;
    }
}