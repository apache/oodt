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

package gov.nasa.jpl.oodt.cas.filemgr.tools;

//JDK imports
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//Lucene imports
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Tool to optimize a {@link LuceneCatalog}'s index directory for search.
 * </p>
 * 
 */
public class OptimizeLuceneCatalog {

    /* the path to the lucene index directory */
    private String catalogPath = null;

    /* the merge factor to use when optimizing the index */
    private int mergeFactor = 20;

    /* our log stream */
    private static Logger LOG = Logger.getLogger(OptimizeLuceneCatalog.class
            .getName());

    /**
     * Default constructor.
     */
    public OptimizeLuceneCatalog(String catPath, int mf) {
        this.catalogPath = catPath;
        this.mergeFactor = mf;
    }

    public void doOptimize() throws Exception {
        IndexWriter writer = null;
        boolean createIndex = false;

        try {
            writer = new IndexWriter(catalogPath, new StandardAnalyzer(),
                    createIndex);
            writer.setMergeFactor(this.mergeFactor);
            long timeBefore = System.currentTimeMillis();
            writer.optimize();
            long timeAfter = System.currentTimeMillis();
            double numSeconds = ((timeAfter - timeBefore) * 1.0) / 1000.0;
            LOG.log(Level.INFO, "LuceneCatalog: [" + this.catalogPath
                    + "] optimized: took: [" + numSeconds + "] seconds");
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Unable to optimize lucene index: ["
                    + catalogPath + "]: Message: " + e.getMessage());
        } finally {
            try {
                writer.close();
            } catch (Exception ignore) {
            }
            writer = null;
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String usage = "OptimizeLuceneCatalog [options]\n"
                + "--catalogPath <path to lucene catalog>\n"
                + "[--mergeFactor <merge factor for index>]\n";

        String catPath = null;
        int mergeFactor = -1;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--catalogPath")) {
                catPath = args[++i];
            } else if (args[i].equals("--mergeFactor")) {
                mergeFactor = Integer.parseInt(args[++i]);
            }
        }

        if (catPath == null) {
            System.err.println(usage);
            System.exit(1);
        }

        if (mergeFactor == -1) {
            mergeFactor = 20; // default
        }

        OptimizeLuceneCatalog optimizer = new OptimizeLuceneCatalog(catPath,
                mergeFactor);
        optimizer.doOptimize();

    }

}
