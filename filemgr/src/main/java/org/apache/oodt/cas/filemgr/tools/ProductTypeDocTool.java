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

//JDK imports
import java.io.File;
import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

//APACHE imports
import org.apache.commons.io.FileUtils;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A tool to output HTML documentation for {@link ProductType} policy xml files.
 * </p>.
 */
public final class ProductTypeDocTool {

    private String xslFilePath;

    private String outputDirPath;

    public ProductTypeDocTool(String xslFilePath, String outputDirPath) {
        this.xslFilePath = xslFilePath;
        this.outputDirPath = outputDirPath;
        if (!this.outputDirPath.endsWith("/")) {
            this.outputDirPath += "/";
        }
    }

    public void doProductTypeDoc(String productTypeXmlFilePath,
            String elementXmlFilePath) throws IOException, TransformerException {
        // copy element xml to current path
        FileUtils.copyFileToDirectory(new File(elementXmlFilePath), new File(
                "."));
        // copy product type xsl to current path
        FileUtils.copyFileToDirectory(new File(xslFilePath), new File("."));

        String xslLocalFilePath = new File(".").getAbsolutePath();
        if (!xslLocalFilePath.endsWith("/")) {
            xslLocalFilePath += "/";
        }
        xslLocalFilePath += new File(xslFilePath).getName();

        String elementLocalFilePath = new File(".").getAbsolutePath();
        if (!elementLocalFilePath.endsWith("/")) {
            elementLocalFilePath += "/";
        }

        elementLocalFilePath += new File(elementXmlFilePath).getName();

        Transformer xformer = TransformerFactory.newInstance().newTransformer(
                new StreamSource(new File(xslLocalFilePath)));

        xformer.setOutputProperty(OutputKeys.INDENT, "yes");
        String productTypeFileName = new File(productTypeXmlFilePath).getName();
        String outputProductTypeDocFileName = productTypeFileName.replaceAll(
                "xml", "html");

        Result result = new StreamResult(new File(outputDirPath
                + outputProductTypeDocFileName));

        xformer.transform(new StreamSource(new File(productTypeXmlFilePath)),
                result);

        // now cleanup
        new File(xslLocalFilePath).delete();
        new File(elementLocalFilePath).delete();
    }

    public static void main(String[] args) throws IOException, TransformerException {
        String productTypeXmlFilePath = null, xslFilePath = null, outputDirPath = null, elementXmlFilePath = null;
        String usage = "ProductTypeDocTool --productTypeXml <path> "
                + "--elementXml <path> --xsl <path> --out <dir path>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--productTypeXml")) {
                productTypeXmlFilePath = args[++i];
            } else if (args[i].equals("--xsl")) {
                xslFilePath = args[++i];
            } else if (args[i].equals("--out")) {
                outputDirPath = args[++i];
            } else if (args[i].equals("--elementXml")) {
                elementXmlFilePath = args[++i];
            }
        }

        if (productTypeXmlFilePath == null || xslFilePath == null
                || outputDirPath == null || elementXmlFilePath == null) {
            System.err.println(usage);
            System.exit(1);
        }

        ProductTypeDocTool tool = new ProductTypeDocTool(xslFilePath,
                outputDirPath);
        tool.doProductTypeDoc(productTypeXmlFilePath, elementXmlFilePath);
    }

}
