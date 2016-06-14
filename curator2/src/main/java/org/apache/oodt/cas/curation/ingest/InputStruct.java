package org.apache.oodt.cas.curation.ingest;

import java.util.List;

/**
 * Class to represent input for ingest
 * @author starchmd
 */
public class InputStruct {
    //public String id;
    public List<InputStruct.InputEntry> entries;
    public static class InputEntry {
        public String pname;
        public String file;
        public long size;
        public long timestamp;
        public Exception error = null;
    }
}