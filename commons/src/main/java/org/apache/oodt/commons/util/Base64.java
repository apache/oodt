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

package org.apache.oodt.commons.util;

import org.apache.oodt.commons.io.Base64DecodingInputStream;
import org.apache.oodt.commons.io.Base64EncodingOutputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Base 64 encoding and decoding.
 *
 * This class provides methods for RFC-1521 specified "base 64" encoding and decoding of
 * arbitrary data.  Pass a byte array into the {@link #encode} method and you'll get a
 * byte array result where all of the bytes are printable ASCII values.  Pass that result
 * into {@link #decode} and you'll get your original byte array.
 *
 * <p>Sincere thanks to Tom Daley for providing a sample encoder algorithm and a great
 * explanation of how RFC-1521 is supposed to work.
 *
 * @author Kelly
 */
public class Base64 {
	/** Encode into base 64.
	 *
	 * Encode the given data into RFC-1521 base 64.  Encoding a null array gives a
	 * null result.
	 *
	 * @param data The data to encode.
	 * @return Base-64 encoded <var>data</var>.
	 */
	public static byte[] encode(final byte[] data) {
		return encode(data, 0, data.length);
	}

	/** Encode into base 64.
	 *
	 * Encode the given data into RFC-1521 base 64.  Encoding a null array gives a
	 * null result.  Start encoding at the given offset and go for the given amount of
	 * bytes.
	 *
	 * @param data The data to encode.
	 * @param offset Where to start looking for data to encode.
	 * @param length How much data to encode.
	 * @return Base-64 encoded <var>data</var>
	 */
	public static byte[] encode(final byte[] data, int offset, int length) {
		if (data == null) {
		  return null;
		}
		if (offset < 0 || offset > data.length) {
		  throw new IndexOutOfBoundsException("Can't encode at index " + offset + " which is beyond array bounds 0.."
											  + data.length);
		}
		if (length < 0) {
		  throw new IllegalArgumentException("Can't encode a negative amount of data");
		}
		if (offset + length > data.length) {
		  throw new IndexOutOfBoundsException("Can't encode beyond right edge of array");
		}
		
		int i, j;
		byte dest[] = new byte[((length+2)/3)*4];

		// Convert groups of 3 bytes into 4.
		for (i = offset, j = 0; i < offset + length - 2; i += 3) {
			dest[j++] = (byte) ((data[i] >>> 2) & 077);
			dest[j++] = (byte) ((data[i+1] >>> 4) & 017 | (data[i] << 4) & 077);
			dest[j++] = (byte) ((data[i+2] >>> 6) & 003 | (data[i+1] << 2) & 077);
			dest[j++] = (byte) (data[i+2] & 077);
		}

		// Convert any leftover bytes.
		if (i < offset + length) {
			dest[j++] = (byte) ((data[i] >>> 2) & 077);
			if (i < offset + length - 1) {
				dest[j++] = (byte) ((data[i+1] >>> 4) & 017 | (data[i] << 4) & 077);
				dest[j++] = (byte) ((data[i+1] << 2) & 077);
			} else {
			  dest[j++] = (byte) ((data[i] << 4) & 077);
			}
		}

		// Now, map those onto base 64 printable ASCII.
		for (i = 0; i <j; i++) {
			if      (dest[i] < 26) {
			  dest[i] = (byte) (dest[i] + 'A');
			} else if (dest[i] < 52) {
			  dest[i] = (byte) (dest[i] + 'a' - 26);
			} else if (dest[i] < 62) {
			  dest[i] = (byte) (dest[i] + '0' - 52);
			} else if (dest[i] < 63) {
			  dest[i] = (byte) '+';
			} else {
			  dest[i] = (byte) '/';
			}
		}

		// Pad the result with and we're done.
		for (; i < dest.length; i++) {
		  dest[i] = (byte) '=';
		}
		return dest;
	}

	/** Decode from base 64.
	 *
	 * Decode the given RFC-1521 base 64 encoded bytes into the original data they
	 * represent.  Decoding null data gives a null result.
	 *
	 * @param data Base-64 encoded data to decode.
	 * @return Decoded <var>data</var>.
	 */
	public static byte[] decode(final byte[] data) {
		return decode(data, 0, data.length);
	}

	/** Decode from base 64.
	 *
	 * Decode the given RFC-1521 base 64 encoded bytes into the original data they
	 * represent.  Decoding null data gives a null result.
	 *
	 * @param data Base-64 encoded data to decode.
	 * @param offset Where to start looking for data to decode.
	 * @param length How much data to decode.
	 * @return Decoded <var>data</var>.
	 */
	public static byte[] decode(final byte[] data, int offset, int length) {
		if (data == null) {
		  return null;
		}
		if (offset < 0 || offset >= data.length) {
		  throw new IndexOutOfBoundsException("Can't decode at index " + offset + " which is beyond array bounds 0.."
											  + (data.length - 1));
		}
		if (length < 0) {
		  throw new IllegalArgumentException("Can't decode a negative amount of data");
		}
		if (offset + length > data.length) {
		  throw new IndexOutOfBoundsException("Can't decode beyond right edge of array");
		}

		// Ignore any padding at the end.
		int tail = offset + length - 1;
		while (tail >= offset && data[tail] == '=') {
		  --tail;
		}
		byte dest[] = new byte[tail + offset + 1 - length/4];

		// First, convert from base-64 ascii to 6 bit bytes.
		for (int i = offset; i < offset+length; i++) {
			if      (data[i] == '=') {
			  data[i] = 0;
			} else if (data[i] == '/') {
			  data[i] = 63;
			} else if (data[i] == '+') {
			  data[i] = 62;
			} else if (data[i] >= '0' && data[i] <= '9') {
			  data[i] = (byte) (data[i] - ('0' - 52));
			} else if (data[i] >= 'a'  &&  data[i] <= 'z') {
			  data[i] = (byte) (data[i] - ('a' - 26));
			} else if (data[i] >= 'A'  &&  data[i] <= 'Z') {
			  data[i] = (byte) (data[i] - 'A');
			}
		}

		// Map those from 4 6-bit byte groups onto 3 8-bit byte groups.
		int i, j;
		for (i = 0 + offset, j = 0; j < dest.length - 2; i += 4, j += 3) {
			dest[j]   = (byte) (((data[i] << 2) & 255) | ((data[i+1] >>> 4) & 003));
			dest[j+1] = (byte) (((data[i+1] << 4) & 255) | ((data[i+2] >>> 2) & 017));
			dest[j+2] = (byte) (((data[i+2] << 6) & 255) | (data[i+3] & 077));
		}

		// And get the leftover ...
		if (j < dest.length) {
		  dest[j] = (byte) (((data[i] << 2) & 255) | ((data[i + 1] >>> 4) & 003));
		}
		if (++j < dest.length) {
		  dest[j] = (byte) (((data[i + 1] << 4) & 255) | ((data[i + 2] >>> 2) & 017));
		}

		// That's it.
		return dest;
	}

	/** This class provides namespace for utility methods and shouldn't be instantiated.
	 */
	private Base64() {
		throw new IllegalStateException(getClass().getName() + " should not be instantiated");
	}

	/** Command-line runner that encodes or decodes.
	 *
	 * @param argv Command-line arguments.
	 */
	public static void main(String[] argv) throws IOException {
		if (argv.length < 1 || argv.length > 2) {
			System.err.println("Usage: encode|decode [file]");
			System.exit(1);
		}
		boolean encode = true;
		if ("encode".equals(argv[0])) {
		  encode = true;
		} else if ("decode".equals(argv[0])) {
		  encode = false;
		} else {
			System.err.println("Specify either \"encode\" or \"decode\"");
			System.exit(1);
		}
		InputStream source = argv.length == 2? new BufferedInputStream(new FileInputStream(argv[1])) : System.in;
		InputStream in;
		OutputStream out;
		if (encode) {
			in = source;
			out = new Base64EncodingOutputStream(System.out);
		} else {
			in = new Base64DecodingInputStream(source);
			out = System.out;
		}
		byte[] buf = new byte[512];
		int numRead;
		while ((numRead = in.read(buf)) != -1) {
		  out.write(buf, 0, numRead);
		}
		in.close();
		out.close();
		System.exit(0);
	}
}

			
