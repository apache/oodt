package org.apache.oodt.cas.catalog.server.channel.avrorpc;

//OODT imports
import org.apache.oodt.cas.catalog.metadata.TransactionalMetadata;
import org.apache.oodt.cas.catalog.page.*;
import org.apache.oodt.cas.catalog.query.QueryExpression;
import org.apache.oodt.cas.catalog.server.channel.AbstractCommunicationChannelClient;
import org.apache.oodt.cas.catalog.struct.Dictionary;
import org.apache.oodt.cas.catalog.struct.Index;
import org.apache.oodt.cas.catalog.struct.TransactionId;
import org.apache.oodt.cas.catalog.system.Catalog;
import org.apache.oodt.cas.catalog.util.PluginURL;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.avro.ipc.Transceiver;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;

//APACHE imports
import org.apache.avro.ipc.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;



public class AvrorpcCommunicationChannelClient extends AbstractCommunicationChannelClient {
    private Transceiver client;
    private AvroCommunicationChannel proxy;
    protected int chunkSize;

    public AvrorpcCommunicationChannelClient(URL serverUrl, int connectionTimeout, int requestTimeout, int chunkSize) throws IOException {

        this.client  = new NettyTransceiver(new InetSocketAddress(serverUrl.getHost(),serverUrl.getPort()),(long)connectionTimeout);

        this.proxy = (AvroCommunicationChannel) SpecificRequestor.getClient(AvroCommunicationChannel.class, client);
        this.chunkSize = chunkSize;

    }

    @Override
    public void shutdown()   {
        try{
            this.proxy.avrorpc_shutdown();

        }
        catch (Exception e){

        }
    }

    @Override
    public boolean isRestrictQueryPermissions()   {
        try {
            return this.serializer.deserializeObject(Boolean.class, (String) this.proxy.avrorpc_isRestrictQueryPermissions());
        }
        catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean isRestrictIngestPermissions()   {
        try {
            return this.serializer.deserializeObject(Boolean.class, (String) this.proxy.avrorpc_isRestrictIngestPermissions());
        }
        catch(Exception e){
return false;
        }

    }

    @Override
    public void addCatalog(Catalog catalog)   {
        try {
            this.proxy.avrorpc_addCatalog1(this.serializer.serializeObject(catalog));
        }
        catch(Exception e){

        }
    }

    @Override
    public void replaceCatalog(Catalog catalog)   {
        try {
            this.proxy.avrorpc_replaceCatalog(
                    this.serializer.serializeObject(catalog)
            );
        }
        catch(Exception e){

        }
    }

    @Override
    public void addCatalog(String catalogId, Index index)   {
        try {
            this.proxy.avrorpc_addCatalog2(catalogId,
                    this.serializer.serializeObject(index));
        }
        catch(Exception e){

        }
    }

    @Override
    public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries)   {
        try {
            this.proxy.avrorpc_addCatalog3(catalogId,
                    this.serializer.serializeObject(index),
                    this.serializer.serializeObject(dictionaries));
        }
        catch (Exception e){

        }
    }

    @Override
    public void addCatalog(String catalogId, Index index, List<Dictionary> dictionaries, boolean restrictQueryPermission, boolean restrictIngestPermission)   {
        try {
            this.proxy.avrorpc_addCatalog5(catalogId,
                    this.serializer.serializeObject(index),
                    this.serializer.serializeObject(dictionaries),
                    this.serializer.serializeObject(restrictQueryPermission),
                    this.serializer.serializeObject(restrictIngestPermission));
        }
        catch(Exception e){

        }
    }

    @Override
    public void addDictionary(String catalogId, Dictionary dictionary)   {
        try {
            this.proxy.avrorpc_addDictionary(catalogId,
                    this.serializer.serializeObject(dictionary));
        }
        catch (Exception e){

        }
    }

    @Override
    public void replaceDictionaries(String catalogId, List<Dictionary> dictionaries)   {
        try {
            this.proxy.avrorpc_replaceDictionaries(catalogId,
                    this.serializer.serializeObject(dictionaries));
        }
        catch (Exception e){

        }
    }

