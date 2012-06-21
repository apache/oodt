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
package org.apache.oodt.cas.pge.writers;

//JDK imports
import static java.lang.Boolean.parseBoolean;

//JDK imports
import java.io.File;
import java.util.logging.Logger;

//JavaX imports.
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.SerializableMetadata;

/**
 * XSL Transformation class which writes Science PGE config files based from the
 * XML format of SerializableMetadata.
 *
 * @author bfoster (Brian Foster)
 */
public class XslTransformWriter implements DynamicConfigFileWriter {

   @Override
   public File generateFile(String filePath, Metadata metadata, Logger logger,
         Object... args) throws Exception {
      File file = new File(filePath);

      String xsltFilePath = (String) args[0];
      Source xsltSource = new StreamSource(new File(xsltFilePath));
      Result result = new StreamResult(file);

      TransformerFactory transFact = TransformerFactory.newInstance();
      Transformer trans = transFact.newTransformer(xsltSource);
      boolean useCDATA = args.length > 1 ? parseBoolean((String) args[1])
            : false;
      Source xmlSource = new DOMSource((new SerializableMetadata(metadata,
            trans.getOutputProperty(OutputKeys.ENCODING), useCDATA)).toXML());

      trans.setOutputProperty(OutputKeys.INDENT, "yes");
      trans.transform(xmlSource, result);

      return file;
   }
}
