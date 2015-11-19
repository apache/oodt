package org.apache.oodt.cas.curation.rest;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;

import org.apache.oodt.cas.curation.configuration.Configuration;
import org.apache.oodt.cas.curation.metadata.FlatDirMetadataHandler;
import org.apache.oodt.cas.filemgr.ingest.Ingester;
import org.apache.oodt.cas.filemgr.ingest.StdIngester;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A backend that uses put to initiate ingest
 *  
 * @author starchmd
 */
@Path("ingest")
public class IngestBackend {

    private static final Logger LOG = Logger.getLogger(IngestBackend.class.getName());
    //GSON serialization object using Google GSON
    private Gson gson = new GsonBuilder().create();
    private static final String DATA_TRANSFER_SERVICE = "org.apache.oodt.cas.filemgr.datatransfer.LocalDataTransferFactory";
    List<Input.InputEntry> current = new LinkedList<Input.InputEntry>();
    XmlRpcFileManagerClient client = null;
    /**
     * Construct a directory backend with hard-coded directories
     */
    public IngestBackend() {}
    @PUT
    @Consumes("application/json")
    /**
     * Runs the ingest
     * @param extractor - optional extractor to run
     * @param json - new json for file
     */
    public Response ingest(@QueryParam("extractor") String extractor,String json) throws Exception {
        try {
            IngestBackend.Input input = gson.fromJson(json, IngestBackend.Input.class);;
            for (Input.InputEntry entry : input.entries) {
                current.add(entry);
            }
            Ingester ingester = new StdIngester(DATA_TRANSFER_SERVICE);
            for (Input.InputEntry entry : input.entries) {
                try {
                    String file = entry.file;
                    String parent = new File(Configuration.getWithReplacement(Configuration.STAGING_AREA_CONFIG)).getParent();
                    File full = new File(parent,file);
                    Metadata meta = new FlatDirMetadataHandler().get(file);
                    ingester.ingest(new URL(Configuration.getWithReplacement(Configuration.FILEMANAGER_URL_CONFIG)), full.getAbsoluteFile(), meta);
                } catch(Exception e) {
                    LOG.severe("Error: Failed to ingest product: "+e.getMessage());
                    entry.error = new Exception(e);
                    throw e;
                }
            }
        } catch (Exception e) {
            LOG.severe("Error: bad request: "+e.getMessage());
            throw e;
        }
        return Response.ok().build();
    }
    @GET
    @Produces("application/json")
    /**
     * Get ingest status
     * @return list of statuses
     * @throws Exception - exception on problem
     */
    public String status() throws Exception {
        try {
            List<Input.InputEntry> torm = new LinkedList<Input.InputEntry>();
            List<Output.OutputEntry> ret = new LinkedList<Output.OutputEntry>();
            if (this.client == null) {
                URL url = new URL(Configuration.getWithReplacement(Configuration.FILEMANAGER_URL_CONFIG));
                this.client = new XmlRpcFileManagerClient(url);
            }
            for (Input.InputEntry entry : current) {
                Output.OutputEntry temp = new Output.OutputEntry();
                temp.file = entry.file;
                temp.timestamp = entry.timestamp;
                if (entry.error != null ) {
                    temp.status = "Error: "+entry.error.getMessage();
                } else if (client.hasProduct(entry.pname)) {
                     torm.add(entry);
                     temp.product = client.getProductByName(entry.pname).getProductId();
                     temp.status = "DONE";
                } else {
                    temp.status = "IN PROGRESS";
                }
                ret.add(temp);
            }
            for (Input.InputEntry entry : torm) {
                this.current.remove(entry);
            }
            Output out = new Output();
            out.status = ret;
            return gson.toJson(out);
        } catch (Exception e) {
            LOG.severe("Error: bad request: "+e.getMessage());
            throw e;
        }
    }
    /**
     * Class to represent input for ingest
     * @author starchmd
     */
    static class Input {
        //public String id;
        public List<InputEntry> entries;
        static class InputEntry {
            public String pname;
            public String file;
            public long size;
            public long timestamp;
            public Exception error = null;
        }
    }
    /**
     * Class representing output
      * @author selina
     */
    static class Output {
        public List<OutputEntry> status; 
        static class OutputEntry {
            public String file;
            public String product;
            public String status;
            public long timestamp;
        }
    }
}
