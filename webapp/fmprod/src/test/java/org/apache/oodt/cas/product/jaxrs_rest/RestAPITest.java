package org.apache.oodt.cas.product.jaxrs_rest;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.oodt.cas.product.jaxrs.resources.ProductResource;
import org.apache.oodt.cas.product.jaxrs.services.CasProductJaxrsService;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RestAPITest extends Assert {
//    private final static String ENDPOINT_ADDRESS = "http://localhost:8080/cas_product_war/jaxrs/product?productId=" +"";
//    private static Server server;
//
//    @BeforeClass
//    public static void initialize() throws Exception {
//        startServer();
//    }
//
//    private static void startServer() throws Exception {
//        JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();
//        sf.setResourceProvider(CasProductJaxrsService.class,
//        new SingletonResourceProvider(new CasProductJaxrsService(), true));
//        sf.setResourceClasses(CasProductJaxrsService.class);
//        sf.setAddress(ENDPOINT_ADDRESS);
//        server = sf.create();
//    }
//
//    @AfterClass
//    public static void destroy() {
//        server.stop();
//        server.destroy();
//    }
//
//    @Test
//    public void testGetProductResourceWithWebClient() {
//        WebClient client = WebClient.create(ENDPOINT_ADDRESS);
//        ProductResource productResource = client.get(ProductResource.class);
//        assertEquals(0, productResource.getProductId());
//
//    }
}
