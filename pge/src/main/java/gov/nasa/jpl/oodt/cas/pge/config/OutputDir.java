//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pge.config;

//JDK imports
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * An output dir for PGE execution
 * </p>.
 */
public class OutputDir {

    private String path;

    private List<RegExprOutputFiles> regExprOutputFilesList;

    private boolean createBeforeExe;

    public OutputDir(String path, boolean createBeforeExe) {
        this.path = path;
        this.createBeforeExe = createBeforeExe;
        this.regExprOutputFilesList = new LinkedList<RegExprOutputFiles>();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public void addRegExprOutputFiles(RegExprOutputFiles regExprOutputFiles) {
        this.regExprOutputFilesList.add(regExprOutputFiles);
    }

    public List<RegExprOutputFiles> getRegExprOutputFiles() {
        return this.regExprOutputFilesList;
    }

    public void setCreateBeforeExe(boolean createBeforeExe) {
        this.createBeforeExe = createBeforeExe;
    }

    public boolean isCreateBeforeExe() {
        return this.createBeforeExe;
    }

}
