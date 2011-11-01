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
package org.apache.oodt.cas.cli.help.printer;

//JDK imports
import java.util.Set;

//OODT imports
import org.apache.oodt.cas.cli.option.CmdLineOption;

/**
 * Help printer responsible for generating help message for given
 * {@link CmdLineOption}s.
 * 
 * @author bfoster (Brian Foster)
 */
public interface CmdLineOptionsHelpPrinter {

   /**
    * Generates help message for given {@link CmdLineOption}s
    * 
    * @param options
    *           {@link CmdLineOption}s for which help message will be generated
    * @return Help message for given {@link CmdLineOption}s.
    */
   public String printHelp(Set<CmdLineOption> options);

}
