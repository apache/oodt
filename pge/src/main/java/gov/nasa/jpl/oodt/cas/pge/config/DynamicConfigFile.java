//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pge.config;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A dynamic representation of a SciPgeConfigFile
 * </p>.
 */
public class DynamicConfigFile {

    private String filePath, writerClass;

    private Object[] args;

    public DynamicConfigFile(String filePath, String writerClass, Object[] args) {
        this.filePath = filePath;
        this.writerClass = writerClass;
        this.args = args;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setWriterClass(String writerClass) {
        this.writerClass = writerClass;
    }

    public String getWriterClass() {
        return this.writerClass;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object[] getArgs() {
        return this.args;
    }

}
