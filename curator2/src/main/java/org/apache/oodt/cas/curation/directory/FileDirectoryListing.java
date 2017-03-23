package org.apache.oodt.cas.curation.directory;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.commons.validation.DirectoryValidator;
import org.apache.oodt.commons.validation.ValidationOutput;

/**
 * Created by shah on 2/22/17.
 */
public class FileDirectoryListing implements DirectoryListing {

  private static final Logger LOG = Logger.getLogger(FileDirectoryListing.class.getName());

  //Types of directory objects
  public enum Type {
    DIRECTORY,
    OBJECT
  }
  public static final String ROOT_NAME = "Root";
  //Attributes of node
  DirectoryListing.Type type;
  String name;
  String path;
  ValidationOutput validation;

  //Children listings (only valid for directory types)
  List<DirectoryListing> children = new LinkedList<DirectoryListing>();

  /**
   * Get a directory listing
   * @param type - type of listing
   * @param name - name of object
   */
  public FileDirectoryListing(DirectoryListing.Type type,String name,String path, ValidationOutput validation) {
    this.name = name;
    this.type = type;
    this.path = path;
    this.children = (type == DirectoryListing.Type.DIRECTORY) ? new LinkedList<DirectoryListing>() : null;
    this.validation = validation;
  }
  /**
   * Create a directory listing
   * @param paths - list of file paths
   * @param validator
   * @return top-level directory listing object
   */
  public static DirectoryListing listingFromFileObjects(Collection<File> paths, File root,
      DirectoryValidator validator) {
    //Shallow copy and sort
    List<File> copy = new LinkedList<File>(paths);
    Collections.sort(copy);
    //Create a stack to hold directories (implementation details)
    LinkedList<DirectoryListing> stack = new LinkedList<DirectoryListing>();
    stack.addLast( (root != null && root.isDirectory()) ?
        new FileDirectoryListing(DirectoryListing.Type.DIRECTORY,root.getPath(),root.getPath(),
            validator != null ? validator.validate(root, Configuration.getAllProperties()):null) :
        new FileDirectoryListing(DirectoryListing.Type.DIRECTORY,ROOT_NAME,"", null));
    for (File file : paths) {
      if (file.equals(root))
        continue;
      //Remove all directories off stack until file starts with last's path
      while (!file.getPath().startsWith(stack.peekLast().path))
        stack.removeLast();
      //Get type and name of this file path and create dl object
      DirectoryListing.Type type =  file.isDirectory() ? DirectoryListing.Type.DIRECTORY : DirectoryListing.Type.OBJECT;
      DirectoryListing dl = new FileDirectoryListing(type,file.getName(),file.getPath(),
          validator != null ? validator.validate(file, Configuration.getAllProperties()) :
              null);

      //Add to last's children
      stack.peekLast().children.add(dl);
      if (type == DirectoryListing.Type.DIRECTORY) {
        stack.addLast(dl);
      }
    }
    return stack.peekFirst();
  }
}