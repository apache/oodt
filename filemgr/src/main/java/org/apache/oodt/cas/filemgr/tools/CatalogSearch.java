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

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author woollard
 * @version $Revision$
 * 
 * <p>
 * A command line-search tool for the File Manager.
 * </p>
 * 
 */
public class CatalogSearch {
    private static Logger LOG = Logger.getLogger(CatalogSearch.class.getName());

    private static FileManagerClient client;

    private static String freeTextBlock = "__FREE__";

    private static String productFilter = "";

    public CatalogSearch() {
    }

    public static void PostQuery(
            org.apache.oodt.cas.filemgr.structs.Query casQuery) {
        Vector products = new Vector();
        try {
            products = (Vector) client.getProductTypes();
        } catch (RepositoryManagerException e) {
            System.out
                    .println("Error getting available product types from the File Manager.");
            LOG.log(Level.SEVERE, e.getMessage());
        }
        for (Object product : products) {
            PostQuery(((ProductType) product).getProductTypeId(),
                casQuery);
        }
    }

    public static void PostQuery(String product,
            org.apache.oodt.cas.filemgr.structs.Query casQuery) {
        Vector results = new Vector();
        ProductType productType = null;

        try {
            productType = client.getProductTypeById(product);
        } catch (RepositoryManagerException e) {
            System.out.println("Could not access Product Type information");
            System.exit(-1);
        }

        try {
            results = (Vector) client.query(casQuery, productType);
        } catch (CatalogException ignore) {
            System.out.println("Error querying the File Manager");
            LOG.log(Level.SEVERE, ignore.getMessage());
            System.exit(-1);
        }

        if (results.isEmpty()) {
            System.out.println("No Products Found Matching This Criteria.");
        } else {
            System.out.println("Products Matching Query");
            for (Object result : results) {
                System.out.print(((Product) result).getProductName()
                                 + "\t");
                System.out.println(((Product) result).getProductId());
            }
        }
    }

    public static void setFilter(String filter) {
        productFilter = filter;
        try {
            client.getProductTypeById(filter);
        } catch (RepositoryManagerException e) {
            System.out.println("No product found with ID: " + filter);
            productFilter = "";
        }
        if (!productFilter.equals("")) {
            System.out.println("Filtering for " + productFilter + " products.");
        }
    }

    public static void removeFilter() {
        productFilter = "";
    }

    public static void ListProducts() {
        Vector products = new Vector();
        try {
            products = (Vector) client.getProductTypes();
        } catch (RepositoryManagerException e) {
            System.out
                    .println("Error getting available product types from the File Manager.");
            LOG.log(Level.SEVERE, e.getMessage());
        }
        for (Object product : products) {
            System.out.print(((ProductType) product).getProductTypeId()
                             + "\t");
            System.out.println(((ProductType) product).getName());
        }
    }

    public static void listElements() {
        Vector products;
        try {
            products = (Vector) client.getProductTypes();
            for (Object product : products) {
                listElements(((ProductType) product).getProductTypeId());
            }
        } catch (RepositoryManagerException e) {
            System.out
                    .println("Error getting available product types from the File Manager.");
            LOG.log(Level.SEVERE, e.getMessage());
        }

    }

    public static void listElements(String prodID) {
        Vector elements = new Vector();
        ProductType type;

        try {
            type = client.getProductTypeById(prodID);
            elements = (Vector) client.getElementsByProductType(type);
        } catch (RepositoryManagerException e1) {
            System.out.println("Could not find a ProductType with the ID: "
                    + prodID);
        } catch (ValidationLayerException e) {
            // TODO Auto-generated catch block
            LOG.log(Level.SEVERE, e.getMessage());
        }

        for (Object element : elements) {
            Element e = (Element) element;
            System.out.print(e.getElementId() + "\t");
            System.out.println(e.getElementName());
        }
    }

    public static void printHelp() {
        String add_filter = "add filter [productType]\n";
        add_filter += "\tAdd a filter on query results to only return products\n";
        add_filter += "\tmatching the specified productType.";

        String remove_filter = "remove filter\n";
        remove_filter += "\tRemove filters on query results.";

        String get_products = "get products\n";
        get_products += "\tReturns all ProductTypeIDs known to the Repository.";

        String get_elements = "get elements\n";
        get_elements += "\tReturns all Elements known to the Validation Layer.";

        String get_elements_for_product = "get elements for [productType]\n";
        get_elements_for_product += "\tReturns all Elements known to the ";
        get_elements_for_product += "Validation Layer\n\tfor the specified ";
        get_elements_for_product += "productType.";

        String query = "query [query]\n";
        query += "\tQueries the Catalog for all products matching the \n";
        query += "\tspecified query. If the filter is set, only products\n";
        query += "\tmatching the productType set in the filter will be\n";
        query += "\treturned. More details about query syntax can be found\n";
        query += "\tat http://lucene.apache.org/java/docs/queryparsersyntax.html";

        String quit = "quit\n";
        quit += "\tExits the program.";

        System.out.println("Available Commands:");
        System.out.println(add_filter);
        System.out.println(remove_filter);
        System.out.println(get_products);
        System.out.println(get_elements);
        System.out.println(get_elements_for_product);
        System.out.println(query);
        System.out.println(quit);
    }

