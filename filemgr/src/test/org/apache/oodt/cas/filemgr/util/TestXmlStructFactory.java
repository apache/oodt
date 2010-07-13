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


package gov.nasa.jpl.oodt.cas.filemgr.util;

//OODT imports
import gov.nasa.jpl.oodt.cas.filemgr.structs.ProductType;
import gov.nasa.jpl.oodt.cas.commons.exec.EnvUtilities;

//JDK imports
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//Junit imports
import junit.framework.TestCase;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test Cases for the Xml Struct Factory.
 * </p>.
 */
public class TestXmlStructFactory extends TestCase {

    private static final String origRepoPath = "[HOME]/some/path";

    private static final String HOME = EnvUtilities.getEnv("HOME");

    private static final String expectedRepoPath = HOME + "/some/path";

    public void testEnvVarReplace() {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document document = null;

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();

            Element productTypeElem = (Element) document.createElement("type");
            productTypeElem.setAttribute("id", "blah");
            productTypeElem.setAttribute("name", "foo");

            Element repoPathElem = (Element) document
                    .createElement("repository");
            repoPathElem.setAttribute("path", origRepoPath);
            productTypeElem.appendChild(repoPathElem);
            
            Element versionerClassElem = (Element)
               document.createElement("versioner");
            versionerClassElem.setAttribute("class", "foo.bar");
            productTypeElem.appendChild(versionerClassElem);
            
            Element descElem = (Element)
               document.createElement("description");
            descElem.appendChild(document.createTextNode("foo"));
            productTypeElem.appendChild(descElem);

            ProductType type = XmlStructFactory.getProductType(productTypeElem);
            assertEquals("The value of the repo path: ["
                    + type.getProductRepositoryPath()
                    + "] was not the expected value of: [" + expectedRepoPath
                    + "]", type.getProductRepositoryPath(), expectedRepoPath);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
