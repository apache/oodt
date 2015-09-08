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

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.RangeQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.system.FileManagerClient;

//JDK imports
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

//Lucene imports
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;

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

    private static QueryParser parser;

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
            e.printStackTrace();
        }
        for (int i = 0; i < products.size(); i++) {
            PostQuery(((ProductType) products.get(i)).getProductTypeId(),
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
            ignore.printStackTrace();
            System.exit(-1);
        }

        if (results.isEmpty()) {
            System.out.println("No Products Found Matching This Criteria.");
        } else {
            System.out.println("Products Matching Query");
            for (int i = 0; i < results.size(); i++) {
                System.out.print(((Product) results.get(i)).getProductName()
                        + "\t");
                System.out.println(((Product) results.get(i)).getProductId());
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
        if (!productFilter.equals(""))
            System.out.println("Filtering for " + productFilter + " products.");
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
            e.printStackTrace();
        }
        for (int i = 0; i < products.size(); i++) {
            System.out.print(((ProductType) products.get(i)).getProductTypeId()
                    + "\t");
            System.out.println(((ProductType) products.get(i)).getName());
        }
    }

    public static void listElements() {
        Vector products = new Vector();
        try {
            products = (Vector) client.getProductTypes();
            for (int i = 0; i < products.size(); i++) {
                listElements(((ProductType) products.get(i)).getProductTypeId());
            }
        } catch (RepositoryManagerException e) {
            System.out
                    .println("Error getting available product types from the File Manager.");
            e.printStackTrace();
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
            e.printStackTrace();
        }

        for (int i = 0; i < elements.size(); i++) {
            Element e = (Element) elements.get(i);
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

    public static void GenerateCASQuery(
            org.apache.oodt.cas.filemgr.structs.Query casQuery,
            Query luceneQuery) {
        if (luceneQuery instanceof TermQuery) {
            Term t = ((TermQuery) luceneQuery).getTerm();
            if (t.field().equals(freeTextBlock)) {
                // if(casQuery.getCriteria().isEmpty())
                // casQuery.addCriterion(new FreeTextQueryCriteria());
                // ((FreeTextQueryCriteria)casQuery.getCriteria().get(0)).addValue(t.text());
            } else {
                casQuery
                        .addCriterion(new TermQueryCriteria(t.field(), t.text()));
            }
        } else if (luceneQuery instanceof PhraseQuery) {
            Term[] t = ((PhraseQuery) luceneQuery).getTerms();
            if (t[0].field().equals(freeTextBlock)) {
                // if(casQuery.getCriteria().isEmpty())
                // casQuery.addCriterion(new FreeTextQueryCriteria());
                // for(int i=0;i<t.length;i++)
                // ((FreeTextQueryCriteria)casQuery.getCriteria().get(0)).addValue(t[i].text());
            } else {
                for (int i = 0; i < t.length; i++)
                    casQuery.addCriterion(new TermQueryCriteria(t[i].field(),
                            t[i].text()));
            }
        } else if (luceneQuery instanceof RangeQuery) {
            Term startT = ((RangeQuery) luceneQuery).getLowerTerm();
            Term endT = ((RangeQuery) luceneQuery).getUpperTerm();
            casQuery.addCriterion(new RangeQueryCriteria(startT.field(), startT
                    .text(), endT.text()));
        } else if (luceneQuery instanceof BooleanQuery) {
            BooleanClause[] clauses = ((BooleanQuery) luceneQuery).getClauses();
            for (int i = 0; i < clauses.length; i++) {
                GenerateCASQuery(casQuery, (clauses[i]).getQuery());
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
                                return;
                            }
                        }
                    }
                } else {
                    System.out.println("Error parsing command");
                    return;
                }
            } else if (com.equalsIgnoreCase("add")) {
                if (tokCount == 3 && tok.nextToken().equalsIgnoreCase("filter")) {
                    setFilter(tok.nextToken());
                } else {
                    System.out.println("Error parsing command");
                    return;
                }
            } else if (com.equalsIgnoreCase("remove")) {
                if (tokCount == 2 && tok.nextToken().equalsIgnoreCase("filter")) {
                    removeFilter();
                } else {
                    System.out.println("Error parsing command");
                    return;
                }
            } else if (com.equalsIgnoreCase("help")) {
                printHelp();
            } else if (com.equalsIgnoreCase("exit")
                    || com.equalsIgnoreCase("quit")) {
                System.out.println("Exiting...");
                System.exit(0);
            } else if (com.equalsIgnoreCase("query")) {
                String query = new String();
                while (tok.hasMoreTokens()) {
                    query += tok.nextToken() + " ";
                }
                System.out.println("querying for: " + query);
                Query parsedQuery = ParseQuery(query);
                org.apache.oodt.cas.filemgr.structs.Query casQuery = new org.apache.oodt.cas.filemgr.structs.Query();

                GenerateCASQuery(casQuery, parsedQuery);
                PostQuery(productFilter, casQuery);
            }
        }
    }

    public static void main(String[] args) {

        String fileManagerUrl = null;
        String welcomeMessage = "CatalogSearch v0.1\n";
        welcomeMessage += "Copyright 2006. California Institute of Technology.\n";
        String usage = "CatalogSearch --url <url to File Manager service>\n";
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        CatalogSearch cs = new CatalogSearch();

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

            String command = "";

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
