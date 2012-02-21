package org.apache.oodt.xmlps.structs;

import static org.easymock.EasyMock.expect;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

public class TestCDEResult extends TestCase {

  private static final String ID = "123";
  private static final String LAST = "doe";
  private static final String FIRST = "john";
  private static final String DOB = "1980-01-01 00:00:00.0";

  private static final String ID1 = "321";
  private static final String LAST1 = "smith";
  private static final String FIRST1 = "jane";
  private static final String DOB1 = "1990-01-01 00:00:00.0";

  private static final String FS = "\t";
  private static final String RS = "$";

  private ResultSet rs;
  private Connection con;
  private ResultSetMetaData rsmet;
  private CDEResult result;
  private IMocksControl ctrl;

  @Override
  protected void setUp() throws Exception {
    ctrl = EasyMock.createNiceControl();
    rs = ctrl.createMock(ResultSet.class);
    con = ctrl.createMock(Connection.class);
    rsmet = ctrl.createMock(ResultSetMetaData.class);
    expect(rs.next()).andReturn(true);
    expect(rsmet.getColumnCount()).andReturn(4).anyTimes();
    expect(rs.getMetaData()).andReturn(rsmet).anyTimes();
    expect(rs.getString(1)).andReturn(ID);
    expect(rs.getString(2)).andReturn(LAST);
    expect(rs.getString(3)).andReturn(FIRST);
    expect(rs.getString(4)).andReturn(DOB);
    expect(rs.next()).andReturn(true);
    expect(rs.getString(1)).andReturn(ID1);
    expect(rs.getString(2)).andReturn(LAST1);
    expect(rs.getString(3)).andReturn(FIRST1);
    expect(rs.getString(4)).andReturn(DOB1);
    expect(rs.next()).andReturn(false);
    ctrl.replay();
    result = new CDEResult(rs,con);
  }

  public void testGetInputStream() {
    InputStream in = null;
    try {
      in = result.getInputStream();
    } catch (IOException e) {
      fail("Could not get inputstream: " + e.getMessage());
    }
    assertNotNull(in);
    assertEquals(CDEResultInputStream.class, in.getClass());

    boolean thrown = false;
    try {
      in = new CDEResult(null, con).getInputStream();
    } catch (IOException e) {
      thrown = true;
    }
    assertTrue("InputStream should throw IOException with null ResultSet!", thrown);

    thrown = false;
    try {
      in = new CDEResult(rs, null).getInputStream();
    } catch (IOException e) {
      thrown = true;
    }
    assertTrue("InputStream should throw IOException with null Connection!", thrown);
  }

  public void testGetNextRowAsString() {
    try {
      assertEquals(ID + FS + LAST + FS + FIRST + FS + DOB + RS, result.getNextRowAsString());
      assertEquals(ID1 + FS + LAST1 + FS + FIRST1 + FS + DOB1 + RS, result.getNextRowAsString());
    } catch (SQLException e) {
      fail("Could not get next row: " + e.getMessage());
    }
  }

  public void testGetMimeType() {
    assertEquals("text/plain", result.getMimeType());
  }

  public void testGetSize() {
    assertEquals(-1, result.getSize());
  }

}
