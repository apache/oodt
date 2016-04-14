package org.apache.oodt.cas.curation.directory.validation;

import java.io.File;

/**
 * Created by bugg on 14/04/16.
 */
public interface DirectoryValidator {

  ValidationOutput validate(File f);

}
