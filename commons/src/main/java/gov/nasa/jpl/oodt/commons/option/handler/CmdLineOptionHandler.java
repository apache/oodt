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


package gov.nasa.jpl.oodt.cas.commons.option.handler;

//JDK imports
import java.util.List;

//OODT imports
import gov.nasa.jpl.oodt.cas.commons.option.CmdLineOption;

//Spring imports
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public abstract class CmdLineOptionHandler {

    public String getOptionUsage(CmdLineOption option) {
        String argName = option.hasArgs() ? " <" + option.getOptionArgName()
                + ">" : "";
        String outputString = " -"
                + StringUtils.rightPad(option.getShortOption() + ",", 7) + "--"
                + StringUtils.rightPad((option.getLongOption() + argName), 49)
                + option.getDescription();
        if (!option.isRequired())
            outputString = "[" + outputString.substring(1) + "]";
        else if (option.getRequiredOptions().size() > 0)
            outputString = "{" + outputString.substring(1) + "}";
        return outputString;
    }

    public abstract void handleOption(CmdLineOption option, List<String> values);

    public abstract String getCustomOptionUsage(CmdLineOption option);

}
