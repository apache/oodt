/**
 * 
 */
package org.apache.oodt.cas.metadata.extractors;

import org.apache.oodt.cas.metadata.AbstractMetExtractor;
import org.apache.oodt.cas.metadata.MetExtractorConfigReader;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * A metadata extractor that allows the user to specify an attached metadata object
 * 
 * @author starchmd
 */
public abstract class MetadataProvidedMetExtractor extends AbstractMetExtractor {

    protected Metadata attached = new Metadata();
    /**
     * Pass-through constructor
     * @param reader - reader to read configuration
     */
    public MetadataProvidedMetExtractor(MetExtractorConfigReader reader) {
        super(reader);
    }
    /**
     * Attache a different metadata object
     * @param metadata - metadata object to attach
     */
    public void attachMetadata(Metadata metadata) {
        this.attached = metadata;
    }
}
