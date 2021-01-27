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


package org.apache.oodt.cas.metadata.preconditions;

//JDK imports

import org.apache.oodt.cas.metadata.exceptions.PreconditionComparatorException;
import org.apache.oodt.cas.metadata.util.MimeTypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

//OODT imports

/**
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A {@link PreConditionComparator} that uses Apache Tika for Mime detection
 * </p>.
 */
public class MimeTypeComparator extends PreConditionComparator<String> {
    private static Logger LOG = LoggerFactory.getLogger(MimeTypeComparator.class);
    private boolean useMagic;

    private MimeTypeUtils mimeTypeUtils = new MimeTypeUtils();

    public MimeTypeComparator() {
        super();
        this.useMagic = false;
    }

    @Override
    protected int performCheck(File product, String mimeType)
            throws PreconditionComparatorException {
        try {
            String tikaMimeType = this.mimeTypeUtils.getMimeType(product);
            if (tikaMimeType == null && useMagic) {
                tikaMimeType = this.mimeTypeUtils
                    .getMimeTypeByMagic(MimeTypeUtils
                        .readMagicHeader(new FileInputStream(product)));
            }
            return tikaMimeType != null ? tikaMimeType.compareTo(mimeType) : 0;
        } catch (Exception e) {
            String msg = String.format("Failed to get mimetype for product [%s]: %s", product, e.getMessage());
            LOG.error(msg, e);
            throw new PreconditionComparatorException(msg);
        }
    }

    public void setMimeTypeRepo(String mimeTypeRepo) {
        try {
            mimeTypeUtils = new MimeTypeUtils(mimeTypeRepo);
        } catch (Exception e) {
            mimeTypeUtils = new MimeTypeUtils();
        }
    }

    public void setUseMagic(boolean useMagic) {
        this.useMagic = useMagic;
    }

}
