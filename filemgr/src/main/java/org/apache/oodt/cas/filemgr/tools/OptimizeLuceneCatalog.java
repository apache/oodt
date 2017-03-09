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
import java.util.logging.Level;
import java.util.logging.Logger;

//Lucene imports
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.store.FSDirectory;

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

    public static final double DOUBLE = 1000.0;
    public static final int INT = 20;
    private DirectoryReader reader;
    private IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

    /* the path to the lucene index directory */
    private String catalogPath = null;

    /* the merge factor to use when optimizing the index */
    private int mergeFactor = 20;

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(OptimizeLuceneCatalog.class
            .getName());

    /**
     * Default constructor.
     */
    public OptimizeLuceneCatalog(String catPath, int mf) {
        this.catalogPath = catPath;
        this.mergeFactor = mf;
        try {
            reader = DirectoryReader.open(FSDirectory.open(Paths.get(catalogPath)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void doOptimize() {
        IndexWriter writer = null;
        boolean createIndex = false;

        try {
            writer = new IndexWriter(reader.directory(), config);
            LogMergePolicy lmp =new LogDocMergePolicy();
            lmp.setMergeFactor(this.mergeFactor);
            config.setMergePolicy(lmp);

            long timeBefore = System.currentTimeMillis();
            //TODO http://blog.trifork.com/2011/11/21/simon-says-optimize-is-bad-for-you/
            //writer.optimize();
            long timeAfter = System.currentTimeMillis();
            double numSeconds = ((timeAfter - timeBefore) * 1.0) / DOUBLE;
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
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)  {
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
            mergeFactor = INT; // default
        }

        OptimizeLuceneCatalog optimizer = new OptimizeLuceneCatalog(catPath,
                mergeFactor);
        optimizer.doOptimize();

    }

}
