// Copyright 2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: NullInputStream.java,v 1.1.1.1 2004-02-28 13:09:15 kelly Exp $

package jpl.eda.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that's always empty.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public final class NullInputStream extends InputStream {
        /**
	 * Construct a null input stream.
         */
        public NullInputStream() {
                open = true;
        }

        /**
	 * Read a byte, which you can't do, so you always get -1 to indicate end-of-file.
         *
         * @return -1 to indicate end of file.
         * @throws IOException If the stream is closed.
         */
        public int read() throws IOException {
                checkOpen();
                return -1;
        }

        public void close() throws IOException {
                checkOpen();
                open = false;
        }

        /**
	 * Check if we're open.
         *
         * @throws IOException If we're not open.
         */
        private void checkOpen() throws IOException {
                if (!open) throw new IOException("Stream closed");
        }

        /** Is the stream open? */
        private boolean open;
}
