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
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.apache.oodt.cas.pushpull.filerestrictions.Parser;
import org.apache.oodt.cas.pushpull.filerestrictions.VirtualFile;
import org.apache.oodt.cas.pushpull.filerestrictions.VirtualFileStructure;
import org.apache.oodt.cas.pushpull.exceptions.ParserException;
import org.apache.oodt.cas.pushpull.expressions.GlobalVariables;
import org.apache.oodt.cas.pushpull.expressions.Method;
import org.apache.oodt.cas.pushpull.expressions.Variable;
import org.apache.oodt.commons.xml.XMLUtils;

//JDK imports
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class DirStructXmlParser implements Parser {

    private static final Logger LOG = Logger.getLogger(DirStructXmlParser.class
            .getName());
    
    private static final HashMap<String, Method> methodRepo = new HashMap<String, Method>();

    public DirStructXmlParser() {}

    public VirtualFileStructure parse(FileInputStream xmlFile)
            throws ParserException {
        try {
            String initialCdDir = "/";
            VirtualFile root = null;
            NodeList list = (DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(new InputSource(xmlFile)))
                    .getDocumentElement().getChildNodes();
            VirtualFile currentFile = null;
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node.getNodeName().equals("dirstruct")) {

                    // parse out starting path
                    String startingPath = ((Element) node)
                            .getAttribute("starting_path");
                    if (startingPath != null) {
                        root = (currentFile = new VirtualFile(
                                initialCdDir = startingPath, true))
                                .getRootDir();
                        VirtualFile temp = currentFile.getParentFile();
                        while (temp != null) {
                            temp.setNoDirs(true);
                            temp.setNoFiles(true);
                            temp = temp.getParentFile();
                        }
                    } else {
                        currentFile = root = VirtualFile.createRootDir();
                    }

                    // parse the directory structure
                    parseDirstructXML(node.getChildNodes(), currentFile);

                } else if (node.getNodeName().equals("variables")) {
                    parseVariablesXML(node.getChildNodes());
                } else if (node.getNodeName().equals("methods")) {
                    parseMethodsXML(node.getChildNodes());
                }
            }
            return new VirtualFileStructure(initialCdDir, root);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParserException("Failed to parse XML file : "
                    + e.getMessage());
        }
    }

    private void parseDirstructXML(NodeList list, VirtualFile currentLoadFile) {
        for (int i = 0; i < list.getLength(); i++) {
            Node dir = list.item(i);
            if (dir.getNodeName().equals("dir")) {
                String dirName = replaceVariablesAndMethods(((Element) dir)
                        .getAttribute("name"));
                currentLoadFile.addChild(new VirtualFile(dirName, true));
                NodeList children;
                if ((children = dir.getChildNodes()).getLength() > 0) {
                    parseDirstructXML(children, currentLoadFile.getChild(
                            dirName, true));
                }
            } else if (dir.getNodeName().equals("nodirs")) {
                currentLoadFile.setNoDirs(true);
            } else if (dir.getNodeName().equals("nofiles")) {
                currentLoadFile.setNoFiles(true);
            } else if (dir.getNodeName().equals("file")) {
                VirtualFile vf = new VirtualFile(
                        replaceVariablesAndMethods(((Element) dir)
                                .getAttribute("name")), false);
                vf.setNoDirs(true);
                currentLoadFile.addChild(vf);
            }
        }
    }

    private String replaceVariablesAndMethods(String input) {
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
            case '$':
                try {
                    if (input.charAt(i + 1) == '{') {
                        StringBuffer variable = new StringBuffer("");
                        for (int j = i + 2; j < input.length(); j++) {
                            char ch = input.charAt(j);
                            if ((ch <= 'Z' && ch >= 'A')
                                    || (ch <= 'z' && ch >= 'a')
                                    || (ch <= '9' && ch >= '0') 
                                    || ch == '_')
                                variable.append(ch);
                            else
                                break;
                        }
                        Variable v = GlobalVariables.hashMap.get(variable
                                .toString());
                        if (v == null)
                        	throw new Exception("No variable defined with name '" + variable.toString() + "'");
                        input = input.replaceFirst("\\$\\{" + variable + "\\}", v.toString());
                        i = i + v.toString().length();
                    }
                } catch (Exception e) {
                	LOG.log(Level.WARNING, "Failed to replace variable in '" + input + " for i = '" + i + "' : " + e.getMessage(), e);
                }
                break;
            case '%':
                try {
                    StringBuffer method = new StringBuffer("");
                    int j = i + 1;
                    for (; j < input.length(); j++) {
                        char ch = input.substring(j, j + 1).charAt(0);
                        if ((ch <= 'Z' && ch >= 'A')
                                || (ch <= 'z' && ch >= 'a')
                                || (ch <= '9' && ch >= '0') || ch == '_')
                            method.append(ch);
                        else
                            break;
                    }

                    if (input.substring(j, j + 1).charAt(0) == '(') {
                        Method m = methodRepo.get(method.toString());
                        StringTokenizer st = new StringTokenizer(input
                                .substring(j, input.substring(j).indexOf(")")
                                        + j), "#\", ()");
                        while (st.hasMoreTokens()) {
                            String arg = st.nextToken();
                            m.addArg(null, arg);
                        }
                        String returnValue = m.execute().toString();
                        input = input.substring(0, i)
                                + returnValue
                                + input.substring(input.substring(i).indexOf(
                                        ")")
                                        + 1 + i);
                        i = i + returnValue.length();
                    } else {
                        LOG.log(Level.SEVERE, "Invalid method signature in "
                                + input + " near " + method);
                        break;
                    }
                } catch (Exception e) {
                }
                break;
            }
        }
        return input;
    }

    private void parseVariablesXML(NodeList list) throws DOMException, Exception {

        // loop through all variable elements
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);

            // parse variable element
            if (node.getNodeName().equals("variable")) {
                NodeList children = node.getChildNodes();

                // create Variable Object
                String variableName = ((Element) node).getAttribute("name");
                Variable variable = new Variable(variableName);

                // loop through to fill Variable
                String type = null, value = null;
                for (int j = 0; j < children.getLength(); j++) {
                    Node child = children.item(j);

                    // get the Variable's name
                    if (child.getNodeName().equals("type")) {
                        type = XMLUtils.getSimpleElementText((Element) child,
                                true).toLowerCase();

                        // get the Variable's value
                    } else if (child.getNodeName().equals("value")) {
                        value = PathUtils.doDynamicReplacement(XMLUtils
                                .getSimpleElementText((Element) child, false));

                        // get the Variable's value's precision infomation
                    } else if (child.getNodeName().equals("precision")) {
                        NodeList grandChildren = child.getChildNodes();
                        for (int k = 0; k < grandChildren.getLength(); k++) {
                            Node grandChild = grandChildren.item(k);
                            // get the precision
                            if (grandChild.getNodeName().equals("locations")) {
                                variable.setPrecision(Integer.parseInt(XMLUtils
                                        .getSimpleElementText((Element) grandChild, true)));
                                // get the fill character to meet the precision
                                // [optional]
                            } else if (grandChild.getNodeName().equals("fill")) {
                                variable.setFillString(
                                    XMLUtils.getSimpleElementText((Element) grandChild, false));
                                // get the side for which the fill character
                                // will be applied [optional]
                            } else if (grandChild.getNodeName().equals("side")) {
                                variable.setFillSide(
                                        (XMLUtils.getSimpleElementText((Element) grandChild, true)
                                            .toLowerCase().equals("front")) 
                                                ? variable.FILL_FRONT
                                                : variable.FILL_BACK);
                            }
                        }
                    }
                }
                // determine if variable is an Integer or a String
                if (type.equals("int")) {
                    variable.setValue(new Integer(value));
                } else
                    variable.setValue(value);

                // store Variable in list of Variables
                GlobalVariables.hashMap.put(variable.getName(), variable);
            }
        }
    }

    private void parseMethodsXML(NodeList list) {

        // loop though all method elements
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            // parse method element
            if (node.getNodeName().equals("method")) {
                NodeList children = node.getChildNodes();

                // create Method Object
                String methodName = ((Element) node).getAttribute("name");
                Method method = new Method(methodName);

                // loop through to fill Method Object
                for (int j = 0; j < children.getLength(); j++) {
                    Node child = children.item(j);

                    // get the Method's behavoir
                    if (child.getNodeName().equals("action")) {
                        method.setBehavoir(XMLUtils.getSimpleElementText(
                                (Element) child, false));

                        // get the Method's arguments
                    } else if (child.getNodeName().equals("args")) {
                        String name, argType = null;
                        NodeList grandChildren = child.getChildNodes();

                        // loop for every arg element
                        for (int k = 0; k < grandChildren.getLength(); k++) {
                            Node grandChild = grandChildren.item(k);

                            // parse arg element
                            if (grandChild.getNodeName().equals("arg")) {
                                name = ((Element) grandChild)
                                        .getAttribute("name");

                                // get arg element properties
                                NodeList greatGrandChildren = grandChild
                                        .getChildNodes();
                                for (int l = 0; l < greatGrandChildren
                                        .getLength(); l++) {
                                    Node greatGrandChild = greatGrandChildren
                                            .item(l);
                                    if (greatGrandChild.getNodeName().equals(
                                            "type")) {
                                        argType = XMLUtils.getSimpleElementText(
                                                (Element) greatGrandChild,
                                                true);
                                    }
                                }

                                // create argument signature in Method
                                method
                                        .addArgSignature(
                                                name,
                                                ((argType.toLowerCase()
                                                        .equals("int")) ? method.INT
                                                        : method.STRING));
                            }
                        }
                    }
                }

                // store Method in list of Methods
                methodRepo.put(method.getName(), method);
            }
        }
    }

}
