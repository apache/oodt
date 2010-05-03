//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.protocol;

//JDK imports
import java.io.Serializable;
import java.net.MalformedURLException;

/**
 * This class wraps up a URL for a given path, and whether the path specified by
 * the URL is a dirctory or not
 * 
 * @author bfoster
 */
public class ProtocolPath implements Serializable {

    private static final long serialVersionUID = 807275248811949120L;

    /**
     * The string verion of the path in the URL
     */
    protected String path;

    protected String remotePath;

    protected boolean relativeToHOME;

    /**
     * Specifies whether this path is a path to a directory or file
     */
    protected boolean isDir;

    public ProtocolPath() {
        super();
    }

    public ProtocolPath(String path, boolean isDir) {
        this.isDir = isDir;
        this.path = this.checkForDelimiters(path);
    }

    protected String checkForDelimiters(String path) {
        if (path.endsWith("/") && path.length() > 1)
            path = path.substring(0, path.length() - 1);
        relativeToHOME = !path.startsWith("/");
        return path;
    }

    public boolean isRelativeToHOME() {
        return relativeToHOME;
    }

    public String getPathString() {
        return path;
    }

    /**
     * Return the name of the file for which this path belongs
     * 
     * @return The Path file name.
     */
    public String getFileName() {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    /**
     * Returns the path that is used when downloading the file
     * 
     * @return The downloading path
     */
    public String getDownloadPath() {
        return (isDirectory()) ? path : path
                .substring(0, path.lastIndexOf("/"))
                + "/" + getDownloadFileName();
    }

    /**
     * Returns the file name that is used when downloading the file
     * 
     * @return The name used during downloading.
     */
    public String getDownloadFileName() {
        return "Downloading_" + getFileName();
    }

    /**
     * Tells whether this path is a path to a directory or not
     * 
     * @return True if this Path is a directory
     */
    public boolean isDirectory() {
        return isDir;
    }

    public boolean equals(Object path) {
        if (path instanceof ProtocolPath) {
            ProtocolPath p = (ProtocolPath) path;
            return (p.getPathString().equals(this.getPathString()));
        }
        return false;
    }

    public String getParentDirPath() throws MalformedURLException {
        if (path.length() <= 1)
            return null;
        return path.substring(0, path.lastIndexOf("/"));
    }

    public String toString() {
        return (path + " isDir=" + this.isDir);
    }

    public ProtocolPath getParentPath() throws MalformedURLException {
        return new ProtocolPath(path.substring(0, path.lastIndexOf("/")), true);
    }

}
