package org.apache.oodt.cas.catalog.server.channel.avrorpc;

//OODT imports
import org.apache.oodt.cas.catalog.page.*;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.server.channel.AbstractCommunicationChannelServer;
import org.apache.oodt.cas.catalog.struct.Dictionary;
import org.apache.oodt.cas.catalog.struct.Index;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.metadata.Metadata;

//JDK imports
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

//APACHE imports
import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;

public class AvrorpcCommunicationChannelServer extends AbstractCommunicationChannelServer implements AvroCommunicationChannel {

    private Server server;

    public AvrorpcCommunicationChannelServer() {
        super();
    }

    @Override
    public Void avrorpc_startup() throws AvroRemoteException {
        this.server = new NettyServer(new SpecificResponder(AvroCommunicationChannel.class,this),new InetSocketAddress(this.port));
        this.server.start();
        return null;

    }

    @Override
    public boolean avrorpc_shutdown() throws AvroRemoteException {
        this.server.close();
        this.server = null;
        return true;
    }


    @Override
    public boolean avrorpc_addCatalog1(String catalogObject) throws AvroRemoteException {
        try {
            this.addCatalog(this.serializer.deserializeObject(Catalog.class,(String)catalogObject));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean avrorpc_replaceCatalog(String catalogObject) throws AvroRemoteException {
        try {
            this.replaceCatalog(this.serializer.deserializeObject(Catalog.class,(String)catalogObject));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean avrorpc_addCatalog2(String catalogId, String indexObject) throws AvroRemoteException {
        try {
            this.addCatalog((String)catalogId,this.serializer.deserializeObject(Index.class,(String)indexObject));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean avrorpc_addCatalog3(String catalogId, String indexObject, String dictionariesObject) throws AvroRemoteException {
        try {
            this.addCatalog((String)catalogId,
                    this.serializer.deserializeObject(Index.class,(String)indexObject),
                    this.serializer.deserializeObject(List.class,(String)dictionariesObject));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean avrorpc_addCatalog5(String catalogId, String indexObject, String dictionariesObject, String restrictQueryPermissionObject, String restrictIngestPermissionObject) throws AvroRemoteException {
        try {
            this.addCatalog((String)catalogId,
                    this.serializer.deserializeObject(Index.class,(String)indexObject),
                    this.serializer.deserializeObject(List.class,(String)dictionariesObject),
                    this.serializer.deserializeObject(Boolean.class,(String)restrictQueryPermissionObject),
                    this.serializer.deserializeObject(Boolean.class,(String)restrictIngestPermissionObject));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean avrorpc_addDictionary(String catalogId, String dictionariesObject) throws AvroRemoteException {
        try {
            this.addDictionary((String)catalogId,
                    this.serializer.deserializeObject(Dictionary.class,(String)dictionariesObject));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean avrorpc_replaceDictionaries(String catalogId, String dictionariesObject) throws AvroRemoteException {
        try {
            this.replaceDictionaries((String)catalogId,
                    this.serializer.deserializeObject(List.class,(String)dictionariesObject));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean avrorpc_replaceIndex(String catalogId, String indexObject) throws AvroRemoteException {
        try {
            this.replaceIndex((String)catalogId,this.serializer.deserializeObject(Index.class,(String)indexObject));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean avrorpc_modifyIngestPermission(String catalogId, String restrictIngestPermissionObject) throws AvroRemoteException {
        try {
            this.modifyIngestPermission((String)catalogId,
                    this.serializer.deserializeObject(Boolean.class,(String)restrictIngestPermissionObject));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean avrorpc_modifyQueryPermission(String catalogId, String restrictQueryPermissionObject) throws AvroRemoteException {
        try {
            this.modifyQueryPermission((String)catalogId,
                    this.serializer.deserializeObject(Boolean.class,(String)restrictQueryPermissionObject));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return true;
    }

    @Override
    public boolean avrorpc_delete(String metadataObject) throws AvroRemoteException {
        try {
            this.delete(this.serializer.deserializeObject(Metadata.class,(String)metadataObject));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return true;
    }

    @Override
    public String avrorpc_getPluginUrls() throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getPluginUrls());
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public boolean avrorpc_addPluginUrls(String pluginUrlsObject) throws AvroRemoteException {
        try {
            this.addPluginUrls(
                    this.serializer.deserializeObject(List.class,(String)pluginUrlsObject)
            );
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return true;
    }

    @Override
    public String avrorpc_getPluginStorageDir() throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getPluginStorageDir());
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public boolean avrorpc_transferFile(String filePath, ByteBuffer fileData, int offset, int numBytes) throws AvroRemoteException {
        FileOutputStream fOut = null;
        try {
            File outFile = new File((String)filePath);
            if (outFile.exists())
                fOut = new FileOutputStream(outFile, true);
            else
                fOut = new FileOutputStream(outFile, false);



            fOut.write(fileData.array(), offset, numBytes);
        }catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }finally {
            try {
                fOut.close();
            }catch(Exception e) {}
        }
        return true;
    }

    @Override
    public String avrorpc_getAllPages(String queryPagerObject) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(
                    this.getAllPages(this.serializer.deserializeObject(QueryPager.class,
                            (String)queryPagerObject)));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String avrorpc_getCalalogProperties1() throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getCalalogProperties());
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String avrorpc_getCalalogProperties2(String catalogUrn) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getCalalogProperties((String)catalogUrn));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String avrorpc_getCatalogServiceTransactionId(String catalogTransactionIdObject, String catalogUrn) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getCatalogServiceTransactionId(this.serializer.deserializeObject(TransactionId.class, (String)catalogTransactionIdObject), (String)catalogUrn));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String avrorpc_getCatalogServiceTransactionId2(String catalogReceiptObject, String generateNewObject) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getCatalogServiceTransactionId(this.serializer.deserializeObject(CatalogReceipt.class,(String)catalogReceiptObject), this.serializer.deserializeObject(Boolean.class, (String)generateNewObject)));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String avrorpc_getCatalogServiceTransactionIds(String catalogTransactionIdsObject, String catalogUrn) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getCatalogServiceTransactionIds(this.serializer.deserializeObject(List.class, (String)catalogTransactionIdsObject), (String)catalogUrn));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }

    }

    @Override
    public String avrorpc_getCurrentCatalogIds() throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getCurrentCatalogIds());
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String avrorpc_getMetadataFromTransactionIdStrings(String catalogServiceTransactionIdStringsObject) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getMetadataFromTransactionIdStrings(this.serializer.deserializeObject(List.class,(String) catalogServiceTransactionIdStringsObject)));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }

    }

    @Override
    public String avrorpc_getMetadataFromTransactionIds(String catalogServiceTransactionIdsObject) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getMetadataFromTransactionIds(this.serializer.deserializeObject(List.class, (String)catalogServiceTransactionIdsObject)));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }

    }

    @Override
    public String avrorpc_getNextPage(String queryPagerObject) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getNextPage(this.serializer.deserializeObject(QueryPager.class, (String)queryPagerObject)));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }

    }

    @Override
    public String avrorpc_getProperty(String key) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getProperty((String)key));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String avrorpc_ingest(String metadataObject) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.ingest(this.serializer.deserializeObject(Metadata.class, (String)metadataObject)));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }

    }

    @Override
    public String avrorpc_isRestrictIngestPermissions() throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(new Boolean(this.isRestrictIngestPermissions()));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String avrorpc_isRestrictQueryPermissions() throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(new Boolean(this.isRestrictQueryPermissions()));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }

    }

    @Override
    public String avrorpc_query1(String queryExpressionObject) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.query(this.serializer.deserializeObject(QueryExpression.class,(String) queryExpressionObject)));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String avrorpc_query2(String queryExpressionObject, String catalogIdsObject) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.query(this.serializer.deserializeObject(QueryExpression.class, (String) queryExpressionObject), this.serializer.deserializeObject(Set.class, (String) catalogIdsObject)));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String avrorpc_getNextPage2(String pageObject) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getNextPage(this.serializer.deserializeObject(Page.class,(String) pageObject)));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String avrorpc_getPage2(String pageInfoObject, String queryExpressionObject) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class, (String) pageInfoObject), this.serializer.deserializeObject(QueryExpression.class, (String) queryExpressionObject)));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public String avrorpc_getPage3(String pageInfoObject, String queryExpressionObject, String catalogIdsObject) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getPage(this.serializer.deserializeObject(PageInfo.class,(String) pageInfoObject), this.serializer.deserializeObject(QueryExpression.class,(String) queryExpressionObject), this.serializer.deserializeObject(Set.class,(String) catalogIdsObject)));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }

    }

    @Override
    public String avrorpc_getMetadata(String pageObject) throws AvroRemoteException {
        try {
            return this.serializer.serializeObject(this.getMetadata(this.serializer.deserializeObject(Page.class,(String) pageObject)));
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
    }

    @Override
    public boolean avrorpc_removeCatalog(String catalogUrn) throws AvroRemoteException {
        try {
            this.removeCatalog((String)catalogUrn);
        } catch (Exception e) {
            throw new AvroRemoteException(e.getMessage());
        }
        return true;}

    @Override
    public void startup() throws Exception {
        avrorpc_startup();
    }
}

