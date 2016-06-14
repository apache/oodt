/**
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
package org.apache.oodt.opendapps.util;

//JDK imports
import java.io.StringWriter;
import java.util.List;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

//OODT imports
import org.apache.oodt.profile.Profile;

/**
 * Utility class to serialize a list of profiles to an XML document.
 * <p/>
 * Based on functionality already contained in the OODT grid ProfileQueryServlet
 * class, but separated as a stand alone utility to reduce dependencies.
 * 
 * @author Luca Cinquini
 * 
 */
public class ProfileSerializer {

  /**
   * Function to serialize a list of {@link Profile}s to XML.
   * 
   * @param profiles
   * @return
   * @throws TransformerException
   */
  public static String toXML(final List<Profile> profiles)
      throws TransformerException {

    final StringWriter writer = new StringWriter();
    writer.append("<?xml version='1.0' encoding='UTF-8'?>");
    writer.append("<!DOCTYPE profiles PUBLIC '" + Profile.PROFILES_DTD_FPI + "' '").append(Profile.PROFILES_DTD_URL)
          .append("'>");
    writer.append("<profiles>");

    final Transformer transformer = createTransformer();
    final Document doc = Profile.createProfileDocument();
    for (final Profile profile : profiles) {

      Node profileNode = profile.toXML(doc);
      DOMSource source = new DOMSource(profileNode);
      StreamResult result = new StreamResult(writer);
      transformer.transform(source, result);

    }

    writer.append("</profiles>");

    return writer.toString();

  }

  /**
   * Create a transformer, properly configured for XML text serialization.
   * 
   * @return a <code>Transformer</code> value.
   * @throws TransformerException
   *           if an error occurs.
   */
  private static Transformer createTransformer() throws TransformerException {

    Transformer transformer;
    synchronized (TRANSFORMER_FACTORY) {
      transformer = TRANSFORMER_FACTORY.newTransformer();
    }

    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
        "4");

    return transformer;
  }

  /** Sole transformer factory this class will ever need. */
  private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory
      .newInstance();

}
