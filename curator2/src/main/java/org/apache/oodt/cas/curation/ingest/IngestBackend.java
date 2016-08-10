package org.apache.oodt.cas.curation.ingest;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.cas.curation.ingest.InputStruct.InputEntry;
import org.apache.oodt.cas.curation.metadata.FlatDirMetadataHandler;
import org.apache.oodt.cas.curation.rest.IngestRest;
import org.apache.oodt.cas.filemgr.ingest.Ingester;
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;

/**
 * Backend for the ingest service
 * @author starchmd
 */
public class IngestBackend {
    private static final String DATA_TRANSFER_SERVICE = "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";
    private static final String IN_PROGRESS = "IN PROGRESS";
    private static final String DONE = "DONE";
    private static final Logger LOG = Logger.getLogger(IngestRest.class.getName());

    private ConcurrentHashMap<String,List<InputEntry>> current = new ConcurrentHashMap<String,List<InputEntry>>();
    
    private XmlRpcFileManagerClient client = null;
    private Ingester ingester = null;
    private URL url = null;
    /**
     * Setup this backend
     * @throws IngestException
     */
    public IngestBackend() throws IngestException {
        try {
            this.url = new URL(Configuration.getWithReplacement(Configuration.FILEMANAGER_URL_CONFIG));
            this.ingester = new StdIngester(DATA_TRANSFER_SERVICE);
            LOG.log(Level.INFO,"Connecting to File Manager at:"+this.url.toString());
            this.client = new XmlRpcFileManagerClient(this.url);
        } catch(Exception e) {
            LOG.log(Level.WARNING,"Error: problem constructing backend: "+e);
            throw new IngestException("Error: problem setting up ingest backend.",e);
        }
    }
    /**
     * Ingest based on input
     * @param input - input struct
     * @param user - user to isolate requests
     */
    public void ingest(InputStruct input,String user) {
        if (!current.containsKey(user)) {
            current.put(user, new LinkedList<InputEntry>());
        }
        for (InputStruct.InputEntry entry : input.entries) {
            current.get(user).add(entry);
        }
        for (InputStruct.InputEntry entry : input.entries) {
            try {
                ingest(entry.file,user);
            } catch(IngestException e) {
                entry.error = e;
            }
        }
    }
    /**
     * Ingests a single file
     * @param file - file to ingest
     * @param user - user to isolate requests
     * @throws IngestException  - error on ingestion
     */
    private void ingest(String file, String user) throws IngestException {
        try {
            File full = null;
            if (!file.startsWith("/")) {
                String parent = new File(Configuration.getWithReplacement(Configuration.STAGING_AREA_CONFIG)).getParent();
                full = new File(parent,file);
            } else {
                full = new File(file);
            }
            FlatDirMetadataHandler handler = new FlatDirMetadataHandler();
            Metadata meta = handler.get(file,user);
            System.out.println("File: "+file+" URL: "+this.url+" Full: "+full.getAbsoluteFile()+" Metadata: "+meta);
            ingester.ingest(this.url, full.getAbsoluteFile(), meta);
            //Remove metadata file after successful ingest
            handler.remove(file,user);
            org.apache.commons.io.FileUtils.deleteQuietly(full);
            File p = full.getParentFile();
            if(p.exists() && p.isDirectory()){
                if(p.list().length==0){
                    FileUtils.deleteDirectory(p);
                }

            }
        } catch(Exception e) {
            LOG.log(Level.WARNING,"Error: failed ingesting product: "+e);
            throw new IngestException("Error: problem while ingesting",e);
        }
    }
    /**
     * Check the status of the currently ingested items
     * @return output struct
     */
    public OutputStruct status(String user) throws IngestException {
        try {
            //List<InputStruct.InputEntry> torm = new LinkedList<InputStruct.InputEntry>();
            List<OutputStruct.OutputEntry> ret = new LinkedList<OutputStruct.OutputEntry>();
            if (this.current.get(user) != null) {
                for (InputStruct.InputEntry entry : current.get(user)) {
                    OutputStruct.OutputEntry temp = new OutputStruct.OutputEntry();
                    temp.file = entry.file;
                    temp.pname = entry.pname;
                    temp.timestamp = entry.timestamp;
                    if (entry.error != null ) {
                        //torm.add(entry);
                        temp.status = "Error: "+entry.error.getMessage();
                    } else if (client.hasProduct(entry.pname)) {
                         //torm.add(entry);
                         temp.product = client.getProductByName(entry.pname).getProductId();
                         temp.status = DONE;
                    } else {
                        temp.status = IN_PROGRESS;
                    }
                    ret.add(temp);
                }
            }
            //for (InputStruct.InputEntry entry : torm) {
            //    this.current.get(user).remove(entry);
            //}
            //if (this.current.get(user).isEmpty()) {
            //    this.current.remove(user);
            //}
            OutputStruct out = new OutputStruct();
            out.status = ret;
            return out;
        } catch (Exception e) {
            LOG.log(Level.WARNING,"Error: failed checking status: "+e);
            throw new IngestException("Error: failed to check status",e);
        }
    }
    /**
     * Clears any errors registered in this system
     */
    public void clearErrors(String user) {
        List<InputStruct.InputEntry> torm = new LinkedList<InputStruct.InputEntry>();
        if (this.current.get(user) == null) {
            return;
        }
        for (InputStruct.InputEntry entry : this.current.get(user)) {
            torm.add(entry);
        }
        for (InputStruct.InputEntry entry : torm) {
            this.current.get(user).remove(entry);
        }
        if (this.current.get(user).isEmpty()) {
            this.current.remove(user);
        }
    }
}
