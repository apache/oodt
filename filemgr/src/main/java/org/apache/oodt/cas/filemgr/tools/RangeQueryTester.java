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
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//Lucene imports
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Utility command line program to test RangeQueries against an underlying
 * Lucene-based Product Catalog.
 * </p>
 * 
 */
public final class RangeQueryTester {

    private String startFieldName = null;

    private String endFieldName = null;

    private String startFieldStartValue = null;

    private String startFieldEndValue = null;

    private String endFieldStartValue = null;

    private String endFieldEndValue = null;

    private String indexPath = null;

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(RangeQueryTester.class
            .getName());

    DirectoryReader reader;
    /**
     * 
     */
    public RangeQueryTester() {
    }

    public List doRangeQuery(String productTypeId) {
        List products = null;
        IndexSearcher searcher = null;
        try {
             reader = DirectoryReader.open(FSDirectory.open(Paths.get(this.indexPath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            searcher = new IndexSearcher(reader);

            // construct a Boolean query here
            BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

            // add the product type as the first clause
            TermQuery prodTypeTermQuery = new TermQuery(new Term(
                    "product_type_id", productTypeId));
            booleanQuery.add(prodTypeTermQuery, BooleanClause.Occur.MUST);

            Term startFieldStartTerm = null, startFieldEndTerm = null;

            if (this.startFieldStartValue != null) {
                startFieldStartTerm = new Term(this.startFieldName,
                        this.startFieldStartValue);
            }

            if (this.startFieldEndValue != null) {
                startFieldEndTerm = new Term(this.startFieldName,
                        this.startFieldEndValue);
            }

            TermRangeQuery query1 = new TermRangeQuery(startFieldEndTerm.field(),startFieldStartTerm.bytes(),
                    startFieldEndTerm.bytes(), true, true);
            booleanQuery.add(query1, BooleanClause.Occur.MUST);

            if (this.endFieldName != null
                    && (this.endFieldStartValue != null || this.endFieldEndValue != null)) {
                Term endFieldEndTerm = null, endFieldStartTerm = null;

                if (this.endFieldStartValue != null) {
                    endFieldStartTerm = new Term(this.endFieldName,
                            this.endFieldStartValue);
                }

                if (this.endFieldEndValue != null) {
                    endFieldEndTerm = new Term(this.endFieldName,
                            this.endFieldEndValue);
                }

                TermRangeQuery query2 = new TermRangeQuery(endFieldEndTerm.field(),endFieldStartTerm.bytes(),
                        endFieldEndTerm.bytes(), true, true);
                booleanQuery.add(query2, BooleanClause.Occur.MUST);
            }

            Sort sort = new Sort(new SortField("CAS.ProductReceivedTime",
                    SortField.Type.STRING, true));
            //TODO Fix number
            TopFieldDocs topDocs = searcher.search(booleanQuery.build(), 1, sort);
            ScoreDoc[] hits = topDocs.scoreDocs;

            if (topDocs.totalHits > 0) {
                products = new Vector(topDocs.totalHits);
                for (int i = 0; i < topDocs.totalHits; i++) {
                    Document productDoc = searcher.doc(hits[i].doc);

                    products.add(productDoc.get("reference_data_store"));
                }
            } else {
                LOG.log(Level.WARNING, "Query: [" + query1
                        + "] for Product Type: [" + productTypeId
                        + "] returned no results");
            }

        } catch (IOException e) {
            LOG.log(Level.WARNING,
                    "IOException when opening index directory: ["
                            + this.indexPath + "] for search: Message: "
                            + e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            if (searcher != null) {
                try {
                    //TODO CLOSE SEARCH
                   // searcher.close();
                } catch (Exception ignore) {
                }
            }
        }

        return products;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String usage = "RangeQueryTester [options]\n"
                + "\t--idxPath </path/to/lucene/index>\n"
                + "\t--productTypeId <product type>\n"
                + "\t--startField <field for start range>\n"
                + "\t--endField <field for end range>\n"
                + "\t--startFieldStart <value>\n"
                + "\t--startFieldEnd <value>\n" + "\t--endFieldStart <value>\n"
                + "\t--endFieldEnd <value>\n";

        String idxPath = null, startField = null, endField = null;
        String startFieldStart = null, startFieldEnd = null;
        String endFieldStart = null, endFieldEnd = null;
        String productTypeId = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--idxPath")) {
                idxPath = args[++i];
            } else if (args[i].equals("--startField")) {
                startField = args[++i];
            } else if (args[i].equals("--endField")) {
                endField = args[++i];
            } else if (args[i].equals("--startFieldStart")) {
                startFieldStart = args[++i];
            } else if (args[i].equals("--startFieldEnd")) {
                startFieldEnd = args[++i];
            } else if (args[i].equals("--endFieldStart")) {
                endFieldStart = args[++i];
            } else if (args[i].equals("--endFieldEnd")) {
                endFieldEnd = args[++i];
            } else if (args[i].equals("--productTypeId")) {
                productTypeId = args[++i];
            }
        }

        if (idxPath == null || productTypeId == null || startField == null
                || (startFieldStart == null && startFieldEnd == null)) {
            System.err.println(usage);
            System.exit(1);
        }

        RangeQueryTester queryTester = new RangeQueryTester();
        queryTester.setIndexPath(idxPath);
        queryTester.setStartFieldEndValue(startFieldEnd);
        queryTester.setStartFieldStartValue(startFieldStart);
        queryTester.setStartFieldName(startField);
        queryTester.setEndFieldName(endField);
        queryTester.setEndFieldStartValue(endFieldStart);
        queryTester.setEndFieldEndValue(endFieldEnd);

        List productFiles = queryTester.doRangeQuery(productTypeId);

        if (productFiles != null && productFiles.size() > 0) {
            for (Object productFile1 : productFiles) {
                String productFile = (String) productFile1;
                System.out.println(productFile);
            }
        } else {
            System.out.println("No results found!");
        }
    }

    /**
     * @return Returns the endFieldName.
     */
    public String getEndFieldName() {
        return endFieldName;
    }

    /**
     * @param endFieldName
     *            The endFieldName to set.
     */
    public void setEndFieldName(String endFieldName) {
        this.endFieldName = endFieldName;
    }

    /**
     * @return Returns the indexPath.
     */
    public String getIndexPath() {
        return indexPath;
    }

    /**
     * @param indexPath
     *            The indexPath to set.
     */
    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }

    /**
     * @return Returns the startFieldName.
     */
    public String getStartFieldName() {
        return startFieldName;
    }

    /**
     * @param startFieldName
     *            The startFieldName to set.
     */
    public void setStartFieldName(String startFieldName) {
        this.startFieldName = startFieldName;
    }

    /**
     * @return Returns the endFieldEndValue.
     */
    public String getEndFieldEndValue() {
        return endFieldEndValue;
    }

    /**
     * @param endFieldEndValue
     *            The endFieldEndValue to set.
     */
    public void setEndFieldEndValue(String endFieldEndValue) {
        this.endFieldEndValue = endFieldEndValue;
    }

    /**
     * @return Returns the endFieldStartValue.
     */
    public String getEndFieldStartValue() {
        return endFieldStartValue;
    }

    /**
     * @param endFieldStartValue
     *            The endFieldStartValue to set.
     */
    public void setEndFieldStartValue(String endFieldStartValue) {
        this.endFieldStartValue = endFieldStartValue;
    }

    /**
     * @return Returns the startFieldEndValue.
     */
    public String getStartFieldEndValue() {
        return startFieldEndValue;
    }

    /**
     * @param startFieldEndValue
     *            The startFieldEndValue to set.
     */
    public void setStartFieldEndValue(String startFieldEndValue) {
        this.startFieldEndValue = startFieldEndValue;
    }

    /**
     * @return Returns the startFieldStartValue.
     */
    public String getStartFieldStartValue() {
        return startFieldStartValue;
    }

    /**
     * @param startFieldStartValue
     *            The startFieldStartValue to set.
     */
    public void setStartFieldStartValue(String startFieldStartValue) {
        this.startFieldStartValue = startFieldStartValue;
    }

}
