package org.apache.oodt.pages.curator;

import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.support.PageFactory;

/**
 * Created by bugg on 06/06/16.
 */
public class HomePageTest extends TestBase {

    HomePage homepage;

    @Parameters({ "path" })
    @BeforeClass
    public void testInit(String path) {
        // Load the page in the browser
        webDriver.get(websiteUrl + path);
        homepage = PageFactory.initElements(webDriver, HomePage.class);
    }
    @Test
    public void testH1Existing() throws InterruptedException {
        Assert.assertTrue(homepage.getH1() != null);
    }
    @Test
    public void test2() throws InterruptedException {
        Assert.assertTrue(true);
    }

}
