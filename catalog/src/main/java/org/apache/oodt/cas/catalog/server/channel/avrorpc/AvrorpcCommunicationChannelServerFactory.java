package org.apache.oodt.cas.catalog.server.channel.avrorpc;


//OODT imports
import org.apache.oodt.cas.catalog.server.channel.CommunicationChannelServer;
import org.apache.oodt.cas.catalog.server.channel.CommunicationChannelServerFactory;
import org.apache.oodt.cas.catalog.system.CatalogServiceFactory;

//JDK imports
import java.util.logging.Level;
import java.util.logging.Logger;

//FRAMEWORK imports
import org.springframework.beans.factory.annotation.Required;

public class AvrorpcCommunicationChannelServerFactory implements CommunicationChannelServerFactory {

    private static Logger LOG = Logger.getLogger(AvrorpcCommunicationChannelServerFactory.class.getName());

    protected int port;
    protected CatalogServiceFactory catalogServiceFactory;

    public AvrorpcCommunicationChannelServerFactory() {}

    @Override
    public CommunicationChannelServer createCommunicationChannelServer() {
        try {
            AvrorpcCommunicationChannelServer server = new AvrorpcCommunicationChannelServer();
            server.setCatalogService(this.catalogServiceFactory.createCatalogService());
            server.setPort(this.port);
            return server;
        }catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create AVRO-RPC server : " + e.getMessage(), e);
            return null;
        }
    }

    @Required
    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    @Required
    public void setCatalogServiceFactory(CatalogServiceFactory catalogServiceFactory) {
        this.catalogServiceFactory = catalogServiceFactory;
    }

}
