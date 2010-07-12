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


package gov.nasa.jpl.oodt.cas.commons.database;

//JDK imports
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.sql.DataSource;

/**
 * 
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Abstract layer around a SQL script
 * </p>.
 */
public class SqlScript {

    public final static char QUERY_ENDS = ';';

    private File script;

    private DataSource ds;

    private boolean useBatch = true;

    private List statementList = null;

    /**
     * @param args
     * @throws SQLException
     */

    public SqlScript(String scriptFileName, DataSource ds) throws SQLException {
        script = new File(scriptFileName);
        statementList = new Vector();
        this.ds = ds;
    }

    public static void main(String[] args) {
        String usage = "SqlScript [options] </path/to/sql/file>\n"
                + "--user <user>\n" + "--pass <pass>\n" + "--url <jdbc url>\n"
                + "--driver <java class spec>\n";

        String user = null, pass = null, url = null, driver = null;
        String sqlScriptFilePath = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--user")) {
                user = args[++i];
            } else if (args[i].equals("--pass")) {
                pass = args[++i];
            } else if (args[i].equals("--driver")) {
                driver = args[++i];
            } else if (args[i].equals("--url")) {
                url = args[++i];
            } else if (!args[i].startsWith("--")) {
                sqlScriptFilePath = args[i];
            }
        }

        if (user == null || pass == null || url == null || driver == null
                || sqlScriptFilePath == null) {
            System.err.println(usage);
            System.exit(1);
        }

        DataSource ds = DatabaseConnectionBuilder.buildDataSource(user, pass,
                driver, url);

        try {
            SqlScript sqlScript = new SqlScript(sqlScriptFilePath, ds);
            sqlScript.loadScript();
            sqlScript.execute();

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void loadScript() throws IOException, SQLException {
        BufferedReader reader = new BufferedReader(new FileReader(script));
        String line;
        StringBuffer query = new StringBuffer();
        boolean queryEnds = false;

        while ((line = reader.readLine()) != null) {
            if (isComment(line))
                continue;
            queryEnds = checkStatementEnds(line);
            query.append(line);
            if (queryEnds) {
                statementList.add(query.toString());
                query.setLength(0);
            }
        }
    }

    public void execute() throws SQLException {
        if (useBatch) {
            doExecuteBatch();
        } else {
            if (statementList != null && statementList.size() > 0) {
                for (Iterator i = statementList.iterator(); i.hasNext();) {
                    String sqlStatement = (String) i.next();
                    doExecuteIndividual(sqlStatement);

                }

            }

        }
    }

    /**
     * @return the useBatch
     */
    public boolean isUseBatch() {
        return useBatch;
    }

    /**
     * @param useBatch
     *            the useBatch to set
     */
    public void setUseBatch(boolean useBatch) {
        this.useBatch = useBatch;
    }

    private boolean isComment(String line) {
        if ((line != null) && (line.length() > 0))
            return (line.charAt(0) == '#');
        return false;
    }

    private boolean checkStatementEnds(String s) {
        return (s.indexOf(QUERY_ENDS) != -1);
    }

    private void doExecuteIndividual(String sql) {
        Connection conn = null;
        Statement statement = null;

        try {
            conn = ds.getConnection();
            statement = conn.createStatement();

            statement.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Exception executing SQL: [" + sql
                    + "]: message: " + e.getMessage());

        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignore) {
                }

                conn = null;
            }
        }
    }

    private void doExecuteBatch() {
        Connection conn = null;
        Statement statement = null;

        try {
            if (statementList != null && statementList.size() > 0) {
                conn = ds.getConnection();
                statement = conn.createStatement();

                for (Iterator i = statementList.iterator(); i.hasNext();) {
                    String sqlStatement = (String) i.next();
                    statement.addBatch(sqlStatement);
                }

                statement.executeBatch();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out
                    .println("Exception executing SQL batch statement: message: "
                            + e.getMessage());

        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception ignore) {
                }

                statement = null;
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignore) {
                }

                conn = null;
            }
        }
    }

}
