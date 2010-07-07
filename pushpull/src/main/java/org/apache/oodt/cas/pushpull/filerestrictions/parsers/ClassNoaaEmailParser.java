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


package org.apache.oodt.cas.pushpull.filerestrictions.parsers;

//OODT imports
import org.apache.oodt.cas.pushpull.filerestrictions.Parser;
import org.apache.oodt.cas.pushpull.filerestrictions.VirtualFile;
import org.apache.oodt.cas.pushpull.filerestrictions.VirtualFileStructure;
import org.apache.oodt.cas.pushpull.protocol.RemoteSite;
import org.apache.oodt.cas.pushpull.exceptions.ParserException;

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
                .getProperty("org.apache.oodt.cas.pushpull.filerestrictions.parsers.class.noaa.email.parser.contains.exprs")
                + ",").split(",");
        for (String containsString : containsStrings)
            if (!email.contains(containsString))
                return false;
        return true;
    }
}
