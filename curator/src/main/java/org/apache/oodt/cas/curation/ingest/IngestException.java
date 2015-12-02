package org.apache.oodt.cas.curation.ingest;

/**
 * A validation backend exception
 * 
 * @author starchmd
 */
public class IngestException extends Exception {
    private static final long serialVersionUID = -7714024388845498231L;
    /**
     * ctor
     * @param message - message
     * @param cause - cause of error
     */
    public IngestException(String message,Exception cause) {
        super(message,cause);
    }
}
