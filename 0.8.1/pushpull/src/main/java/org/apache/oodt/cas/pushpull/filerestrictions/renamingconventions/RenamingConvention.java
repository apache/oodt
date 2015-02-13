/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.oodt.cas.pushpull.filerestrictions.renamingconventions;

//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//OODT imports
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.pushpull.retrievalsystem.RemoteFile;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class RenamingConvention {
	
	private static Logger LOG = Logger.getLogger(RenamingConvention.class.getName());

    private RenamingConvention() throws InstantiationException {
        throw new InstantiationException("Don't construct RenamingConventions!");
    }

    /**
     * Generates a unique file name for the given ProtocolFile
     * 
     * @param fileToGenNewNameFor
     *            The file for which a unique name will be generated
     * @return The unique file name (just the name).
     */
    public static String rename(RemoteFile fileToGenNewNameFor,
            String renamingString) {
    	try {
    	    renamingString = grepReplace(renamingString, fileToGenNewNameFor);
    	    renamingString = grepRemoveReplace(renamingString, fileToGenNewNameFor);
	        renamingString = replace(renamingString, "[FILENAME]",
	                fileToGenNewNameFor.getProtocolFile().getName());
	        renamingString = replace(renamingString, "[PATH_NO_FILENAME]",
	                getParentPath(fileToGenNewNameFor));
	        renamingString = replace(renamingString, "[HOST]", fileToGenNewNameFor
	                .getProtocolFile().getSite().getURL().getHost());
	        renamingString = replace(renamingString, "[PARENT_FILENAME]",
	                getParentFileName(fileToGenNewNameFor));
	        renamingString = replace(renamingString, "[PARENT_PATH_NO_FILENAME]",
	                getGrandParentPath(fileToGenNewNameFor));
	        renamingString = replace(renamingString, "[URL]", fileToGenNewNameFor
	                .getProtocolFile().getSite().getURL().toExternalForm());
	        renamingString = replace(renamingString, "[IS_DIR]", String
	                .valueOf(fileToGenNewNameFor.getProtocolFile().isDir()));
	        renamingString = PathUtils.doDynamicReplacement(
	            renamingString, fileToGenNewNameFor.getAllMetadata());
    	}catch (Exception e) {
    		LOG.log(Level.WARNING, "Failed to rename " + fileToGenNewNameFor 
    				+ " : " + e.getMessage());
    	}
        return renamingString;
    }

    private static String grepReplace(String theString, RemoteFile fileToGenNewNameFor) {
        Pattern grepPattern = Pattern.compile("\\[GREP\\(.*\\,.*\\)\\]");
        Matcher grepMatcher = grepPattern.matcher(theString);
        while (grepMatcher.find()) {
            String origGrepString = theString.substring(grepMatcher.start(),
                    grepMatcher.end()).trim();
            String grepString = origGrepString.replace("[GREP('", "").replace(
                    "')]", "").trim();
            String[] grepStringSplit = grepString.split("','");
            String pattern = grepStringSplit[0];
            String string = rename(fileToGenNewNameFor, grepStringSplit[1]);
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(string);
            if (m.find()) {
                theString = theString.replace(origGrepString, string.substring(
                        m.start(), m.end()));
            } else {
                theString = theString.replace(origGrepString, "null");
            }
        }
        return theString;
    }

    private static String grepRemoveReplace(String theString, RemoteFile fileToGenNewNameFor) {
        Pattern grepPattern = Pattern.compile("\\[GREP_RM\\(.*,.*\\)\\]");
        Matcher grepMatcher = grepPattern.matcher(theString);
        while (grepMatcher.find()) {
            String origGrepString = theString.substring(grepMatcher.start(),
                    grepMatcher.end()).trim();
            String grepString = origGrepString.replace("[GREP_RM('", "")
                    .replace("')]", "").trim();
            String[] grepStringSplit = grepString.split("','");
            String pattern = grepStringSplit[0];
            String string = rename(fileToGenNewNameFor, grepStringSplit[1]);
            System.out.println("PAT_STR: " + pattern + " " + string);
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(string);
            if (m.find()) {
                theString = theString.replace(origGrepString, string.replace(
                        string.substring(m.start(), m.end()), ""));
            } else {
                theString = theString.replace(origGrepString, "null");
            }
        }
        return theString;
    }
     
    private static String replace(String theString,
            String theValueToBeReplaced, String whatToReplaceWith) {
        if (theValueToBeReplaced == null || theValueToBeReplaced.equals(""))
            return theString;
        if (whatToReplaceWith == null)
            whatToReplaceWith = "";
        return theString.replace(theValueToBeReplaced, whatToReplaceWith);
    }

    private static String getParentPath(RemoteFile fileToGenNewNameFor) {
        String parentPath = "";
        try {
            parentPath = fileToGenNewNameFor.getProtocolFile().getParent().getPath();
        } catch (Exception e) {
        }
        return parentPath;
    }

    private static String getParentFileName(RemoteFile fileToGenNewNameFor) {
        String parentFileName = "";
        try {
            parentFileName = fileToGenNewNameFor.getProtocolFile().getParent().getName();
        } catch (Exception e) {
        }
        return parentFileName;
    }

    private static String getGrandParentPath(RemoteFile fileToGenNewNameFor) {
        String grandParentPath = "";
        try {
            grandParentPath = fileToGenNewNameFor.getProtocolFile().getParent()
                    .getParent().getPath();
        } catch (Exception e) {
        }
        return grandParentPath;
    }

}
