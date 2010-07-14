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


package org.apache.oodt.commons.option;

//JDK imports
import java.util.LinkedList;
import java.util.List;

//OODT imports
import org.apache.oodt.commons.option.handler.CmdLineOptionHandler;
import org.apache.oodt.commons.option.handler.StdCmdLineOptionHandler;
import org.apache.oodt.commons.option.required.RequiredOption;
import org.apache.oodt.commons.option.validator.CmdLineOptionValidator;
import org.apache.oodt.commons.spring.SpringSetIdInjectionType;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>Describe your class here</p>
 * 
 */
public class CmdLineOption implements Comparable<CmdLineOption>, SpringSetIdInjectionType {

    private String shortOption;

    private String longOption;

    private String description;

    private String optionArgName;

    private boolean required;
    
    private List<RequiredOption> requiredOptions;

    private boolean hasArgs;

    private boolean performAndQuit;
    
    private String id;

    private Class<?> type;

    private CmdLineOptionHandler handler;

    private List<CmdLineOptionValidator> validators;

    public CmdLineOption() {
        this.optionArgName = "arg";
        this.required = false;
        this.hasArgs = false;
        this.performAndQuit = false;
        this.requiredOptions = new LinkedList<RequiredOption>();
        this.handler = new StdCmdLineOptionHandler();
        this.validators = new LinkedList<CmdLineOptionValidator>();
    }

    public CmdLineOption(String shortOption, String longOption,
            String description, boolean hasArgs) {
        this();
        this.id = longOption;
        this.shortOption = shortOption;
        this.longOption = longOption;
        this.description = description;
        this.hasArgs = hasArgs;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setHandler(CmdLineOptionHandler handler) {
        this.handler = handler;
    }

    public CmdLineOptionHandler getHandler() {
        return this.handler;
    }

    public List<CmdLineOptionValidator> getValidators() {
        return this.validators;
    }

    public void setValidators(List<CmdLineOptionValidator> validators) {
        this.validators = validators;
    }
    
    public String getShortOption() {
        return shortOption;
    }

    public void setShortOption(String shortOption) {
        this.shortOption = shortOption;
    }

    public String getLongOption() {
        return longOption;
    }

    public void setLongOption(String longOption) {
        this.longOption = longOption;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean hasArgs() {
        return hasArgs;
    }

    public void setHasArgs(boolean hasArgs) {
        this.hasArgs = hasArgs;
    }

    public void setOptionArgName(String optionArgName) {
        this.optionArgName = optionArgName;
    }

    public String getOptionArgName() {
        return optionArgName;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    public List<RequiredOption> getRequiredOptions() {
        return this.requiredOptions;
    }

    public void setRequiredOptions(List<RequiredOption> requiredOptions) {
        this.requiredOptions = requiredOptions;
        if (this.requiredOptions.size() > 0)
            this.required = true;
    }

    public boolean isPerformAndQuit() {
		return performAndQuit;
	}

	public void setPerformAndQuit(boolean performAndQuit) {
		this.performAndQuit = performAndQuit;
	}

	public int compareTo(CmdLineOption cmdLineOption) {
        int thisScore = (this.required ? 2 : 0)
                - (this.requiredOptions.size() > 0 ? 1 : 0);
        int compareScore = (cmdLineOption.required ? 2 : 0)
                - (cmdLineOption.requiredOptions.size() > 0 ? 1 : 0);
        if ((!this.required && !cmdLineOption.required)
                || thisScore == compareScore) {
            if (this.shortOption != null && cmdLineOption.shortOption != null)
                return this.shortOption.compareTo(cmdLineOption.shortOption);
            else
                return this.longOption.compareTo(cmdLineOption.longOption);
        } else {
            return new Integer(compareScore).compareTo(thisScore);
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof CmdLineOption) {
            CmdLineOption compareObj = (CmdLineOption) obj;
            return compareObj.shortOption.equals(this.shortOption)
                    || compareObj.longOption.equals(this.longOption);
        } else
            return false;
    }
    
}
