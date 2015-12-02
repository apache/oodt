package org.apache.oodt.cas.curation.ingest;

import java.util.List;

/**
 * Class representing output
  * @author selina
 */
public class OutputStruct {
    public List<OutputStruct.OutputEntry> status; 
    public static class OutputEntry {
        public String file;
        public String product;
        public String status;
        public long timestamp;
    }
}