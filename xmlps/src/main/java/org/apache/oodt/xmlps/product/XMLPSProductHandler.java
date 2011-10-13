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

package org.apache.oodt.xmlps.product;

//OODT imports
import org.apache.oodt.product.ProductException;
import org.apache.oodt.product.QueryHandler;
import org.apache.oodt.xmlps.mapping.DatabaseTable;
import org.apache.oodt.xmlps.mapping.FieldScope;
import org.apache.oodt.xmlps.mapping.Mapping;
import org.apache.oodt.xmlps.mapping.MappingField;
import org.apache.oodt.xmlps.mapping.MappingReader;
import org.apache.oodt.xmlps.mapping.funcs.MappingFunc;
import org.apache.oodt.xmlps.queryparser.Expression;
import org.apache.oodt.xmlps.queryparser.HandlerQueryParser;
import org.apache.oodt.xmlps.structs.CDEResult;
import org.apache.oodt.xmlps.structs.CDERow;
import org.apache.oodt.xmlps.structs.CDEValue;
import org.apache.oodt.xmlps.util.XMLQueryHelper;
import org.apache.oodt.xmlquery.QueryElement;
import org.apache.oodt.xmlquery.Result;
import org.apache.oodt.xmlquery.XMLQuery;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * <p>
 * An XML configurable version of a Product Server that requires no code
 * to be written to plug into a local site's relational backend DBMS.
 * </p>.
 */
public class XMLPSProductHandler implements QueryHandler {

    /* our log stream */
    private static final Logger LOG = Logger
            .getLogger(XMLPSProductHandler.class.getName());

    protected Mapping mapping;

    private DBMSExecutor executor;

    protected XMLPSProductHandler(String phony) {
        /* this is to get around invoking the default constructor in sub-classes */
    }

