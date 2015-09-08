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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;
import org.apache.oodt.cas.filemgr.util.SqlParser;

//APACHE imports
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A tool to return product ids given a {@link Query} against the File Manager.
 * </p>
 */
@Deprecated
public final class QueryTool {

    private static String freeTextBlock = "__FREE__";

    private FileManagerClient client = null;

    private static enum QueryType { LUCENE, SQL }; 
    
    /* our log stream */
    private static final Logger LOG = Logger.getLogger(QueryTool.class.getName());

    public QueryTool(URL fmUrl) throws InstantiationException {
        try {
            client = RpcCommunicationFactory.createClient(fmUrl);
        } catch (ConnectionException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    public static Query parseQuery(String query) {
        QueryParser parser;
        // note that "__FREE__" is a control work for free text searching
        parser = new QueryParser(freeTextBlock, new CASAnalyzer());
        Query luceneQ = null;
        try {
            luceneQ = (Query) parser.parse(query);
        } catch (ParseException e) {
            System.out.println("Error parsing query text.");
            System.exit(-1);
        }
        return luceneQ;
    }

    public List query(org.apache.oodt.cas.filemgr.structs.Query query) {
        List prodIds = new Vector();
        List products = new Vector();

        List productTypes = safeGetProductTypes();

        if (productTypes != null && productTypes.size() > 0) {
            for (Iterator i = productTypes.iterator(); i.hasNext();) {
                ProductType type = (ProductType) i.next();
                try {
                    products = client.query(query, type);
                    if (products != null && products.size() > 0) {
                        for (Iterator j = products.iterator(); j.hasNext();) {
                            Product product = (Product) j.next();
                            prodIds.add(product.getProductId());
                        }
                    }
                } catch (CatalogException e) {
                    LOG.log(Level.WARNING, "Exception querying for: ["
                            + type.getName() + "] products: Message: "
                            + e.getMessage());
                }

            }

        }

        return prodIds;

    }

    public void generateCASQuery(
            org.apache.oodt.cas.filemgr.structs.Query casQuery,
            Query luceneQuery) {
        if (luceneQuery instanceof TermQuery) {
            Term t = ((TermQuery) luceneQuery).getTerm();
            if (t.field().equals(freeTextBlock)) {
                // nothing for now
            } else {
                casQuery.addCriterion(new TermQueryCriteria(t.field(), 
                        t.text()));
            }
        } else if (luceneQuery instanceof PhraseQuery) {
            Term[] t = ((PhraseQuery) luceneQuery).getTerms();
            if (t[0].field().equals(freeTextBlock)) {
                // nothing for now
            } else {
                for (int i = 0; i < t.length; i++)
                    casQuery.addCriterion(new TermQueryCriteria(
                            t[i].field(), t[i].text()));
            }
        } else if (luceneQuery instanceof RangeQuery) {
            Term startT = ((RangeQuery) luceneQuery).getLowerTerm();
            Term endT = ((RangeQuery) luceneQuery).getUpperTerm();
            casQuery.addCriterion(new RangeQueryCriteria(startT
                    .field(), startT.text(), endT.text()));
        } else if (luceneQuery instanceof BooleanQuery) {
            BooleanClause[] clauses = ((BooleanQuery) luceneQuery).getClauses();
            for (int i = 0; i < clauses.length; i++) {
                generateCASQuery(casQuery, (clauses[i]).getQuery());
            }
        } else {
            throw new RuntimeException(
                    "Error parsing query! Cannot determine clause type: ["
                            + luceneQuery.getClass().getName() + "] !");
        }
    }

    private List safeGetProductTypes() {
        List prodTypes = null;

        try {
            prodTypes = client.getProductTypes();
        } catch (RepositoryManagerException e) {
            LOG.log(Level.WARNING,
                    "Error obtaining product types from file manager: ["
                            + client.getFileManagerUrl() + "]: Message: "
                            + e.getMessage());
        }

        return prodTypes;
    }

    public static void main(String[] args) throws Exception {
        String usage = "Usage: QueryTool [options] \n"
            + "options: \n"
            + "--url <fm url> \n"
            + "  Lucene like query options: \n"
            + "    --lucene \n"
            + "         -query <query> \n"
            + "  SQL like query options: \n"
            + "    --sql \n"
            + "         -query <query> \n"
            + "         -sortBy <metadata-key> \n"
            + "         -outputFormat <output-format-string> \n";
            		
        String fmUrlStr = null, queryStr = null, sortBy = null, outputFormat = null, delimiter = null;
        QueryType queryType = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--lucene")) {
                if (queryType != null)
                    exit("ERROR: Can only perform one query at a time! \n" + usage);
                if (args[++i].equals("-query"))
                    queryStr = args[++i];
                else 
                    exit("ERROR: Must specify a query! \n" + usage);
                queryType = QueryType.LUCENE;
            }else if (args[i].equals("--sql")) {
                if (queryType != null)
                    exit("ERROR: Can only perform one query at a time! \n" + usage);
                if (args[++i].equals("-query"))
                    queryStr = args[++i];
                else 
                    exit("ERROR: Must specify a query! \n" + usage);
                for (; i < args.length; i++) {
                    if (args[i].equals("-sortBy"))
                        sortBy = args[++i];
                    else if (args[i].equals("-outputFormat"))
                        outputFormat = args[++i];
                    else if (args[i].equals("-delimiter"))
                        delimiter = args[++i];
                }
                queryType = QueryType.SQL;
            }else if (args[i].equals("--url")) {
                fmUrlStr = args[++i];
            }
        }

        if (queryStr == null || fmUrlStr == null) 
            exit("Must specify a query and filemgr url! \n" + usage);
        
        if (queryType == QueryType.LUCENE) {
            URL fmUrl = new URL(fmUrlStr);
            QueryTool queryTool = new QueryTool(fmUrl);
            org.apache.oodt.cas.filemgr.structs.Query casQuery = new org.apache.oodt.cas.filemgr.structs.Query();
            queryTool.generateCASQuery(casQuery, parseQuery(queryStr));
    
            List prodIds = queryTool.query(casQuery);
            if (prodIds != null && prodIds.size() > 0) {
                for (Iterator i = prodIds.iterator(); i.hasNext();) {
                    String prodId = (String) i.next();
                    System.out.println(prodId);
                }
            }
        }else {
            System.out.println(performSqlQuery(queryStr, sortBy, outputFormat, delimiter != null ? delimiter : "\n", fmUrlStr));
        }

    }
    
    private static String performSqlQuery(String query, String sortBy, String outputFormat, String delimiter, String filemgrUrl) 
            throws MalformedURLException, CatalogException, ConnectionException, QueryFormulationException {
        ComplexQuery complexQuery = SqlParser.parseSqlQuery(query);
        complexQuery.setSortByMetKey(sortBy);
        complexQuery.setToStringResultFormat(outputFormat);
        List<QueryResult> results = RpcCommunicationFactory.createClient(new URL(filemgrUrl)).complexQuery(complexQuery);
        StringBuffer returnString = new StringBuffer("");
        for (QueryResult qr : results) 
            returnString.append(qr.toString() + delimiter);
        return returnString.substring(0, returnString.length() - delimiter.length());
    }
    
    private static void exit(String msg) {
        System.err.println(msg);
        System.exit(1);
    }
}
