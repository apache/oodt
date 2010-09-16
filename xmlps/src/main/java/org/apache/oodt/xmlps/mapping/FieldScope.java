/**
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

package org.apache.oodt.xmlps.mapping;

/**
 * 
 * <p>
 * Defines the scope of a {@link MappingField}.
 * </p>.
 */
public abstract class FieldScope {

    public static FieldScope QUERY = new QueryFieldScope();

    public static FieldScope RETURN = new ReturnFieldScope();

    private static final String SCOPE_QUERY = "query";

    private static final String SCOPE_RETURN = "return";

    protected abstract String getType();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FieldScope)) {
            return false;
        }

        FieldScope other = (FieldScope) o;
        return this.getType().equals(other.getType());
    }

    private static final class QueryFieldScope extends FieldScope {

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.oodt.xmlps.mapping.FieldScope#getType()
         */
        @Override
        protected String getType() {
            return SCOPE_QUERY;
        }

    }

    private static final class ReturnFieldScope extends FieldScope {

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.oodt.xmlps.mapping.FieldScope#getType()
         */
        @Override
        protected String getType() {
            return SCOPE_RETURN;
        }

    }

}
