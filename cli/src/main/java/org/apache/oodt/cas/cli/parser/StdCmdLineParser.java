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
package org.apache.oodt.cas.cli.parser;

//JDK imports
import java.util.List;

//OODT imports
import org.apache.oodt.cas.cli.util.ParsedArg;

//Google imports
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

/**
 * Standard Command-line parser which parser command line options of the form
 * --longOption or -shortOption followed by 0 or more values.
 * 
 * @author bfoster (Brian Foster)
 */
public class StdCmdLineParser implements CmdLineParser {

   public List<ParsedArg> parse(String[] args) {
      List<ParsedArg> parsedArgs = Lists.newArrayList();

      for (String arg : args) {
         if (isOption(arg)) {
            parsedArgs.add(new ParsedArg(getOptionName(arg),
                  ParsedArg.Type.OPTION));
         } else {
            parsedArgs.add(new ParsedArg(arg, ParsedArg.Type.VALUE));
         }
      }
      return parsedArgs;
   }

   @VisibleForTesting
   /* package */static boolean isOption(String arg) {
      return (arg.startsWith("-"));
   }

   @VisibleForTesting
   /* package */static String getOptionName(String arg) {
      if (arg.startsWith("--")) {
         return arg.substring(2);
      } else if (arg.startsWith("-")) {
         return arg.substring(1);
      } else {
         return null;
      }
   }
}
