package org.apache.oodt.cas.curation.directory;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * A file-system based directory for listing into the curator.
 * 
 * @author starchmd
 */
public class FileDirectory implements Directory {

    String directory = null;
    /**
     * Build the object around a set directory
     * @param directory
     */
    public FileDirectory(String directory) {
        this.directory = directory;
    }
    /* (non-Javadoc)
     * @see org.apache.oodt.cas.curation.directory.Directory#list()
     */
    @Override
    public DirectoryListing list() throws IOException {
        Collection<File> listing = FileUtils.listFilesAndDirs(new File(directory),TrueFileFilter.INSTANCE,TrueFileFilter.INSTANCE);
        return DirectoryListing.lisingFromFileObjects(listing,listing.iterator().next());
    }

}
