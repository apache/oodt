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
 * A regular expression class to define what OutputFiles
 * to identify after running a PGE
 * </p>.
 */
public class RegExprOutputFiles {

    private String regExp;

    private String converterClass;

    private Object[] args;

    private RenamingConv renamingConv;
    
    public RegExprOutputFiles(String regExp, String converterClass, 
    		RenamingConv renamingConv, Object[] args) {
        this.regExp = regExp;
        this.converterClass = converterClass;
        this.renamingConv = renamingConv;
        this.args = args;
    }
    
    public RenamingConv getRenamingConv() {
    	return this.renamingConv;
    }

    public String getRegExp() {
        return this.regExp;
    }

    public String getConverterClass() {
        return this.converterClass;
    }

    public Object[] getArgs() {
        return this.args;
    }

}
