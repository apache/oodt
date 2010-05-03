//Copyright (c) 2007, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.parsers;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.Parser;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.VirtualFile;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.VirtualFileStructure;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.RemoteSite;
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.ParserException;

//JDK imports
import java.io.FileInputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class ClassNoaaEmailParser implements Parser {

    public ClassNoaaEmailParser() {}

    public VirtualFileStructure parse(FileInputStream emailFile)
            throws ParserException {
        try {
            VirtualFile root = VirtualFile.createRootDir();
            Scanner s = new Scanner(emailFile);
            StringBuffer sb = new StringBuffer("");
            while (s.hasNextLine())
                sb.append(s.nextLine() + "\n");

            if (!validEmail(sb.toString()))
                throw new ParserException(
                        "Email not a IASI data processed notification email");

            Pattern cdPattern = Pattern.compile("\\s*cd\\s{1,}.{1,}?(?:\\s|$)");
            Matcher cdMatcher = cdPattern.matcher(sb);
            Pattern getPattern = Pattern.compile("\\s*get\\s{1,}.{1,}?(?:\\s|$)");
            Matcher getMatcher = getPattern.matcher(sb);
            
            VirtualFile vf = null;
            while (cdMatcher.find() && getMatcher.find()) {
                String cdCommand = sb.substring(cdMatcher.start(), cdMatcher.end());
                String directory = cdCommand.trim().split(" ")[1];

                vf = new VirtualFile(root, directory, true);
                vf.setNoDirs(true);

                String getCommand = sb.substring(getMatcher.start(), getMatcher.end());
                String file = getCommand.trim().split(" ")[1];

                if (file.endsWith("*")) {
                    vf.addChild(new VirtualFile(file.substring(0,
                            file.length() - 1), false));
                    vf.addChild(new VirtualFile(file.substring(0,
                            file.length() - 1)
                            + ".sig", false));
                } else {
                    vf.addChild(new VirtualFile(file, false));
                }
            }

            Pattern ftpPattern = Pattern.compile("\\sftp\\..*?\\s");
            Matcher ftpMatcher = ftpPattern.matcher(sb);
            RemoteSite remoteSite = null;
            if (ftpMatcher.find()) {
                String ftpSite = sb.substring(ftpMatcher.start(), ftpMatcher.end()).trim();
                remoteSite = new RemoteSite(null, new URL("ftp://"
                        + ftpSite), "anonymous", System
                        .getenv("user.name")
                        + "@jpl.nasa.gov");
            }

            return new VirtualFileStructure(remoteSite, "/", root);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParserException("Failed to parse IASI email : "
                    + e.getMessage());
        }
    }

    private boolean validEmail(String email) {
        String[] containsStrings = (System.getProperties()
                .getProperty("gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.parsers.class.noaa.email.parser.contains.exprs")
                + ",").split(",");
        for (String containsString : containsStrings)
            if (!email.contains(containsString))
                return false;
        return true;
    }
}
