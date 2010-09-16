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
 * Defines the type of a {@link MappingField}
 * </p>.
 */
public abstract class FieldType {

    private static final String TYPE_DYNAMIC = "dynamic";

    private static final String TYPE_CONSTANT = "constant";

    public static final FieldType DYNAMIC = new DynamicFieldType();

    public static final FieldType CONSTANT = new ConstantFieldType();

    protected abstract String getType();
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof FieldType)){
            return false;
        }
        
        FieldType other = (FieldType)o;
        return this.getType().equals(other.getType());
    }

    private static class DynamicFieldType extends FieldType {

        public DynamicFieldType() {

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.oodt.xmlps.mapping.FieldType#getType()
         */
        @Override
        protected String getType() {
            return TYPE_DYNAMIC;
        }

    }

    private static class ConstantFieldType extends FieldType {

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.oodt.xmlps.mapping.FieldType#getType()
         */
        @Override
        protected String getType() {
            // TODO Auto-generated method stub
            return TYPE_CONSTANT;
        }

    }

}
