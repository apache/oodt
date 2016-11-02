//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.apache.oodt.cas.filemgr.system;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.*;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.entity.ByteArrayEntity;

import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.xmlrpc.XmlRpcClientException;
import org.apache.xmlrpc.XmlRpcTransport;

public class CommonsXmlRpcTransport implements XmlRpcTransport {
    private URL url;
    private HttpClient client;
    private Header userAgentHeader;
    private boolean http11;
    private boolean gzip;
    private boolean rgzip;
    private Credentials creds;
    protected HttpPost method;
    private int timeout = 10;
    private int connecttimeout = 10;
    private String password;
    private String user;
    private String auth;

    public CommonsXmlRpcTransport(URL url, HttpClient client) {
        this.userAgentHeader = new BasicHeader("User-Agent", "Apache XML-RPC 2.0");
        this.http11 = false;
        this.gzip = false;
        this.rgzip = false;
        this.url = url;
        if(client == null) {
            RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout(timeout * 1000)
                    .setConnectTimeout(connecttimeout * 1000)
                    .build();
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            if(!user.equals(null)) {
                credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user));
            }
            else if(!user.equals(null)&& !password.equals(null)){
                credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
            }
            else if(!auth.equals(null)){

            }

            System.out.println("building empty registry");
            Registry<AuthSchemeProvider> r = RegistryBuilder.<AuthSchemeProvider>create().build();
            HttpClient newClient = HttpClients.custom().setDefaultAuthSchemeRegistry(r).setDefaultCredentialsProvider(credsProvider).setDefaultRequestConfig(config).build();
            this.client = newClient;
        } else {
            this.client = client;
        }

    }

    public CommonsXmlRpcTransport(URL url) {
        this(url, (HttpClient)null);
    }



    public InputStream sendXmlRpc(byte[] request) throws IOException, XmlRpcClientException {
        this.method = new HttpPost(this.url.toString());
        //this.method.setHttp11(this.http11);
        this.method.setHeader(new BasicHeader("Content-Type", "text/xml"));
        if(this.rgzip) {
            this.method.setHeader(new BasicHeader("Content-Encoding", "gzip"));
        }

        if(this.gzip) {
            this.method.setHeader(new BasicHeader("Accept-Encoding", "gzip"));
        }

        this.method.setHeader(this.userAgentHeader);
        if(this.rgzip) {
            ByteArrayOutputStream hostURI = new ByteArrayOutputStream();
            GZIPOutputStream hostConfig = new GZIPOutputStream(hostURI);
            hostConfig.write(request);
            hostConfig.finish();
            hostConfig.close();
            byte[] lgzipo = hostURI.toByteArray();

            HttpEntity entity = new ByteArrayEntity(lgzipo);


            this.method.setEntity(entity);

            //this.method.setRequestContentLength(-1);
        } else {
            HttpEntity entity = new ByteArrayEntity(request);
            this.method.setEntity(entity);
//            this.method.setRequestBody(new ByteArrayInputStream(request));
        }

        URI hostURI1 = null;
        try {
            hostURI1 = new URI(this.url.toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpHost hostConfig1 = new HttpHost(hostURI1.toString());
        HttpResponse response = this.client.execute(this.method);
        boolean lgzipo1 = false;
        Header[] lHeader = response.getHeaders("Content-Encoding");
        if(lHeader != null && lHeader.length>0) {
            String lValue = lHeader[0].getValue();
            if(lValue != null) {
                lgzipo1 = lValue.indexOf("gzip") >= 0;
            }
        }

        return (InputStream)(lgzipo1?new GZIPInputStream(response.getEntity().getContent()):response.getEntity().getContent());
    }

    public void setHttp11(boolean http11) {
        this.http11 = http11;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public void setRGzip(boolean gzip) {
        this.rgzip = gzip;
    }

    public void setUserAgent(String userAgent) {
        this.userAgentHeader =new BasicHeader("User-Agent", userAgent);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setConnectionTimeout(int ctimeout) {
        this.connecttimeout = ctimeout;
    }

    public void setBasicAuthentication(String user, String password) {
        this.user = user;
        this.password = password;

    }

    public void setBasicAuthentication(String auth) {
        this.auth = auth;
    }

    public void endClientRequest() throws XmlRpcClientException {
        this.method.releaseConnection();
    }
}
