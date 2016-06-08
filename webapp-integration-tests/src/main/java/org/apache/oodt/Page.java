package org.apache.oodt;

import org.openqa.selenium.WebDriver;

/**
 * Created by bugg on 05/06/16.
 */
public abstract class Page {
    protected WebDriver webDriver;
    /*
     * Constructor injecting the WebDriver interface
     *
     * @param webDriver
     */
    public Page(WebDriver webDriver) {
        this.webDriver = webDriver;
    }
    public WebDriver getWebDriver() {
        return webDriver;
    }
    public String getTitle() {
        return webDriver.getTitle();
    }

}