    @Override
    public void replaceIndex(String catalogId, Index index)   {
        try {
            this.proxy.avrorpc_replaceIndex(catalogId,
                    this.serializer.serializeObject(index));
        }
        catch (Exception e){

        }
    }

    @Override
    public void modifyIngestPermission(String catalogId, boolean restrictIngestPermission)   {
        try {
            this.proxy.avrorpc_modifyIngestPermission(catalogId,
                    this.serializer.serializeObject(restrictIngestPermission));
        } catch (Exception e){

        }
    }

    @Override
    public void modifyQueryPermission(String catalogId, boolean restrictQueryPermission)   {
        try {
            this.proxy.avrorpc_modifyQueryPermission(catalogId,
                    this.serializer.serializeObject(restrictQueryPermission));
        }
        catch (Exception e){

        }
    }

    @Override
    public void removeCatalog(String catalogUrn)   {
        try {
            this.proxy.avrorpc_removeCatalog(catalogUrn);
        }
        catch (Exception e){

        }
    }

    @Override
    public List<PluginURL> getPluginUrls()   {
        try {
            return this.serializer.deserializeObject(List.class, (String) this.proxy.avrorpc_getPluginUrls());
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public void addPluginUrls(List<PluginURL> pluginUrls)   {
        try {
            this.proxy.avrorpc_addPluginUrls(this.serializer.serializeObject(pluginUrls));
        }
        catch (Exception e){

        }
    }

    @Override
    public URL getPluginStorageDir()   {
        try {
            return this.serializer.deserializeObject(URL.class, this.proxy.avrorpc_getPluginStorageDir());
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public void transferUrl(URL fromUrl, URL toURL)   {
        System.out.println("Transfering '" + fromUrl + "' to '" + toURL + "'");
        FileInputStream is = null;
        try {
            byte[] buf = new byte[this.chunkSize];
            is = new FileInputStream(new File(fromUrl.getPath()));
            int offset = 0;
            int numBytes = 0;
            while ((numBytes = is.read(buf, offset, chunkSize)) != -1)
                this.transferFile(new File(toURL.getPath()).getAbsolutePath(), buf, offset, numBytes);
        }catch (Exception e) {

        }finally {
            try {
                is.close();
            }catch(Exception e) {}
        }
    }

    protected void transferFile(String filePath, byte[] fileData, int offset,
                                int numBytes)   {
        try {
            this.proxy.avrorpc_transferFile(filePath, ByteBuffer.wrap(fileData), offset, numBytes);
        }
        catch (Exception e){

        }
    }

    @Override
    public Set<String> getCurrentCatalogIds() {
        try {
            return this.serializer.deserializeObject(Set.class, (String) this.proxy.avrorpc_getCurrentCatalogIds());
        }
        catch (Exception e){
            return null;
        }

    }

    @Override
    public TransactionReceipt ingest(Metadata metadata) {
        try {
            return this.serializer.deserializeObject(TransactionReceipt.class, (String) this.proxy.avrorpc_ingest(this.serializer.serializeObject(metadata)));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public void delete(Metadata metadata) {
        try {
            this.proxy.avrorpc_delete(this.serializer.serializeObject(metadata));
        }
        catch (Exception e){

        }
    }

    @Override
    public List<String> getProperty(String key) {
        try {
            return this.serializer.deserializeObject(List.class, (String) this.proxy.avrorpc_getProperty(key));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public Properties getCalalogProperties() {
        try {
            return this.serializer.deserializeObject(Properties.class, (String) this.proxy.avrorpc_getCalalogProperties1());
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public Properties getCalalogProperties(String catalogUrn) {
        try {
            return this.serializer.deserializeObject(Properties.class, (String) this.proxy.avrorpc_getCalalogProperties2(catalogUrn));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public Page getNextPage(Page page) {
        try {
            return this.serializer.deserializeObject(Page.class, (String) this.proxy.avrorpc_getNextPage2(this.serializer.serializeObject(page)));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public Page getPage(PageInfo pageInfo, QueryExpression queryExpression) {
        try {
            return this.serializer.deserializeObject(Page.class, (String) this.proxy.avrorpc_getPage2(
                    this.serializer.serializeObject(pageInfo), this.serializer.serializeObject(queryExpression)));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public Page getPage(PageInfo pageInfo, QueryExpression queryExpression, Set<String> catalogIds) {
        try {
            return this.serializer.deserializeObject(Page.class, (String) this.proxy.avrorpc_getPage3(
                    this.serializer.serializeObject(pageInfo),
                    this.serializer.serializeObject(queryExpression),
                    this.serializer.serializeObject(catalogIds)
            ));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public List<TransactionalMetadata> getMetadata(Page page) {
        try {
            return this.serializer.deserializeObject(List.class, (String) this.proxy.avrorpc_getMetadata(
                    this.serializer.serializeObject(page)
            ));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public QueryPager query(QueryExpression queryExpression) {
        try {
            return this.serializer.deserializeObject(QueryPager.class, (String) this.proxy.avrorpc_query1(
                    this.serializer.serializeObject(queryExpression)
            ));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public QueryPager query(QueryExpression queryExpression, Set<String> catalogIds) {
        try {
            return this.serializer.deserializeObject(QueryPager.class, (String) this.proxy.avrorpc_query2(
                    this.serializer.serializeObject(queryExpression),
                    this.serializer.serializeObject(catalogIds)
            ));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public List<TransactionalMetadata> getNextPage(QueryPager queryPager) {
        try {
            return this.serializer.deserializeObject(List.class, (String) this.proxy.avrorpc_getNextPage(
                    this.serializer.serializeObject(queryPager)
            ));
        }
        catch (Exception e){
            return null;
        }
    }
     // somethings is wrong
    @Override
    public List<TransactionId<?>> getTransactionIdsForAllPages(QueryPager queryPager) {
        return null;
    }

    @Override
    public List<TransactionalMetadata> getAllPages(QueryPager queryPager) {
        try {
            return this.serializer.deserializeObject(List.class, (String) this.proxy.avrorpc_getAllPages(
                    this.serializer.serializeObject(queryPager)
            ));
        }
        catch (Exception e){
            return null;
        }
    }

    @Override
    public List<TransactionalMetadata> getMetadataFromTransactionIdStrings(List<String> catalogServiceTransactionIdStrings) {
        try {
            return this.serializer.deserializeObject(List.class, (String) this.proxy.avrorpc_getMetadataFromTransactionIdStrings(
                    this.serializer.serializeObject(catalogServiceTransactionIdStrings)
            ));
        }
        catch (Exception e){
return null;
        }
    }

    @Override
    public List<TransactionalMetadata> getMetadataFromTransactionIds(List<TransactionId<?>> catalogServiceTransactionIds) {
        try {
            return this.serializer.deserializeObject(List.class, (String) this.proxy.avrorpc_getMetadataFromTransactionIds(
                    this.serializer.serializeObject(catalogServiceTransactionIds)
            ));
        }
        catch (Exception e){
return null;
        }
    }

    @Override
    public List<TransactionId<?>> getCatalogServiceTransactionIds(List<TransactionId<?>> catalogTransactionIds, String catalogUrn) {
        try {
            return this.serializer.deserializeObject(List.class, (String) this.proxy.avrorpc_getCatalogServiceTransactionId2(
                    this.serializer.serializeObject(catalogTransactionIds),
                    catalogUrn
            ));
        }
        catch (Exception e){
         return null;
        }
    }

    @Override
    public TransactionId<?> getCatalogServiceTransactionId(TransactionId<?> catalogTransactionId, String catalogUrn) {
        try {
            return this.serializer.deserializeObject(TransactionId.class, (String) this.proxy.avrorpc_getCatalogServiceTransactionId2(
                    this.serializer.serializeObject(catalogTransactionId),
                    catalogUrn
            ));
        }
        catch (Exception e){
            return
                    null;
        }
    }

    @Override
    public TransactionId<?> getCatalogServiceTransactionId(CatalogReceipt catalogReceipt, boolean generateNew) {
        try {
            return this.serializer.deserializeObject(TransactionId.class, (String) this.proxy.avrorpc_getCatalogServiceTransactionId2(
                    this.serializer.serializeObject(catalogReceipt),
                    this.serializer.serializeObject(generateNew)
            ));
        }
        catch (Exception e){
            return null;
        }
    }
}
