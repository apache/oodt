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

package org.apache.oodt.cas.filemgr.tools;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;
import org.apache.oodt.commons.xml.XMLUtils;

//JDK imports
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A simple tool to write out a .met {@link Metadata} file for a specified
 * {@link Product}.
 * </p>.
 */
public final class MetadataDumper {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(MetadataDumper.class
            .getName());

    /* our file manager client */
    private FileManagerClient fmClient = null;

    private final static String FILENAME = "Filename";

    private final static String PRODUCT_NAME = "CAS.ProductName";

    public MetadataDumper(String fmUrlStr) throws InstantiationException {
        try {
            this.fmClient = RpcCommunicationFactory.createClient(new URL(fmUrlStr));
        } catch (MalformedURLException e) {
            LOG.log(Level.SEVERE, "malformed file manager url: [" + fmUrlStr
                    + "]", e);
            throw new InstantiationException(e.getMessage());
        } catch (ConnectionException e) {
            LOG.log(Level.SEVERE, "unable to connect to file manager: ["
                    + fmUrlStr + "]", e);
            throw new InstantiationException(e.getMessage());
        }
    }

    private Metadata getMetadata(String productId) {
        Product product;

        try {
            product = this.fmClient.getProductById(productId);
        } catch (Exception e) {
            throw new RuntimeException("Unable to retrieve product:["
                    + productId + "] by id");
        }

        Metadata met;

        try {
            met = this.fmClient.getMetadata(product);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get metadata for product: ["
                    + product.getProductName() + "]");
        }

        return met;
    }

    private void writeMetFileToDir(Metadata met, String fullMetFilePath) {
        try {
            XMLUtils.writeXmlFile(new SerializableMetadata(met).toXML(), fullMetFilePath);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Met file not generated: reason: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Dumps the {@link Metadata} from the {@link Product} identified by the
     * given <code>productId</code>. The {@link Metadata} is written to the
     * local directory <code>.</code>, where this program was invoked from.
     * 
     * @param productId
     *            The string identifier of the product to dump {@link Metadata}
     *            from.
     */
    public void dumpMetadata(String productId) {
        dumpMetadata(productId, new File(".").getAbsolutePath());
    }

    /**
     * Dumps the {@link Metadata} from the {@link Product} identified by the
     * given <code>productId</code>. The {@link Metadata} is written to the
     * specified <code>outDirPath</code>.
     * 
     * @param productId
     *            The string identifier of the product to dump {@link Metadata}
     *            from.
     * @param outDirPath
     *            The path on the local filesystem to write the {@link Metadata}
     *            file to.
     */
    public void dumpMetadata(String productId, String outDirPath) {
        Metadata met = getMetadata(productId);
        String fullMetFilePath = outDirPath;
        fullMetFilePath = (fullMetFilePath.endsWith("/")) ? fullMetFilePath
                : fullMetFilePath + "/";
        String filename = met.getMetadata(FILENAME) != null ? met
                .getMetadata(FILENAME) : met.getMetadata(PRODUCT_NAME);
        fullMetFilePath += filename + ".met";
        writeMetFileToDir(met, fullMetFilePath);
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws InstantiationException {
        String fileManagerUrlStr = null, productId = null, outDirPath = null;
        String usage = "MetadataDumper --url <filemgr url> --productId <id> [--out <dir path>]\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--url")) {
                fileManagerUrlStr = args[++i];
            } else if (args[i].equals("--productId")) {
                productId = args[++i];
            } else if (args[i].equals("--out")) {
                outDirPath = args[++i];
            }
        }

        if (fileManagerUrlStr == null || productId == null) {
            System.err.println(usage);
            System.exit(1);
        }

        MetadataDumper dumper = new MetadataDumper(fileManagerUrlStr);
        if (outDirPath != null) {
            dumper.dumpMetadata(productId, outDirPath);
        } else {
            dumper.dumpMetadata(productId);
        }
    }

}
