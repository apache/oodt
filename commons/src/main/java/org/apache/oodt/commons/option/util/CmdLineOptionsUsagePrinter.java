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


package org.apache.oodt.commons.option.util;

//JDK imports
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

//OODT imports
import org.apache.oodt.commons.option.CmdLineOption;
import org.apache.oodt.commons.option.CmdLineOptionInstance;
import org.apache.oodt.commons.option.required.RequiredOption;

//Spring imports
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class CmdLineOptionsUsagePrinter {

    private CmdLineOptionsUsagePrinter() {
    }

    public static void printUsage(PrintStream ps, List<CmdLineOption> options) {
        ps.println(createPrintableHeader(options));
    }
    
    public static void printUsage(PrintStream ps, List<CmdLineOption> options, CmdLineOptionInstance optionHelp) {
    	ps.println("Option Help when option '--" + optionHelp.getOption().getLongOption() + "' is specified with values " + optionHelp.getValues());
    	ps.println(" - Required:");
    	Vector<CmdLineOption> required = new Vector<CmdLineOption>();
    	for (CmdLineOption option : options) {
    		for (RequiredOption reqOption : option.getRequiredOptions()) {
    			if (reqOption.getOptionLongName().equals(optionHelp.getOption().getLongOption()) && reqOption.getOptionValues().containsAll(optionHelp.getValues())) {
    				required.add(option);
    				ps.println("    -" + option.getShortOption() + " [--" + option.getLongOption() + "] " + (option.hasArgs() ? "<" + option.getOptionArgName() + ">" : ""));
    				break;
    			}
    		}
    	}
    	ps.println(" - Optional:");
    	for (CmdLineOption option : options) {
    		if (!required.contains(option) && option.getHandler().affectsOption(optionHelp))
				ps.println("    -" + option.getShortOption() + " [--" + option.getLongOption() + "] " + (option.hasArgs() ? "<" + option.getOptionArgName() + ">" : ""));
    	}
    } 


    private static String createPrintableHeader(List<CmdLineOption> options) {
        sortOptions(options);
        StringBuffer outputString = new StringBuffer(
                "-----------------------------------------------------------------------------------------------------------------\n");
        outputString.append("|" + StringUtils.rightPad(" Short", 7) + "|"
                + StringUtils.rightPad(" Long", 50) + "| Description\n");
        outputString
                .append("-----------------------------------------------------------------------------------------------------------------\n");
        for (CmdLineOption option : options) {
            outputString.append(option.getHandler().getOptionUsage(option)
                    + "\n"
                    + getFormattedString(option.getHandler()
                            .getCustomOptionUsage(option), 62, 139));
            if (option.getRequiredOptions().size() > 0)
                outputString.append(getFormattedString("RequiredOptions: "
                        + option.getRequiredOptions() + "\n", 62, 139));
            else
                outputString.append("\n");
        }
        outputString
                .append("-----------------------------------------------------------------------------------------------------------------");
        return outputString.toString();
    }

    private static void sortOptions(List<CmdLineOption> options) {
        Collections.sort(options);
    }

    private static String getFormattedString(String string, int startIndex,
            int endIndex) {
        StringBuffer outputString = new StringBuffer("");
        String[] splitStrings = StringUtils.split(string, " ");
        StringBuffer curLine = null;
        for (int i = 0; i < splitStrings.length; i++) {
            curLine = new StringBuffer("");
            curLine.append(splitStrings[i] + " ");

            for (; i + 1 < splitStrings.length
                    && curLine.length() + splitStrings[i + 1].length() <= (endIndex - startIndex); i++)
                curLine.append(splitStrings[i + 1] + " ");

            outputString.append(StringUtils.repeat(" ", startIndex)
                    + curLine.toString() + "\n");
        }
        return outputString.toString();
    }

}
