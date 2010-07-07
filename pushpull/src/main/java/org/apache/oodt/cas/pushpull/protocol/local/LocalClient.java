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


package org.apache.oodt.cas.pushpull.protocol.local;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

//OODT imports
import org.apache.oodt.cas.pushpull.exceptions.ProtocolException;
import org.apache.oodt.cas.pushpull.protocol.Protocol;
import org.apache.oodt.cas.pushpull.protocol.ProtocolFile;
import org.apache.oodt.cas.pushpull.protocol.ProtocolPath;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class LocalClient extends Protocol {

    private File currentFile;

    protected LocalClient() {
        super("file");
        currentFile = new File(System.getProperty("user.home", "/"));
    }

    @Override
    public void abortCurFileTransfer() throws ProtocolException {
        // do nothing
    }

    @Override
    protected void chDir(ProtocolPath path) throws ProtocolException {
        currentFile = new File(path.getPathString());
    }

    @Override
    public void cdToRoot() throws ProtocolException {
        currentFile = new File("/");
    }

    @Override
    protected void connect(String host, String username, String password)
            throws ProtocolException {
        // do nothing
    }

    @Override
    public void disconnectFromServer() throws ProtocolException {
        // do nothing
    }

    @Override
    public void getFile(ProtocolFile file, File toLocalFile)
            throws ProtocolException {
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = new FileInputStream(new File(file.getProtocolPath()
                    .getPathString())).getChannel();
            outChannel = new FileOutputStream(toLocalFile).getChannel();

            // magic number for Windows, 64Mb - 32Kb)
            int maxCount = (64 * 1024 * 1024) - (32 * 1024);
            long size = inChannel.size();
            long position = 0;
            while (position < size)
                position += inChannel
                        .transferTo(position, maxCount, outChannel);
        } catch (Exception e) {
            throw new ProtocolException("Failed to get file '" + file + "' : "
                    + e.getMessage());
        } finally {
            try {
                if (inChannel != null)
                    inChannel.close();
            } catch (Exception e) {
            }
            try {
                if (outChannel != null)
                    outChannel.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean isConnected() throws ProtocolException {
        return true;
    }

    @Override
    public List<ProtocolFile> listFiles() throws ProtocolException {
        try {
            LinkedList<ProtocolFile> files = new LinkedList<ProtocolFile>();
            File[] fileList = currentFile.listFiles();
            for (File file : fileList) {
                if (file != null)
                    files.add(new ProtocolFile(this.getRemoteSite(),
                            new ProtocolPath(file.getAbsolutePath(), file
                                    .isDirectory())));
            }
            return files;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ProtocolException("Failed to ls : " + e.getMessage());
        }
    }

    @Override
    public ProtocolFile getCurrentWorkingDir() throws ProtocolException {
        try {
            return new ProtocolFile(this.getRemoteSite(), new ProtocolPath(
                    currentFile.getAbsolutePath(), true));
        } catch (Exception e) {
            throw new ProtocolException("Failed to pwd : " + e.getMessage());
        }
    }

    @Override
    protected boolean deleteFile(ProtocolFile file) {
        return new File(file.getProtocolPath().getPathString()).delete();
    }

}
