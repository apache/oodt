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
import java.io.File;

//OODT imports
import org.apache.oodt.cas.metadata.exceptions.PreconditionComparatorException;

/**
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * The compator to use when checking file size in {@link PreConEvalUtils}
 * </p>.
 */
public class FileSizeComparator extends PreConditionComparator<Long> {

    @Override
    protected int performCheck(File product, Long compareItem)
            throws PreconditionComparatorException {
        return Long.valueOf(product.length()).compareTo(compareItem);
    }

}
