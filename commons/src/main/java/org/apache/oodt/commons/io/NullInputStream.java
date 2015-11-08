// Licensed to the Apache Software Foundation (ASF) under one or more contributor
// license agreements.  See the NOTICE.txt file distributed with this work for
// additional information regarding copyright ownership.  The ASF licenses this
// file to you under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy of
// the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations under
// the License.

package org.apache.oodt.commons.io;

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
                if (!open) {
                        throw new IOException("Stream closed");
                }
        }

        /** Is the stream open? */
        private boolean open;
}
