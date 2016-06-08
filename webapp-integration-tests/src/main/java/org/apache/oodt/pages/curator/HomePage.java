package org.apache.oodt.pages.curator;

import org.apache.oodt.Page;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

/**
 * Created by bugg on 05/06/16.
 */
public class HomePage extends Page {

    @FindBy(how = How.CLASS_NAME, using = "refresh-tree")
    @CacheLookup
    private WebElement refreshTreeButton;

    @FindBy(how = How.ID, using="tree-view")
    @CacheLookup
    private WebElement treeViewDiv;

    @FindBy(how = How.ID, using="ingest")
    @CacheLookup
    private WebElement ingestButton;

    @FindBy(how = How.ID, using="clear-metadata")
    @CacheLookup
    private WebElement clearMetadataButton;

    @FindBy(how = How.ID, using="ingest-clear-errors")
    @CacheLookup
    private WebElement ingestClearErrors;


    public HomePage(WebDriver webDriver) {
        super(webDriver);
    }

    public void clickRefreshTreeButton() {
        refreshTreeButton.click();
    }

    public WebElement getTreeViewDiv() {
        return treeViewDiv;
    }

    public void clickIngestButton() {
        ingestButton.click();
    }

    public void clickClearMetadataButton() {
        clearMetadataButton.click();
    }

    public void getIngestClearErrors() {
        ingestClearErrors.click();
    }
}
