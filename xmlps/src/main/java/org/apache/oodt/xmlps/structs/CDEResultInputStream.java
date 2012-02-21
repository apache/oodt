package org.apache.oodt.xmlps.structs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

class CDEResultInputStream extends InputStream {

  private final CDEResult res;
  private ByteArrayInputStream rowStream;

  public CDEResultInputStream(CDEResult res) {
    this.res = res;
  }

  private boolean fetchNextRow() throws IOException {
    String s = null;
    try {
      s = res.getNextRowAsString();
    } catch (SQLException e) {
    }
    if (rowStream != null)
      rowStream.close();
    rowStream = s == null ? null : new ByteArrayInputStream(s.getBytes("UTF-8"));
    return rowStream != null;
  }

  private boolean ensureOpen() throws IOException {
    if (rowStream == null || rowStream.available() <= 0)
      return fetchNextRow();
    return true;
  }

  @Override
  public int read() throws IOException {
    return ensureOpen() ? rowStream.read() : -1;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (!ensureOpen())
      return -1;

    if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length)
        || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return 0;
    }

    int total = 0;
    int n = rowStream.read(b, off, len);
    total += n;
    while (n != -1 && total < len) {
      if (!fetchNextRow())
        return total;
      n = rowStream.read(b, off + total, len - total);
      total += n;
    }
    return total;
  }

  @Override
  public void close() throws IOException {
    if (rowStream != null)
      rowStream.close();
    rowStream = null;

    try {
      res.close();
    } catch (SQLException e) {
      throw new IOException(e);
    }
  }

  @Override
  public synchronized int available() throws IOException {
    if (rowStream == null)
      return 0;
    return rowStream.available();
  }

  @Override
  public synchronized void mark(int readlimit) {
    throw new UnsupportedOperationException("Mark not supported");
  }

  @Override
  public synchronized void reset() throws IOException {
    throw new UnsupportedOperationException("Reset not supported");
  }

  @Override
  public boolean markSupported() {
    return false;
  }

}