    public XMLPSProductHandler() throws InstantiationException {
        String MappingFilePath = System
                .getProperty("org.apache.oodt.xmlps.xml.mapFilePath");

        if (MappingFilePath == null) {
            throw new InstantiationException(
                    "Need to specify path to xml mapping file!");
        }

        try {
            mapping = MappingReader.getMapping(MappingFilePath);
        } catch (Exception e) {
            throw new InstantiationException(
                    "Unable to parse mapping xml file: ["
                            + MappingFilePath + "]: reason: "
                            + e.getMessage());
        }

        /* load the db properties file */
        /*
         * if one exists: otherwise, don't bother and just print out the SQL to
         * the console.
         */
        String dbPropFilePath = System
                .getProperty("org.apache.oodt.xmlps.xml.dbPropFilePath");
        if (dbPropFilePath != null) {
            try {
                System.getProperties()
                        .load(new FileInputStream(dbPropFilePath));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                throw new InstantiationException(e.getMessage());
            }

            executor = new DBMSExecutor();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.oodt.product.QueryHandler#query(org.apache.oodt.xmlquery.XMLQuery)
     */
    public XMLQuery query(XMLQuery query) throws ProductException {
        List<QueryElement> whereSet = query.getWhereElementSet();
        List<QueryElement> selectSet = query.getSelectElementSet();
        try {
            translateToDomain(selectSet, true);
            translateToDomain(whereSet, false);
        } catch (Exception e) {
            LOG.severe(e.getMessage());
            throw new ProductException(e.getMessage());
        }

        queryAndPackageResults(query);

        return query;
    }

    public static void main(String[] args) throws Exception {
        String usage = "XMLPSProductHandler <query>\n";

        if (args.length != 1) {
            System.err.println(usage);
            System.exit(1);
        }

        XMLPSProductHandler handler = new XMLPSProductHandler();
        XMLQuery q = handler.query(XMLQueryHelper
                .getDefaultQueryFromQueryString(args[0]));
        System.out.println(q.getXMLDocString());
    }

    protected List<QueryElement> getElemNamesFromQueryElemSet(
            List<QueryElement> origSet) {
        if (origSet == null || (origSet != null && origSet.size() == 0))
            return null;

        List<QueryElement> newSet = new Vector<QueryElement>();

        for (Iterator<QueryElement> i = origSet.iterator(); i.hasNext();) {
            QueryElement elem = i.next();
            if (elem.getRole().equals(XMLQueryHelper.ROLE_ELEMNAME)
                    && !mapping.constantField(elem.getValue())) {
                newSet.add(elem);

            }

        }

        return newSet;

    }

    protected List<QueryElement> getConstElemNamesFromQueryElemSet(
            List<QueryElement> origSet) {
        if (origSet == null || (origSet != null && origSet.size() == 0))
            return null;

        List<QueryElement> newSet = new Vector<QueryElement>();

        for (Iterator<QueryElement> i = origSet.iterator(); i.hasNext();) {
            QueryElement elem = i.next();
            if (elem.getRole().equals(XMLQueryHelper.ROLE_ELEMNAME)
                    && mapping.constantField(elem.getValue())) {
                newSet.add(elem);
            }
        }

        return newSet;
    }

    protected void queryAndPackageResults(XMLQuery query) {
        Stack<QueryElement> queryStack = HandlerQueryParser
                .createQueryStack(query.getWhereElementSet());
        Expression parsedQuery = HandlerQueryParser.parse(queryStack,
                this.mapping);
        List<QueryElement> names = getElemNamesFromQueryElemSet(query
                .getSelectElementSet());

        String querySelectNames = toSQLSelectColumns(names);

        StringBuffer sqlBuf = new StringBuffer("SELECT ");
        sqlBuf.append(querySelectNames);
        sqlBuf.append(" FROM ");
        sqlBuf.append(mapping.getDefaultTable());
        sqlBuf.append(" ");

        if (mapping.getNumTables() > 0) {
            for (Iterator<String> i = mapping.getTableNames().iterator(); i
                    .hasNext();) {
                String tableName = i.next();
                DatabaseTable tbl = mapping.getTableByName(tableName);
                sqlBuf.append("INNER JOIN ");
                sqlBuf.append(tbl.getName());
                sqlBuf.append(" ON ");
                sqlBuf.append(tbl.getName());
                sqlBuf.append(".");
                sqlBuf.append(tbl.getJoinFieldName());
                sqlBuf.append(" = ");
                sqlBuf.append((tbl.getDefaultTableJoin() != null &&
                        !tbl.getDefaultTableJoin().equals("")) ? tbl
                        .getDefaultTableJoin() : mapping.getDefaultTable());
                sqlBuf.append(".");
                sqlBuf.append(tbl.getDefaultTableJoinFieldName());
                sqlBuf.append(" ");
            }
        }

        if(parsedQuery != null){
            sqlBuf.append(" WHERE ");
            sqlBuf.append(parsedQuery.evaluate());
        }

        LOG.log(Level.INFO, sqlBuf.toString());

        if (executor != null) {
            try {
                CDEResult res = executor.executeLocalQuery(this.mapping,
                        sqlBuf.toString(), toSQLResultSetColumns(names));

                res = addConstFields(res,
                        getConstElemNamesFromQueryElemSet(query
                                .getSelectElementSet()));

                if (res.getRows() == null
                        || (res.getRows() != null && res.getRows().size() == 0)) {
                  LOG.log(Level.WARNING, "Query retrieved no results.");
                }
                
                Result r = res.toResult();
                if (r != null) {
                    query.getResults().add(r);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                LOG.log(Level.WARNING, "Error executing sql: ["
                        + sqlBuf.toString() + "]: Message: " + e.getMessage());
            }
        }

    }

    private CDEResult addConstFields(CDEResult res,
            List<QueryElement> constFlds) {
        CDEResult newRes = res;
        if (constFlds != null && constFlds.size() > 0) {
            if (res == null
                    || (res != null && (res.getRows() == null || (res.getRows() != null && res
                            .getRows().size() == 0)))) {
                newRes = new CDEResult();
                // add one row
                newRes.getRows().add(new CDERow());
            }

            for (Iterator<QueryElement> i = constFlds.iterator(); i.hasNext();) {
                QueryElement elem = i.next();
                MappingField fld = this.mapping.getFieldByLocalName(elem
                        .getValue());

                for (Iterator<CDERow> j = newRes.getRows().iterator(); j
                        .hasNext();) {
                    CDERow row = j.next();
                    CDEValue val = new CDEValue(fld.getName(), fld
                            .getConstantValue());
                    row.getVals().add(val);
                }
            }
        }

        return newRes;

    }

    private String toSQLSelectColumns(List<QueryElement> elems) {
        if (elems == null || (elems != null && elems.size() == 0))
            return null;

        StringBuilder buf = new StringBuilder();
        for (QueryElement qe : elems) {
            MappingField fld = this.mapping.getFieldByLocalName(qe.getValue());
            if (fld != null) {
                buf.append(fld.getLocalName());
                buf.append(" as ");
                buf.append(fld.getName());
                buf.append(",");
            }
        }

        buf.deleteCharAt(buf.length() - 1);

        return buf.toString();
    }

    private List<String> toSQLResultSetColumns(List<QueryElement> elems) {
        if (elems == null || (elems != null && elems.size() == 0))
          return null;

        List<String> resultSetNames = new ArrayList<String>();
        for (QueryElement qe : elems) {
            MappingField fld = this.mapping.getFieldByLocalName(qe.getValue());
            if (fld != null) {
                resultSetNames.add(fld.getName());
            }
        }

        return resultSetNames;
    }

    protected void translateToDomain(List<QueryElement> elemSet,
            boolean selectSet) throws Exception {
        // go through each query element: use the mapping fields
        // to translate the names

        for (Iterator<QueryElement> i = elemSet.iterator(); i.hasNext();) {
            QueryElement elem = i.next();
            if (elem.getRole().equals(XMLQueryHelper.ROLE_ELEMNAME)) {
                // do the translation
                String elemValue = elem.getValue();
                MappingField fld = this.mapping.getFieldByName(elemValue);
                // make sure fld is not null
                if (fld == null) {
                    continue;
                }

                // make sure scope is null, or if it's not null, then it's
                // FieldScope.QUERY

                if (fld.getScope() != null
                        && fld.getScope().equals(FieldScope.RETURN)) {
                    // skip
                    continue;
                }

                // check to see if it has a dbname attr, if not, then the name
                // stays
                // the same
                String newFldName = fld.getLocalName();

                elem.setValue(newFldName);

                // now translate the domain vocab if there are translate funcs
                // present and this isn't the select set

                if (!selectSet && fld.getFuncs() != null
                        && fld.getFuncs().size() > 0) {
                    // the next query element should be
                    // XMLQueryHelper.ROLE_LITERAL
                    if (!i.hasNext())
                        break;
                    QueryElement litElem = i.next();
                    if (!litElem.getRole().equals(XMLQueryHelper.ROLE_LITERAL)) {
                        throw new Exception("next query element not "
                                + XMLQueryHelper.ROLE_LITERAL + "! role is "
                                + litElem.getRole() + " instead!");
                    }

                    for (Iterator<MappingFunc> j = fld.getFuncs().iterator(); j
                            .hasNext();) {
                        MappingFunc func = j.next();
                        CDEValue origVal = new CDEValue(fld.getName(),
                                litElem.getValue());
                        CDEValue newVal = func.translate(origVal);
                        litElem.setValue(newVal.getVal());
                    }

                }

            }
        }

    }

}
