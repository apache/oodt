//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.filerestrictions;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.protocol.RemoteSite;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class VirtualFileStructure {

    private String pathToRoot;

    private VirtualFile root;

    private RemoteSite remoteSite;

    public VirtualFileStructure(String pathToRoot, VirtualFile root) {
        this.pathToRoot = pathToRoot;
        this.root = root;
    }

    public VirtualFileStructure(RemoteSite remoteSite, String pathToRoot,
            VirtualFile root) {
        this(pathToRoot, root);
        this.remoteSite = remoteSite;
    }

    public String getPathToRoot() {
        return this.pathToRoot;
    }

    public VirtualFile getRootVirtualFile() {
        return this.root;
    }

    public RemoteSite getRemoteSite() {
        return this.remoteSite;
    }

    public boolean isRootBased() {
        return this.pathToRoot.startsWith("/");
    }

}
