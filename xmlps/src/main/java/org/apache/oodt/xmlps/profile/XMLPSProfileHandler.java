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

package org.apache.oodt.xmlps.profile;

//OODT imports
import org.apache.oodt.xmlps.mapping.DatabaseTable;
import org.apache.oodt.xmlps.mapping.MappingReader;
import org.apache.oodt.xmlps.product.XMLPSProductHandler;
import org.apache.oodt.xmlps.profile.DBMSExecutor;
import org.apache.oodt.xmlps.queryparser.Expression;
import org.apache.oodt.xmlps.queryparser.HandlerQueryParser;

//JDK imports
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.profile.Profile;
import org.apache.oodt.profile.ProfileException;
import org.apache.oodt.profile.handlers.ProfileHandler;
import org.apache.oodt.xmlquery.QueryElement;
import org.apache.oodt.xmlquery.XMLQuery;

/**
 * 
 * <p>
 * An implementation of a {@link ProfileHandler} that extends the capabilities
 * of the {@link XMLPSProductHandler}, and uses the XML Specification defined
 * by the mapping file to represent its field mapping information.
 * </p>.
 */
public class XMLPSProfileHandler extends XMLPSProductHandler implements
        ProfileHandler {

    private DBMSExecutor executor;

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(XMLPSProfileHandler.class
            .getName());

    private String resLocationSpec;

    public XMLPSProfileHandler() throws InstantiationException {
        super(null);
        String mappingFilePath = System
                .getProperty("org.apache.oodt.xmlps.profile.xml.mapFilePath");

        if (mappingFilePath == null) {
            throw new InstantiationException(
                    "Need to specify path to xml mapping file!");
        }

        try {
            mapping = MappingReader.getMapping(mappingFilePath);
        } catch (Exception e) {
            throw new InstantiationException(
                    "Unable to parse profile mapping xml file: ["
                            + mappingFilePath + "]: reason: "
                            + e.getMessage());
        }

        // load the db properties file
                // if one exists: otherwise, don't bother and just print out the SQL to
        // the console.
        // 
        String dbPropFilePath = System
                .getProperty("org.apache.oodt.xmlps.profile.xml.dbPropFilePath");
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

        this.resLocationSpec = System
                .getProperty("org.apache.oodt.xmlps.profile.xml.resLocationSpec");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.profile.handlers.ProfileHandler#findProfiles(org.apache.oodt.xmlquery.XMLQuery)
     */
    public List<Profile> findProfiles(XMLQuery query) throws ProfileException {
        List<QueryElement> whereSet = query.getWhereElementSet();
        List<QueryElement> selectSet = query.getSelectElementSet();
        try {
            translateToDomain(selectSet, true);
            translateToDomain(whereSet, false);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProfileException(e.getMessage());
        }
        List<Profile> profs = queryAndPackageProfiles(query);
        return profs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.profile.handlers.ProfileHandler#get(java.lang.String)
     */
    public Profile get(String id) throws ProfileException {
        throw new ProfileException("Method not implemented!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.oodt.profile.handlers.ProfileHandler#getID()
     */
    public String getID() {
        return mapping.getId();
    }

    protected List<Profile> queryAndPackageProfiles(XMLQuery query) {
        Stack<QueryElement> queryStack = HandlerQueryParser
                .createQueryStack(query.getWhereElementSet());
        Expression parsedQuery = HandlerQueryParser.parse(queryStack,
                this.mapping);
        List<Profile> profs = null;

        StringBuffer sqlBuf = new StringBuffer("SELECT *");
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
                sqlBuf.append(tbl.getDefaultTableJoin());
                sqlBuf.append(".");
                sqlBuf.append(tbl.getDefaultTableJoinFieldName());
                sqlBuf.append(" ");
            }
        }

        if (parsedQuery != null) {
            sqlBuf.append(" WHERE ");
            sqlBuf.append(parsedQuery.evaluate());
        }

        LOG.log(Level.INFO, sqlBuf.toString());

        if (executor != null) {
            try {
                profs = executor.executeLocalQuery(this.mapping, sqlBuf
                        .toString(), this.resLocationSpec);

            } catch (SQLException e) {
                e.printStackTrace();
                LOG.log(Level.WARNING, "Error executing sql: ["
                        + sqlBuf.toString() + "]: Message: " + e.getMessage());
            }
        }

        return profs;
    }

    protected void translateToDomain(List<QueryElement> elemSet,
            boolean selectSet) throws Exception {
        super.translateToDomain(elemSet, selectSet);
    }

}
