//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.parsers;

//JDK imports
import java.io.FileInputStream;
import java.util.Scanner;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.ParserException;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.Parser;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.VirtualFile;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.VirtualFileStructure;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class FileListParser implements Parser {

	public FileListParser() {}
	
    public VirtualFileStructure parse(FileInputStream inputFile)
            throws ParserException {
        Scanner scanner = new Scanner(inputFile);
        VirtualFile root = VirtualFile.createRootDir();
        String initialCdDir = "/";
        if (scanner.hasNextLine()) {
            initialCdDir = scanner.nextLine();
            while (scanner.hasNextLine()) {
                new VirtualFile(root, initialCdDir + "/" + scanner.nextLine(),
                        false);
            }
        }
        return new VirtualFileStructure(initialCdDir, root);
    }

}
