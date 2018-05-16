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


package org.apache.oodt.cas.filemgr.ingest;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.exceptions.IngestException;
import org.apache.oodt.cas.metadata.MetExtractor;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.cas.metadata.util.GenericMetadataObjectFactory;


//JDK imports
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A command line I/F for {@link Product} ingestion.
 * </p>.
 */
public class CmdLineIngester extends StdIngester {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(CmdLineIngester.class
            .getName());

    public CmdLineIngester(String serviceFactory) {
        super(serviceFactory);
    }

    /**
     * @param args
     * @throws org.apache.oodt.cas.filemgr.structs.exceptions.IngestException
     * @throws java.net.MalformedURLException
     */
    public static void main(String[] args) throws IOException, IngestException {
        String usage = CmdLineIngester.class.getName()
                + " --url <filemgr url> [options]\n"
                + "[--extractor <met extractor class name> <met conf file path>|"
                + "--metFile <met file path>]\n"
                + "--transferer <data transfer service factory>\n"
                + "[--file <full path> | --in reads list of files from STDIN]\n";

        String fmUrlStr = null, extractorClassName = null, filePath = null;
        String transferServiceFactory = null, metConfFilePath = null;
        String metFilePath = null;
        boolean readFromStdin = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--url")) {
                fmUrlStr = args[++i];
            } else if (args[i].equals("--extractor")) {
                extractorClassName = args[++i];
                metConfFilePath = args[++i];
            } else if (args[i].equals("--file")) {
                filePath = args[++i];
            } else if (args[i].equals("--in")) {
                readFromStdin = true;
            } else if (args[i].equals("--transferer")) {
                transferServiceFactory = args[++i];
            } else if (args[i].equals("--metFile")) {
                metFilePath = args[++i];
            }
        }

        if (fmUrlStr == null
                || ((extractorClassName == null || metConfFilePath == null) && metFilePath == null)
                || (metFilePath != null && (metConfFilePath != null || extractorClassName != null))
                || transferServiceFactory == null
                || (filePath == null && !readFromStdin)
                || (readFromStdin && metFilePath != null)
                || (readFromStdin && filePath != null)) {
            System.err.println(usage);
            System.exit(1);
        }

        CmdLineIngester ingester = new CmdLineIngester(transferServiceFactory);
        MetExtractor extractor;
        if (readFromStdin) {
            List<String> prods = readProdFilesFromStdin();
            extractor = GenericMetadataObjectFactory
                    .getMetExtractorFromClassName(extractorClassName);
            ingester.ingest(new URL(fmUrlStr), prods, extractor, new File(
                    metConfFilePath));
        } else {
            String productID;
            if (metFilePath != null) {
                productID = ingester.ingest(new URL(fmUrlStr), new File(
                        filePath), new SerializableMetadata(
                        new FileInputStream(metFilePath)));
            } else {
                extractor = GenericMetadataObjectFactory
                        .getMetExtractorFromClassName(extractorClassName);
                productID = ingester.ingest(new URL(fmUrlStr), new File(
                        filePath), extractor, new File(metConfFilePath));
            }

            System.out.println("Result: " + productID);
        }

        ingester.close();
    }

    private static List<String> readProdFilesFromStdin() {
        List<String> prodFiles = new Vector<String>();
        BufferedReader br;

        br = new BufferedReader(new InputStreamReader(System.in));

        String line = null;

        try {
            while ((line = br.readLine()) != null) {
                prodFiles.add(line);
            }
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error reading prod file: line: [" + line
                    + "]: Message: " + e.getMessage(), e);
        }

        return prodFiles;
    }
}
