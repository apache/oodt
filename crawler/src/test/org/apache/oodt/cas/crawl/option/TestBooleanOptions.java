//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.crawl.option;

//Junit imports
import junit.framework.TestCase;

//OODT imports
import gov.nasa.jpl.oodt.cas.crawl.MetExtractorProductCrawler;
import gov.nasa.jpl.oodt.cas.crawl.ProductCrawler; // for javadoc

/**
 * @author mattmann
 * @version $Revision$
 * @since OODT-241
 * 
 * <p>
 * Class ensures that boolean options such as --noRecur and --crawlForDirs are
 * settable in {@link ProductCrawler} derivatives
 * </p>.
 */
public final class TestBooleanOptions extends TestCase {

    public void testSetBooleanOptions() {
        MetExtractorProductCrawler crawler = new MetExtractorProductCrawler();
        try {
            crawler.getClass().getMethod("setNoRecur",
                    new Class[] { boolean.class }).invoke(crawler,
                    new Object[] { new Boolean(true) });
        } catch (Exception e) {
            fail(e.getMessage());
        }

        try {
            crawler.getClass().getMethod("setCrawlForDirs",
                    new Class[] { boolean.class }).invoke(crawler,
                    new Object[] { new Boolean(true) });
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue(crawler.isNoRecur());
        assertTrue(crawler.isCrawlForDirs());

    }

}
