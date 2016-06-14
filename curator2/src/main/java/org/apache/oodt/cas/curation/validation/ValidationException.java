/**
 * 
 */
package org.apache.oodt.cas.curation.validation;

/**
 * A validation backend exception
 * 
 * @author starchmd
 */
public class ValidationException extends Exception {
    private static final long serialVersionUID = -7714024388845498231L;
    /**
     * ctor
     * @param message - message
     * @param cause - cause of error
     */
    public ValidationException(String message,Exception cause) {
        super(message,cause);
    }
}
