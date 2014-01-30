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

package org.apache.oodt.cas.filemgr.versioning;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Reference;
import org.apache.oodt.cas.filemgr.structs.Product;

//JDK imports
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author mattmann
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A Utility component to help out with versioning.
 * </p>
 * 
 * 
 */
public final class VersioningUtils {

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(VersioningUtils.class
            .getName());

    // filter to only find directories when doing a listFiles
    private static FileFilter DIR_FILTER = new FileFilter() {
        public boolean accept(File file) {
            return file.isDirectory();
        }
    };

    // filter to only find files when doing a listFiles
    private static FileFilter FILE_FILTER = new FileFilter() {
        public boolean accept(File file) {
            return file.isFile();
        }
    };

    public static List<Reference> getReferencesFromDir(File dirRoot) {
        List<Reference> references = null;

        if (dirRoot == null)
            throw new IllegalArgumentException("null");
        if (!dirRoot.isDirectory())
            dirRoot = dirRoot.getParentFile();

        references = new Vector<Reference>();

        Stack<File> stack = new Stack<File>();
        stack.push(dirRoot);
        while (!stack.isEmpty()) {
            File dir = stack.pop();
            // add the reference for the dir
            // except if it's the rootDir, then, skip it
            if (!dir.equals(dirRoot)) {
                try {
                    Reference r = new Reference();
                    r.setOrigReference(dir.toURL().toExternalForm());
                    r.setFileSize(dir.length());
                    references.add(r);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    LOG.log(Level.WARNING,
                            "MalformedURLException when generating reference for dir: "
                                    + dir);
                }
            }

            File[] files = dir.listFiles(FILE_FILTER);

            for (int i = 0; i < files.length; i++) {
                // add the file references
                try {
                    Reference r = new Reference();
                    r.setOrigReference(files[i].toURL().toExternalForm());
                    r.setFileSize(files[i].length());
                    references.add(r);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    LOG.log(Level.WARNING,
                            "MalformedURLException when generating reference for file: "
                                    + files[i]);
                }

            }
            File[] subdirs = dir.listFiles(DIR_FILTER);
            if (subdirs != null)
                for (int i = 0; i < subdirs.length; i++) {
                    stack.push(subdirs[i]);
                }
        }

        return references;
    }

    public static List<String> getURIsFromDir(File dirRoot) {
        List<String> uris = null;

        if (dirRoot == null)
            throw new IllegalArgumentException("null");
        if (!dirRoot.isDirectory())
            dirRoot = dirRoot.getParentFile();

        uris = new Vector<String>();

        Stack<File> stack = new Stack<File>();
        stack.push(dirRoot);
        while (!stack.isEmpty()) {
            File dir = stack.pop();
            // add the reference for the dir
            // except if it's the rootDir, then, skip it
            if (!dir.equals(dirRoot)) {
                uris.add(dir.toURI().toString());
            }

            File[] files = dir.listFiles(FILE_FILTER);

            for (int i = 0; i < files.length; i++) {
                // add the file references
                uris.add(files[i].toURI().toString());
            }

            File[] subdirs = dir.listFiles(DIR_FILTER);
            if (subdirs != null)
                for (int i = 0; i < subdirs.length; i++) {
                    stack.push(subdirs[i]);
                }
        }

        return uris;
    }

    public static void createBasicDataStoreRefsHierarchical(List<Reference> references) {
        // file:///www/folder1
        // file:///www/folder1/file1
        // file:///www/folder1/file2
        // file:///www/folder1/folder2/
        // file:///www/folder1/folder2/file3

        // toDir: file:///www/myfolder/product1
        // origDir: file:///www/folder1

        String toDirRef = references.get(0)
                .getDataStoreReference();
        String origDirRef = references.get(0).getOrigReference();
        String origDirRefName = new File(origDirRef).getName();

        for (Iterator<Reference> i = references.iterator(); i.hasNext();) {
            Reference r = i.next();

            // don't bother with the first one, because it's already set
            // correctly
            if (r.getOrigReference().equals(origDirRef)) {
                continue;
            }

            // get the first occurence of the origDir name in the string
            // then, the ref becomes:
            // toDir+r.getOrigRef.substring(first occurence of
            // origDir).substring(first occurence of '/'+1)

            String dataStoreRef = toDirRef;
            int firstOccurenceOfOrigDir = r.getOrigReference().indexOf(
                    origDirRefName);
            String tmpRef = r.getOrigReference().substring(
                    firstOccurenceOfOrigDir);
            LOG.log(Level.FINER, "tmpRef: " + tmpRef);
            int firstOccurenceSlash = tmpRef.indexOf("/");
            dataStoreRef += tmpRef.substring(firstOccurenceSlash + 1);

            LOG.log(Level.FINE, "VersioningUtils: Generated data store ref: "
                    + dataStoreRef + " from origRef: " + r.getOrigReference());
            r.setDataStoreReference(dataStoreRef);
        }

    }

    public static void createBasicDataStoreRefsFlat(String productName,
            String productRepoPath, List<Reference> references) {
        for (Iterator<Reference> i = references.iterator(); i.hasNext();) {
            Reference r = i.next();

            String dataStoreRef = null;
            String productRepoPathRef = null;

            try {
                productRepoPathRef = new File(new URI(productRepoPath)).toURL()
                        .toExternalForm();

                if (!productRepoPathRef.endsWith("/")) {
                    productRepoPathRef += "/";
                }

                dataStoreRef = productRepoPathRef
                        + URLEncoder.encode(productName, "UTF-8") + "/"
                        + new File(new URI(r.getOrigReference())).getName();
            } catch (IOException e) {
                LOG.log(Level.WARNING,
                        "VersioningUtils: Error generating dataStoreRef for "
                                + r.getOrigReference() + ": Message: "
                                + e.getMessage());
            } catch (URISyntaxException e) {
                LOG.log(Level.WARNING,
                        "VersioningUtils: Error generating dataStoreRef for "
                                + r.getOrigReference() + ": Message: "
                                + e.getMessage());
            }

            LOG.log(Level.FINE, "VersioningUtils: Generated data store ref: "
                    + dataStoreRef + " from origRef: " + r.getOrigReference());
            r.setDataStoreReference(dataStoreRef);
        }

    }

    public static void addRefsFromUris(Product p, List<String> uris) {
        // add the refs to the Product
        for (Iterator<String> i = uris.iterator(); i.hasNext();) {
            String ref = i.next();
            Reference r = new Reference(ref, null, quietGetFileSizeFromUri(ref));
            p.getProductReferences().add(r);
        }
    }

    public static String getAbsolutePathFromUri(String uriStr) {
        URI uri = null;
        String absPath = null;

        try {
            uri = new URI(uriStr);
            absPath = new File(uri).getAbsolutePath();
        } catch (URISyntaxException e) {
            LOG.log(Level.WARNING,
                    "URISyntaxException getting URI from URI str: [" + uriStr
                            + "]");
        }

        return absPath;
    }

    private static long quietGetFileSizeFromUri(String uri) {
        File fileRef = null;

        try {
            fileRef = new File(new URI(uri));
        } catch (URISyntaxException e) {
            LOG.log(Level.WARNING,
                    "URISyntaxException when getting file size from uri: ["
                            + uri + "]: Message: " + e.getMessage());
            return -1L;
        }

        return fileRef.length();
    }

}
