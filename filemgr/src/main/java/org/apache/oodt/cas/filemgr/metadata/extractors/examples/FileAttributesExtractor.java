package org.apache.oodt.cas.filemgr.metadata.extractors.examples;

import org.apache.oodt.cas.filemgr.metadata.extractors.AbstractFilemgrMetExtractor;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rverma
 * @author adhulipala
 */
public class FileAttributesExtractor extends AbstractFilemgrMetExtractor {

    public static final String BASIC_FILE_ATTRIBUTES = "*";
    public static final String POSIX_FILE_ATTRIBUTES = "posix:*";

    private String attributes;

    Logger LOG = LoggerFactory.getLogger(FileAttributesExtractor.class);

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

        LOG.info(fileAttributesMetadata.toString());

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
            LOG.error(e.getMessage(), e);
        }

        if (file != null) {
            Map<String, Object> attrMap = new HashMap<String, Object>();
            try {
                attrMap = Files.readAttributes(file.toPath(), attributes);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }

            for (Map.Entry<String, Object> entry : attrMap.entrySet()) {
                met.addMetadata(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return met;
    }
}