package org.apache.oodt.cas.catalog.server.channel.avrorpc;

//OODT imports
import org.apache.oodt.cas.catalog.server.channel.CommunicationChannelClient;
import org.apache.oodt.cas.catalog.server.channel.CommunicationChannelClientFactory;

//JDK imports
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

//FRAMEWORK imports
import org.springframework.beans.factory.annotation.Required;

public class AvrorpcCommunicationChannelClientFactory implements CommunicationChannelClientFactory {
    private static Logger LOG = Logger.getLogger(AvrorpcCommunicationChannelClientFactory.class.getName());

    protected String serverUrl;
    protected int connectionTimeout;
    protected int requestTimeout;
    protected int chunkSize;

    @Override
    public CommunicationChannelClient createCommunicationChannelClient() {
        try {
            return new AvrorpcCommunicationChannelClient(new URL(this.serverUrl), this.connectionTimeout, this.requestTimeout, this.chunkSize);
        }catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create AvrorpcCommunicationChannelClient : " + e.getMessage(), e);
            return null;
        }
    }

    @Required
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getServerUrl() {
        return this.serverUrl;
    }

    /**
     * @param connectionTimeout timeout for client in minutes
     */
    @Required
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     *
     * @param requestTimeout timout for client in minutes
     */
    @Required
    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    @Required
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }
}
