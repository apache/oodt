package org.apache.oodt.cas.filemgr.structs.query;

import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class TestQueryResult extends TestCase {

  private QueryResult qr;

  @Override
  protected void setUp() throws Exception {
    Product p = new Product();
    p.setProductId("TestProductId");
    Metadata m = new Metadata();
    m.addMetadata(CoreMetKeys.FILENAME, "datafile.dat");
    m.addMetadata("Owners", Arrays.asList("Bob", "Billy"));
    qr = new QueryResult(p, m);
  }

  public void testToString() {
    String str = qr.toString();
    assertTrue(str.contains("datafile.dat"));
    assertTrue(str.contains("Bob,Billy"));
    assertEquals(",", str.replace("datafile.dat", "").replace("Bob,Billy",""));
  }

  public void testToStringFormat() {
    qr.setToStringFormat("$Owners,$Filename");
    assertEquals("Bob,Billy,datafile.dat", qr.toString());

    qr.setToStringFormat("$Filename\t$Owners");
    List<String> list = Arrays.asList(qr.toString().split("\t"));
    assertEquals(2, list.size());
    assertEquals("datafile.dat", list.get(0));
    assertEquals("Bob,Billy", list.get(1));
  }

  public void testToStringEmpty() {
    qr.setMetadata(new Metadata());
    assertEquals("", qr.toString());
  }

  public void testEquals() {
    assertEquals(qr, qr);
    assertFalse(qr.equals(null));
    assertFalse(qr.equals(0));

    QueryResult metnull = new QueryResult(qr.getProduct(), null);
    assertFalse(metnull.equals(qr));
    assertFalse(qr.equals(metnull));
    assertEquals(metnull, new QueryResult(qr.getProduct(), null));

    QueryResult prodnull = new QueryResult(null, new Metadata(qr.getMetadata()));
    assertFalse(prodnull.equals(qr));
    assertFalse(qr.equals(prodnull));
    assertEquals(prodnull, new QueryResult(null, new Metadata(qr.getMetadata())));

    QueryResult equal = new QueryResult(qr.getProduct(), new Metadata(qr.getMetadata()));
    assertEquals(qr, equal);
    assertEquals(equal, qr);

    QueryResult fmt = new QueryResult(qr.getProduct(), new Metadata(qr.getMetadata()));
    fmt.setToStringFormat("blah");
    assertFalse(qr.equals(fmt));
    assertFalse(fmt.equals(qr));

    QueryResult fmt2 = new QueryResult(qr.getProduct(), new Metadata(qr.getMetadata()));
    fmt2.setToStringFormat("blah");
    assertEquals(fmt, fmt2);
    assertEquals(fmt2, fmt);
  }

}
