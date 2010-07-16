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
import java.io.IOException;
import java.util.LinkedList;

//Spring imports
import org.springframework.beans.BeansException;
import org.springframework.context.support.FileSystemXmlApplicationContext;

//Junit imports
import junit.framework.TestCase;

/**
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Test suite for the {@link PreCondEvalUtils} class
 * </p>.
 */
public class TestPreCondEvalUtils extends TestCase {

    LinkedList<String> preconditions;
    
    private PreCondEvalUtils evalUtils;
    

    public TestPreCondEvalUtils() throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, BeansException, IOException {
        this.preconditions = new LinkedList<String>();
        this.preconditions.add("CheckThatDataFileSizeIsGreaterThanZero");
        this.preconditions.add("CheckThatDataFileExists");
        this.preconditions.add("CheckDataFileMimeType");
        File preCondFile = new File(getClass().getResource("met_extr_preconditions.xml").getFile());
        assertNotNull(preCondFile);
        this.evalUtils = new PreCondEvalUtils(new FileSystemXmlApplicationContext(preCondFile.toURL().toExternalForm()));
    }

    public void testEval(){
        File prodFile = null;
        try{
            prodFile = new File(getClass().getResource("met_extr_preconditions.xml").getFile());
            assertTrue(this.evalUtils.eval(this.preconditions, prodFile));
        }
        catch(Throwable e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
