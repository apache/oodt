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
import java.util.logging.Logger;
import java.util.logging.Level;

//OODT static imports
import static org.apache.oodt.cas.metadata.util.PathUtils.doDynamicReplacement;

//OODT imports
import org.apache.oodt.cas.metadata.exceptions.PreconditionComparatorException;
import org.apache.oodt.commons.exec.ExecUtils;


/**
 * 
 * @author vmallder
 * @version $Revision$
 * 
 * <p>
 * The pre-condition comparator to use when you have an external script or 
 * application using {@link ExecUtils} that will perform some processing and 
 * return the result to be checked against the compareItem. 
 * </p>.
 */
public class ExternPreconditionComparator extends PreConditionComparator<Long> {

   private String executeCommand; 
   
   protected static final Logger LOG = Logger
      .getLogger( ExternPreconditionComparator.class.getName()); 
   
    
   public void setExecuteCommand( String cmd )  {
      this.executeCommand = cmd;
   }
	
   public String getExecuteCommand() {
      return this.executeCommand;
   }
	
    @Override
    protected int performCheck(File product, Long compareItem)
        throws PreconditionComparatorException {
			
        String envReplacedExecuteCommand = "";

        try {
            envReplacedExecuteCommand = doDynamicReplacement( executeCommand ) +
                " " + product.getName();

        } catch (Exception e ) {
            LOG.log(Level.WARNING,
                "Exception running extern comparator calling doDynamicReplacement with : command ["
                + executeCommand
                + "]: Message: " + e.getMessage());
                return 1; 
        }
		
        // Determine working directory
        String workingDirPath = product.getParentFile().getAbsolutePath();
        File workingDir = new File(workingDirPath);
        int status = -1;

        try {
            status = ExecUtils.callProgram(envReplacedExecuteCommand, workingDir);
         
        } catch (IOException e) {
            LOG.log(Level.WARNING,
                "IOException running extern comparator: commandLine: ["
                    + envReplacedExecuteCommand
                    + "]: Message: " + e.getMessage());
            return 1; 
        }
		
        return new Long(status).compareTo(compareItem);
    }
}

