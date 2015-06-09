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


package org.apache.oodt.cas.pushpull.filerestrictions;

//JDK imports
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class VirtualFile {

    private String regExp;

    private LinkedList<VirtualFile> children;

    private VirtualFile parent;

    private boolean noDirs;

    private boolean noFiles;

    private boolean isDir;

    private boolean allowNewFiles;

    private boolean allowNewDirs;

    private VirtualFile(boolean isDir) {
        this.isDir = isDir;
        children = new LinkedList<VirtualFile>();
        noDirs = noFiles = !isDir;
        allowNewFiles = allowNewDirs = isDir;
    }

    public VirtualFile(String path, boolean isDir) {
        this(isDir);

        if (path != null) {
            StringTokenizer st = new StringTokenizer(path, "/");

            if (st.countTokens() > 1) {
                VirtualFile vf = path.startsWith("/") ? VirtualFile
                        .createRootDir()
                        : new VirtualFile(st.nextToken(), true);
                String curRegExp = st.nextToken();

                while (st.hasMoreTokens()) {
                    VirtualFile temp = new VirtualFile(curRegExp, true);
                    vf.addChild(temp);
                    vf = temp;
                    curRegExp = st.nextToken();
                }
                this.regExp = curRegExp;
                vf.addChild(this);
            } else if (st.countTokens() > 0) {
                this.regExp = st.nextToken();
                if (path.startsWith("/"))
                    VirtualFile.createRootDir().addChild(this);
            } else {
                this.copy(VirtualFile.createRootDir());
            }
        } else
            this.copy(VirtualFile.createRootDir());
    }

    public VirtualFile(VirtualFile root, String path, boolean isDir) {
        this(isDir);

        if (path != null) {
            StringTokenizer st = new StringTokenizer(path, "/");

            if (st.countTokens() > 0) {
                VirtualFile vf = root;
                String curRegExp = st.nextToken();

                while (st.hasMoreTokens()) {
                    VirtualFile temp = new VirtualFile(curRegExp, true);
                    vf.addChild(temp);
                    vf = temp;
                    curRegExp = st.nextToken();
                }
                this.regExp = curRegExp;
                vf.addChild(this);
            } else
                this.copy(root);
        } else
            this.copy(root);
    }

    public static VirtualFile createRootDir() {
        VirtualFile root = new VirtualFile(true);
        root.regExp = "/";
        return root;
    }

    public VirtualFile getRootDir() {
        VirtualFile vf = this;
        while (vf.getParentFile() != null)
            vf = vf.getParentFile();
        return vf;
    }

    public static VirtualFile mergeTwoFiles(VirtualFile vf1, VirtualFile vf2) {
        if (vf1.isDir && vf1.isDir) {
            VirtualFile newFile = VirtualFile.createRootDir();
            newFile.children.addAll(vf1.children);
            newFile.children.addAll(vf2.children);
            return newFile;
        } else
            return null;
    }

    public void addChild(VirtualFile vf) {
        if (this.isDir) {
            VirtualFile existingChildWithSameName = this.getChild(vf.regExp,
                    vf.isDir);
            if (existingChildWithSameName == null) {
                children.add(vf);
                if (vf.isDir())
                    allowNewDirs = false;
                else
                    allowNewFiles = false;
                vf.parent = this;
            } else {
                vf.copy(existingChildWithSameName);
            }
        }
    }

    public LinkedList<VirtualFile> getChildren() {
        return children;
    }

    public VirtualFile getChild(String regExp, boolean isDirectory) {
        for (VirtualFile vf : children) {
            // System.out.println("GETCHILD: " + regExp + " " + vf.regExp);
            if ((regExp.equals(vf.regExp) || Pattern.matches(vf.regExp, regExp))
                    && vf.isDir == isDirectory)
                return vf;
        }
        return null;
    }

    public VirtualFile getChildRecursive(VirtualFile vf) {
        return this.getChildRecursive(vf.getAbsolutePath(), vf.isDir);
    }

    public VirtualFile getChildRecursive(String path, boolean isDirectory) {
        StringTokenizer st = new StringTokenizer(path, "/");
        VirtualFile vf = this;
        while (st.hasMoreTokens()) {
            String curRegExp = st.nextToken();
            if (st.hasMoreTokens()) {
                if ((vf = vf.getChild(curRegExp, true)) == null)
                    return null;
            } else {
                return vf.getChild(curRegExp, isDirectory);
            }
        }
        return null;
    }

    public boolean hasChild(VirtualFile vf) {
        if (children.contains(vf))
            return true;
        return false;
    }

    public String getAbsolutePath() {
        if (regExp == null)
            return null;
        StringBuffer path = new StringBuffer(this.regExp);
        VirtualFile parent = this.parent;
        while (parent != null) {
            path.insert(0, (parent.regExp != "/" ? parent.regExp : "") + "/");
            parent = parent.parent;
        }
        return path.toString();
    }

    public VirtualFile getParentFile() {
        return parent;
    }

    public String getRegExp() {
        return regExp;
    }

    public void setNoDirs(boolean noDirs) {
        if (this.isDir) {
            if (noDirs)
                allowNewDirs = false;
            this.noDirs = noDirs;
        }
    }

    public void setNoFiles(boolean noFiles) {
        if (this.isDir) {
            if (noFiles)
                allowNewFiles = false;
            this.noFiles = noFiles;
        }
    }

    public boolean allowNewDirs() {
        return allowNewDirs;
    }

    public boolean allowNewFiles() {
        return allowNewFiles;
    }

    public boolean allowNoDirs() {
        return noDirs;
    }

    public boolean allowNoFiles() {
        return noFiles;
    }

    public boolean isDir() {
        return isDir;
    }

    public void copy(VirtualFile vf) {
        this.allowNewDirs = vf.allowNewDirs;
        this.allowNewFiles = vf.allowNewFiles;
        this.children = vf.children;
        this.isDir = vf.isDir;
        this.noDirs = vf.noDirs;
        this.noFiles = vf.noFiles;
        this.parent = vf.parent;
        this.regExp = vf.regExp;
    }

    // TODO: make it compare against all variables
    public boolean equals(Object obj) {
        if (obj instanceof VirtualFile) {
            VirtualFile compareFile = (VirtualFile) obj;
            if (compareFile.getRegExp().equals(regExp)
                    && compareFile.isDir() == this.isDir)
                return true;
        }
        return false;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(
                "-<VirtualFile>\t : allowNewDirs/noDirs\t : allowNewFiles/noFiles\n------------\n");
        LinkedList<VirtualFile> printFiles = new LinkedList<VirtualFile>();
        printFiles.add(this);
        sb.append(printVirtualFiles(printFiles, "-"));
        return sb.toString();
    }

    private StringBuffer printVirtualFiles(LinkedList<VirtualFile> list,
            String spacer) {
        StringBuffer output = new StringBuffer("");
        for (VirtualFile vf : list) {
            output.append(spacer);
            output.append(vf.getRegExp()
                    + (vf.isDir && !vf.regExp.equals("/") ? "/" : "") + "\t : "
                    + vf.allowNewDirs + "/" + vf.noDirs + "\t\t : "
                    + vf.allowNewFiles + "/" + vf.noFiles + "\n");
            output.append(printVirtualFiles(vf.getChildren(), " " + spacer));
        }
        return output;
    }

}
