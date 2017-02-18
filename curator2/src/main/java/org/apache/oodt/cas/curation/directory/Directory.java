package org.apache.oodt.cas.curation.directory;

/**
 * Anything that can be a directory listed by curator.  Allows implementation of 
 * different backends to the staging area (not just file system).
 * 
 * @author starchmd
 */
public interface Directory {
    /**
     * Perform a listing of this directory.
     * @return - directory listing
     */
    public DirectoryListing list() throws Exception;
}
