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

package org.apache.oodt.xmlps.mapping;

//OODT imports

import org.apache.oodt.commons.xml.XMLUtils;
import org.apache.oodt.xmlps.exceptions.XmlpsException;
import org.apache.oodt.xmlps.mapping.funcs.MappingFunc;
import org.apache.oodt.xmlps.util.GenericCDEObjectFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

/**
 *
 * <p>
 * A static final reader class for reading {@link Mapping}s.
 * </p>
 * .
 */
public final class MappingReader implements MappingReaderMetKeys {

  private MappingReader() throws InstantiationException {
    throw new InstantiationException("Don't construct reader objects!");
  }

  public static Mapping getMapping(InputStream is) throws XmlpsException {
    Document mappingDoc = XMLUtils.getDocumentRoot(is);
    Mapping map = new Mapping();

    Element rootElem = mappingDoc.getDocumentElement();

    map.setId(rootElem.getAttribute("id"));
    map.setName(rootElem.getAttribute("name"));
    readTables(rootElem, map);
    readFields(rootElem, map);

    return map;

  }

  public static Mapping getMapping(URL mappingUrl) throws IOException, XmlpsException {
    return getMapping(mappingUrl.openStream());
  }

  public static Mapping getMapping(String filePath) throws FileNotFoundException, XmlpsException {
    return getMapping(new FileInputStream(filePath));

  }

  private static void readTables(Element rootElem, Mapping map)
      throws XmlpsException {
    Element tblsElem = XMLUtils.getFirstElement(TABLES_OUTER_TAG, rootElem);
    if (tblsElem == null) {
      throw new XmlpsException("Unable to parse mapping XML file: [" + map.getName()
          + "]: reason: no defined tables tag element!");

    }

    String defaultTbl = tblsElem.getAttribute("default");
    // make sure that the default attribute is set
    if (defaultTbl == null || (defaultTbl.equals(""))) {
      throw new XmlpsException("Unable to parse mapping XML file: [" + map.getName()
          + "]: reason: there needs to be a default table defined "
          + "by the \"default\" attribute!");
    }

    map.setDefaultTable(defaultTbl);

    // get a list of all the tables, and process them one by one
    NodeList tableNodes = tblsElem.getElementsByTagName(TABLE_TAG);
    if (tableNodes != null && tableNodes.getLength() > 0) {
      for (int i = 0; i < tableNodes.getLength(); i++) {
        Element tableElem = (Element) tableNodes.item(i);
        DatabaseTable tbl = readTable(tableElem);
        if (tbl.getDefaultTableJoin() == null || tbl.getDefaultTableJoin().isEmpty())
          tbl.setDefaultTableJoin(map.getDefaultTable());
        map.addTable(tbl.getName(), tbl);
      }
    }
  }

  private static DatabaseTable readTable(Element tableElem) {
    DatabaseTable tbl = new DatabaseTable();
    tbl.setJoinFieldName(tableElem.getAttribute(TABLE_ATTR_JOIN_FLD));
    tbl.setName(tableElem.getAttribute(TABLE_ATTR_NAME));
    tbl.setDefaultTableJoinFieldName(tableElem
        .getAttribute(TABLE_ATTR_BASE_TBL_JOIN_FLD));
    tbl.setDefaultTableJoin(tableElem
        .getAttribute(TABLE_ATTR_BASE_TBL_JOIN_TABLE));
    return tbl;
  }

  private static void readFields(Element rootElem, Mapping map) {
    NodeList fldNodes = rootElem.getElementsByTagName(FIELD_TAG);

    if (fldNodes != null && fldNodes.getLength() > 0) {
      for (int i = 0; i < fldNodes.getLength(); i++) {
        MappingField fld = readField((Element) fldNodes.item(i));
        if (fld.getTableName() == null || fld.getTableName().isEmpty())
          fld.setTableName(map.getDefaultTable());
        map.addField(fld.getName(), fld);
      }
    }

  }

  private static MappingField readField(Element fldElem) {
    MappingField field = new MappingField();
    field.setTableName(fldElem.getAttribute(FIELD_ATTR_TABLE));
    field.setName(fldElem.getAttribute(FIELD_ATTR_NAME));
    field
        .setType(fldElem.getAttribute(FIELD_ATTR_TYPE).equals("dynamic") ? FieldType.DYNAMIC
            : FieldType.CONSTANT);
    field.setString(Boolean.valueOf(fldElem.getAttribute(FIELD_ATTR_STRING)));
    field.setConstantValue(fldElem.getAttribute(FIELD_ATTR_VALUE));
    field.setDbName(fldElem.getAttribute(FIELD_ATTR_DBNAME));
    if (fldElem.getAttribute(FIELD_ATTR_SCOPE) != null
        && !fldElem.getAttribute(FIELD_ATTR_SCOPE).equals("")) {

      field
          .setScope(fldElem.getAttribute(FIELD_ATTR_SCOPE).equals("query") ? FieldScope.QUERY
              : FieldScope.RETURN);
    }

    field.setFuncs(getTranslateFuncs(fldElem));

    return field;

  }

  private static List<MappingFunc> getTranslateFuncs(Element fldElem) {
    Element translateElem = XMLUtils.getFirstElement(FIELD_TRANSLATE_TAG,
        fldElem);

    List<MappingFunc> funcs = new Vector<MappingFunc>();

    if (translateElem != null) {
      // check for func tags
      NodeList funcNodes = translateElem.getElementsByTagName(FUNC_TAG);
      if (funcNodes != null && funcNodes.getLength() > 0) {
        for (int i = 0; i < funcNodes.getLength(); i++) {
          Element funcElem = (Element) funcNodes.item(i);
          funcs.add(getFunc(funcElem));
        }
      }
    }

    return funcs;
  }

  private static MappingFunc getFunc(Element funcElem) {
    String funcClass = funcElem.getAttribute(FUNC_ATTR_CLASS);
    MappingFunc func = GenericCDEObjectFactory
        .getMappingFuncFromClassName(funcClass);

    if (func != null) {
      func.configure(getPropsFromElementAttrs(funcElem.getAttributes()));
    }

    return func;
  }

  private static Properties getPropsFromElementAttrs(NamedNodeMap map) {
    Properties props = new Properties();

    if (map != null) {
      for (int i = 0; i < map.getLength(); i++) {
        Node mapNode = map.item(i);
        props.setProperty(mapNode.getNodeName(), mapNode.getNodeValue());
      }
    }

    return props;
  }

}
