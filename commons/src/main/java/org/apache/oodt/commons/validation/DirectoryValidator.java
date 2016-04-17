package org.apache.oodt.commons.validation;

import java.io.File;
import java.util.Map;

/**
 * Interface for Directory Validation inside CAS Curator
 *
 * @author tbarber
 */
public interface DirectoryValidator {

  ValidationOutput validate(File f, Map<String,String> stagingpath);

}
