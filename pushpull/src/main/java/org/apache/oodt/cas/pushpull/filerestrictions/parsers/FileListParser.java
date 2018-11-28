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

//JDK imports
import java.io.FileInputStream;
import java.util.Scanner;


import org.apache.oodt.cas.metadata.Metadata;
//OODT imports
import org.apache.oodt.cas.pushpull.exceptions.ParserException;
import org.apache.oodt.cas.pushpull.filerestrictions.Parser;
import org.apache.oodt.cas.pushpull.filerestrictions.VirtualFile;
import org.apache.oodt.cas.pushpull.filerestrictions.VirtualFileStructure;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class FileListParser implements Parser {

	public FileListParser() {}
	
    public VirtualFileStructure parse(FileInputStream inputFile, Metadata metadata)
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
