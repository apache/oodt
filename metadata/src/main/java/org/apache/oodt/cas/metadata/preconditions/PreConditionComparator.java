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
import org.apache.oodt.commons.spring.SpringSetIdInjectionType;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.exceptions.PreconditionComparatorException;
import static org.apache.oodt.cas.metadata.preconditions.PreConditionOperatorMetKeys.*;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

/**
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * The abstract base class for evaluating {@link PreCondition} checks for
 * running a {@link org.apache.oodt.cas.metadata.MetExtractor}
 * </p>.
 */
public abstract class PreConditionComparator<CompareType> implements SpringSetIdInjectionType {

    private String fileExtension;

    private String type;

    private CompareType compareItem;

    private String description;

    private String id;
    
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public boolean passes(File product) {
        try {
            if (fileExtension != null) {
                product = new File(product.getAbsolutePath() + "."
                                   + this.fileExtension);
            }
            return eval(this.type, this.performCheck(product, this.compareItem));
        } catch (Exception e) {
            return false;
        }
    }

    protected abstract int performCheck(File product, CompareType compareItem)
            throws PreconditionComparatorException;

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    @Required
    public void setType(String type) {
        this.type = type;
    }

    @Required
    public void setCompareItem(CompareType compareItem) {
        this.compareItem = compareItem;
    }

    private static boolean eval(String opKey, int preconditionResult)
            throws MetExtractionException {
        opKey = opKey.toUpperCase();
        if (preconditionResult == 0) {
            return EQUAL_TO.equals(opKey);
        } else if (preconditionResult > 0) {
            return NOT_EQUAL_TO.equals(opKey) || GREATER_THAN.equals(opKey);
        } else if (preconditionResult < 0) {
            return NOT_EQUAL_TO.equals(opKey) || LESS_THAN.equals(opKey);
        } else {
            throw new MetExtractionException("evalType is not a valid type");
        }
    }

}
