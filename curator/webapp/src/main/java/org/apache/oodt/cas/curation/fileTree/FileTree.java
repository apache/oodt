package org.apache.oodt.cas.curation.fileTree;

import java.util.Set;
import java.util.regex.Pattern;

import org.apache.wicket.markup.html.WebComponent;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

public class FileTree extends WebComponent{

  public FileTree(String id) {
    super(id);
  }

  private static final long serialVersionUID = 3911179455208050261L;

  public static Set<String> getImageFiles() {
    Pattern pattern = Pattern.compile(".*\\.png");
    return new Reflections(FileTree.class.getPackage(), new ResourcesScanner())
        .getResources(pattern);
  }

}
