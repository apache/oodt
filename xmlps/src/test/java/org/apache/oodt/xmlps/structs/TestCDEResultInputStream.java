package org.apache.oodt.xmlps.structs;

import static org.easymock.EasyMock.expect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

public class TestCDEResultInputStream extends TestCase {
  
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
  private CDEResultInputStream in;
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
    in = new CDEResultInputStream(result);
  }
  
 @Override
  protected void tearDown() throws Exception {
    in.close();
  }
  
  public void testRead() {
    String expected = ID + FS + LAST + FS + FIRST + FS + DOB + RS;
    String expected1 = ID1 + FS + LAST1 + FS + FIRST1 + FS + DOB1 + RS;
    try {
      for (int i = 0; i < expected.length(); i++) {
        assertEquals((int)expected.charAt(i), in.read());
      }
      for (int i = 0; i < expected1.length(); i++) {
        assertEquals((int)expected1.charAt(i), in.read());
      }
      assertEquals(-1, in.read());
    } catch (IOException e) {
      fail("IOException: " + e.getMessage());
    }
  }
  
  public void testReadCharArrayIntInt() {
    byte[] buf = new byte[128];
    int n = 0;
    int length = 0;
    String expected = null;
    
    try {
      expected = ID + FS + LAST;
      length = expected.length();
      n = in.read(buf, 0, length);
      assertEquals(length, n);
      assertEquals(expected, new String(buf, 0, n));

      expected = FS + FIRST;
      length = expected.length();
      n = in.read(buf, 0, length);
      assertEquals(length, n);
      assertEquals(expected, new String(buf, 0, n));

      expected = FS + DOB.substring(0, length-1);
      length = expected.length();
      n = in.read(buf, 0, length);
      assertEquals(length, n);
      assertEquals(expected, new String(buf, 0, n));
      
      expected = DOB.substring(4) + RS + ID1 + FS + LAST1 + FS + FIRST1 + FS + DOB1 + RS;
      length = buf.length;
      n = in.read(buf, 0, length);
      assertEquals(expected.length(), n);
      assertEquals(expected, new String(buf, 0, n));
      
      n = in.read(buf, 0, 10);
      assertEquals(-1, n);
    } catch (IOException e) {
      fail("IOException: " + e.getMessage());
    }
  }

}
