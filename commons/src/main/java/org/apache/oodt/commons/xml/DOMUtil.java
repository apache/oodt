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


package org.apache.oodt.commons.xml;

import org.apache.oodt.commons.exceptions.CommonsException;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * 
 * <p>
 * This class was adapted from an O'Reilly site on DOM utilities. It contains a
 * few helper methods to make extracting out XML text from DOM representations a
 * little easier.
 * </p>
 * 
 * @author mattmann
 * @version 1.0
 */
public class DOMUtil {

    /**
     * 
     * <p>
     * Method returns the First occurence of Element 'name' in the DOM Node
     * 'element'.
     * </p>
     * 
     * @param element
     *            The DOM Element node to traverse.
     * @param name
     *            The XML name of the Element to return.
     * @return Element "element" with Name "name"'s first occurence.
     */
    public static Element getFirstElement(Element element, String name)
        throws CommonsException {
        NodeList n1 = element.getElementsByTagName(name);

        if (n1.getLength() < 1) {
            throw new CommonsException("Element: " + element + " does not contain: "
                    + name);
        }

        return (Element) n1.item(0);
    }

    /**
     * *
     * <p>
     * This function is intended when you have a DOM element with no other DOM
     * elements inside (i.e. <Tag><Tag2>here is text</Tag2></Tag>)
     * </p> *
     * 
     * @param node
     *            The DOM 'SimpleElement' as defined in the Function definition.
     * @param name
     *            The name of the Text to retreive.
     * @return the Text inbetween the simple element tags.
     */
    public static String getSimpleElementText(Element node, String name)
        throws CommonsException {
        Element namedElement = getFirstElement(node, name);
        return getSimpleElementText(namedElement);
    }

    /**
     * *
     * <p>
     * This function is intended for use when you have merely text between an
     * XML Element (i.e. <Tag>text here</Tag>).
     * </p>
     * 
     * @param node
     *            The DOM XML Tag, with text inbetween.
     * @return String text inbetween the simple element tag.
     */
    public static String getSimpleElementText(Element node) {
        StringBuilder sb = new StringBuilder();
        NodeList children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Text) {
                sb.append(child.getNodeValue());
            }
        }

        return sb.toString();
    }
}
