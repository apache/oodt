/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oodt.cas.protocol;

//APACHE imports
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * A path representing a file/directory over some {@link Protocol}
 * 
 * @author bfoster
 */
public class ProtocolFile {

	public static final String SEPARATOR = "/";
	
	private String path;
	private boolean isDir;
	private ProtocolFile parent;
	
	public ProtocolFile(String path, boolean isDir) {
		this(null, path, isDir);
	}
	
	public ProtocolFile(ProtocolFile parent, String path, boolean isDir) {
		this.parent = parent;
		this.isDir = isDir;
		Validate.notNull(path, "ProtocolFile's path cannot be NULL");
		this.path = path.length() > 0 && !path.equals(SEPARATOR) ? StringUtils
				.chomp(path, SEPARATOR) : path;
	}

	/**
	 * True is this path is a directory.
	 * 
	 * @return True if directory, false otherwise
	 */
	public boolean isDir() {
		return isDir;
	}

	/**
	 * Gets the {@link String} representation of this path.
	 * 
	 * @return The {@link String} representation of this path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Gets the name of this file this path represents (i.e. '/path/to/file' will
	 * return 'file')
	 * 
	 * @return The name of the file this path represents
	 */
	public String getName() {
		return path.substring(path.lastIndexOf(SEPARATOR) + 1);
	}

	/**
	 * True if this path is a relative path (i.e. does not start with
	 * {@link SEPARATOR}).
	 * 
	 * @return True is this a relative path, false otherwise
	 */
	public boolean isRelative() {
		return !path.startsWith(SEPARATOR);
	}

	/**
	 * Gets the parent {@link ProtocolFile} for this path.
	 * 
	 * @return The parent {@link ProtocolFile}
	 */
	public ProtocolFile getParent() {
		if (parent != null) {
			return parent;
		} else {
			int index = StringUtils.lastIndexOf(path, SEPARATOR);
			return (index > 0) ? new ProtocolFile(StringUtils.substring(path, 0, index), true) : null;
		}
	}

	/**
	 * Get Absolute pathed {@link ProtocolFile} version of this {@link ProtocolFile}.
	 * 
	 * @return the absolute pathed version of this {@link ProtocolFile}
	 */
	public ProtocolFile getAbsoluteFile() {
		if (this.isRelative()) {
			ProtocolFile parent = this.getParent().getAbsoluteFile();
			if (parent != null) {
				return new ProtocolFile(StringUtils.chomp(parent.getPath(), SEPARATOR)
						+ SEPARATOR + this.getPath(), this.isDir());
			}
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return this.getPath().hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object path) {
		if (path instanceof ProtocolFile) {
			ProtocolFile p = (ProtocolFile) path;
			return (p.getAbsoluteFile().getPath()
					.equals(this.getAbsoluteFile().getPath()) && p.isDir() == this
					.isDir());
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "{parent = '" + this.parent + "', path = '" + path + "', isDir = '" + isDir + "'}";
	}
}
