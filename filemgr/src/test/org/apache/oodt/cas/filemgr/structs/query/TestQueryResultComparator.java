package org.apache.oodt.cas.filemgr.structs.query;

import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

public class TestQueryResultComparator extends TestCase {

  private QueryResult qr1;
  private QueryResult qr2;
  private QueryResult qrfilename;
  private QueryResult qrowners;
  private QueryResult qrnull;
  private QueryResult qrnull2;

  @Override
  protected void setUp() throws Exception {
    Product p = new Product();
    p.setProductId("TestProductID");
    Metadata m = new Metadata();
    m.addMetadata(CoreMetKeys.FILENAME, "datafile.dat");
    m.addMetadata("Owners", Arrays.asList("Chad", "Cam"));
    qr1 = new QueryResult(p, m);

    Metadata m2 = new Metadata();
    m2.addMetadata(CoreMetKeys.FILENAME, "textfile.dat");
    m2.addMetadata("Owners", Arrays.asList("Bob", "Billy"));
    qr2 = new QueryResult(p, m2);

    Metadata filename = new Metadata();
    filename.addMetadata(CoreMetKeys.FILENAME, "foo.txt");
    qrfilename = new QueryResult(p, filename);

    Metadata owners = new Metadata();
    owners.addMetadata("Owners", Arrays.asList("Dave", "Dan"));
    qrowners = new QueryResult(p, owners);

    qrnull = new QueryResult(p, new Metadata());
    qrnull2 = new QueryResult(p, new Metadata());
  }

  public void testCompare() {
    List<QueryResult> list = Arrays.asList(qr1, qrowners, qrnull2, qr2, qrnull, qrfilename);
    QueryResultComparator c = new QueryResultComparator();
    c.setSortByMetKey(CoreMetKeys.FILENAME);

    Collections.sort(list, c);
    assertEquals(qr1, list.get(0));
    assertEquals(qrfilename, list.get(1));
    assertEquals(qr2, list.get(2));
    List<QueryResult> sub = list.subList(3, list.size());
    assertEquals(3, sub.size());
    assertTrue(sub.contains(qrnull));
    assertTrue(sub.contains(qrnull2));
    assertTrue(sub.contains(qrowners));

    c.setSortByMetKey("Owners");
    Collections.sort(list, c);
    assertEquals(qr2, list.get(0));
    assertEquals(qr1, list.get(1));
    assertEquals(qrowners, list.get(2));
    sub = list.subList(3, list.size());
    assertEquals(3, sub.size());
    assertTrue(sub.contains(qrfilename));
    assertTrue(sub.contains(qrnull));
    assertTrue(sub.contains(qrnull2));
  }

}
