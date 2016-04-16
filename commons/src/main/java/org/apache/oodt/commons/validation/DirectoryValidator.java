package org.apache.oodt.commons.validation;

import java.io.File;

/**
 * Interface for Directory Validation inside CAS Curator
 *
 * @author tbarber
 */
public interface DirectoryValidator {

  ValidationOutput validate(File f, String stagingpath);

}
