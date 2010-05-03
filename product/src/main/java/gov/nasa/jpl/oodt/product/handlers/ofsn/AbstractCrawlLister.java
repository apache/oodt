//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.product.handlers.ofsn;

//JDK imports
import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import jpl.eda.product.ProductException;

/**
 * 
 * An abstract {@link OFSNListHandler} for generating file lists based on two
 * simple flags: recurse, and crawlForDirs, indicating to crawl for directories
 * rather than files.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public abstract class AbstractCrawlLister implements OFSNListHandler {

  protected final static Logger LOG = Logger
      .getLogger(AbstractCrawlLister.class.getName());

  protected static final FileFilter FILE_FILTER = new FileFilter() {
    public boolean accept(File pathname) {
      if (pathname.isFile()) {
        return true;
      } else
        return false;
    }
  };

  protected static final FileFilter DIR_FILTER = new FileFilter() {
    public boolean accept(File pathname) {
      if (pathname.isDirectory()) {
        return true;
      } else
        return false;
    }
  };

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNListHandler#configure(java.
   * util.Properties)
   */
  public abstract void configure(Properties conf);

  /*
   * (non-Javadoc)
   * 
   * @see
   * gov.nasa.jpl.oodt.product.handlers.ofsn.OFSNListHandler#getListing(java
   * .lang.String)
   */
  public abstract File[] getListing(String ofsn) throws ProductException;

  protected File[] crawlFiles(File dirRoot, boolean recur,
      boolean crawlForDirs) {
    if (dirRoot == null || ((dirRoot != null && !dirRoot.exists())))
      throw new IllegalArgumentException("dir root: [" + dirRoot
          + "] is null or non existant!");

    List<File> fileList = new Vector<File>();

    // start crawling
    Stack<File> stack = new Stack<File>();
    stack.push(dirRoot.isDirectory() ? dirRoot : dirRoot.getParentFile());
    while (!stack.isEmpty()) {
      File dir = (File) stack.pop();
      LOG.log(Level.INFO, "OFSN: Crawling " + dir);

      File[] productFiles = null;
      if (crawlForDirs) {
        productFiles = dir.listFiles(DIR_FILTER);
      } else {
        productFiles = dir.listFiles(FILE_FILTER);
      }

      for (int j = 0; j < productFiles.length; j++) {
        fileList.add(productFiles[j]);
      }

      if (recur) {
        File[] subdirs = dir.listFiles(DIR_FILTER);
        if (subdirs != null)
          for (int j = 0; j < subdirs.length; j++)
            stack.push(subdirs[j]);
      }
    }

    return fileList.toArray(new File[fileList.size()]);
  }

}