    public static Query ParseQuery(String query) {
        // note that "__FREE__" is a control work for free text searching
        QueryParser parser = new QueryParser(freeTextBlock, new CASAnalyzer());
        Query luceneQ = null;
        try {
            luceneQ = (Query) parser.parse(query);
        } catch (ParseException e) {
            System.out.println("Error parsing query text.");
            System.exit(-1);
        }
        return luceneQ;
    }

    public static void GenerateCASQuery(
            org.apache.oodt.cas.filemgr.structs.Query casQuery,
            Query luceneQuery) {
        if (luceneQuery instanceof TermQuery) {
            Term t = ((TermQuery) luceneQuery).getTerm();
            if (!t.field().equals(freeTextBlock)) {
                casQuery
                        .addCriterion(new TermQueryCriteria(t.field(), t.text()));
            }
        } else if (luceneQuery instanceof PhraseQuery) {
            Term[] t = ((PhraseQuery) luceneQuery).getTerms();
            if (!t[0].field().equals(freeTextBlock)) {
                for (Term aT : t) {
                    casQuery.addCriterion(new TermQueryCriteria(aT.field(),
                        aT.text()));
                }
            }
        } else if (luceneQuery instanceof TermRangeQuery) {
            BytesRef startT = ((TermRangeQuery) luceneQuery).getLowerTerm();
            BytesRef endT = ((TermRangeQuery) luceneQuery).getUpperTerm();

            //TODO CHECK THIS RANGE!
            casQuery.addCriterion(new RangeQueryCriteria(((TermRangeQuery) luceneQuery).getField(), startT.utf8ToString(), endT.utf8ToString()));
        } else if (luceneQuery instanceof BooleanQuery) {
            List<BooleanClause> clauses = ((BooleanQuery) luceneQuery).clauses();
            for (BooleanClause clause : clauses) {
                GenerateCASQuery(casQuery, (clause).getQuery());
            }
        } else {
            System.out.println("Error Parsing Query");
            System.exit(-1);
        }
    }

    public static void CommandParser(String command) {
        StringTokenizer tok = new StringTokenizer(command, " ");
        int tokCount = tok.countTokens();

        if (tokCount > 0) {
            String com = tok.nextToken();
            if (com.equalsIgnoreCase("get")) {
                if (tokCount > 1) {
                    String subcom = tok.nextToken();
                    if (subcom.equalsIgnoreCase("products")) {
                        ListProducts();
                    } else if (subcom.equalsIgnoreCase("elements")) {
                        if (tokCount == 4
                                && tok.nextToken().equalsIgnoreCase("for")) {
                            String prodElements = tok.nextToken();
                            listElements(prodElements);
                        } else {
                            if (tokCount == 2) {
                                listElements();
                            } else {
                                System.out.println("Error parsing command");
                            }
                        }
                    }
                } else {
                    System.out.println("Error parsing command");
                }
            } else if (com.equalsIgnoreCase("add")) {
                if (tokCount == 3 && tok.nextToken().equalsIgnoreCase("filter")) {
                    setFilter(tok.nextToken());
                } else {
                    System.out.println("Error parsing command");
                }
            } else if (com.equalsIgnoreCase("remove")) {
                if (tokCount == 2 && tok.nextToken().equalsIgnoreCase("filter")) {
                    removeFilter();
                } else {
                    System.out.println("Error parsing command");
                }
            } else if (com.equalsIgnoreCase("help")) {
                printHelp();
            } else if (com.equalsIgnoreCase("exit")
                    || com.equalsIgnoreCase("quit")) {
                System.out.println("Exiting...");
                System.exit(0);
            } else if (com.equalsIgnoreCase("query")) {
                StringBuilder query = new StringBuilder();
                while (tok.hasMoreTokens()) {
                    query.append(tok.nextToken()).append(" ");
                }
                System.out.println("querying for: " + query);
                Query parsedQuery = ParseQuery(query.toString());
                org.apache.oodt.cas.filemgr.structs.Query casQuery = new org.apache.oodt.cas.filemgr.structs.Query();

                GenerateCASQuery(casQuery, parsedQuery);
                PostQuery(productFilter, casQuery);
            }
        }
    }

    public static void main(String[] args) {

        String fileManagerUrl = null;
        String welcomeMessage = "CatalogSearch v0.1\n";
        String usage = "CatalogSearch --url <url to File Manager service>\n";
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        // determine url
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--url")) {
                fileManagerUrl = args[++i];
            }
        }

        if (fileManagerUrl == null) {
            System.err.println(usage);
            System.exit(1);
        }

        // connect with Filemgr Client
        boolean clientConnect = true;
        try {
            client = RpcCommunicationFactory.createClient(new URL(fileManagerUrl));
        } catch (Exception e) {
            System.out
                    .println("Exception when communicating with file manager, errors to follow: message: "
                            + e.getMessage());
            clientConnect = false;
        }

        if (clientConnect) {

            System.out.println(welcomeMessage);

            String command;

            for (;;) {
                System.out.print("CatalogSearch>");

                try {
                    command = in.readLine();
                    CommandParser(command);

                } catch (IOException e) {
                    System.out.println("\nError Reading Query\n");
                    System.exit(-1);
                }
            }
        }
    }

}
