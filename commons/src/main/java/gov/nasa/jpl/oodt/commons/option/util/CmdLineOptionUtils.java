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


package gov.nasa.jpl.oodt.cas.commons.option.util;

//JDK imports
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//OODT imports
import gov.nasa.jpl.oodt.cas.commons.option.CmdLineOption;
import gov.nasa.jpl.oodt.cas.commons.option.CmdLineOptionInstance;
import gov.nasa.jpl.oodt.cas.commons.option.parser.CmdLineOptionParser;
import gov.nasa.jpl.oodt.cas.commons.option.required.RequiredOption;
import gov.nasa.jpl.oodt.cas.commons.option.validator.CmdLineOptionValidator;

//Spring imports
import org.springframework.context.ApplicationContext;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 * <p>Describe your class here</p>.
 */
public class CmdLineOptionUtils {

    private CmdLineOptionUtils() {
    }

    public static CmdLineOption getOptionByName(String optionName,
            List<CmdLineOption> options) {
        for (CmdLineOption option : options)
            if (option.getLongOption().equals(optionName)
                    || option.getShortOption().equals(optionName))
                return option;
        return null;
    }

    public static CmdLineOptionInstance getOptionInstanceByName(
            String optionName, List<CmdLineOptionInstance> optionInsts) {
        for (CmdLineOptionInstance optionInst : optionInsts)
            if (optionInst.getOption().getLongOption().equals(optionName)
                    || optionInst.getOption().getShortOption().equals(
                            optionName))
                return optionInst;
        return null;
    }

    public static List<String> getOptionValues(String optionName,
            List<CmdLineOptionInstance> options) {
        for (CmdLineOptionInstance optionInst : options)
            if (optionInst.getOption().getLongOption().equals(optionName)
                    || optionInst.getOption().getShortOption().equals(
                            optionName))
                return optionInst.getValues();
        return null;
    }

    public static List<CmdLineOption> getOptions(ApplicationContext appContext) {
        List<CmdLineOption> options = new LinkedList<CmdLineOption>();
        Map optionsMap = appContext.getBeansOfType(CmdLineOption.class);
        for (Iterator<Entry> iter = optionsMap.entrySet().iterator(); iter
                .hasNext();)
            options.add((CmdLineOption) iter.next().getValue());
        return options;
    }

    public static List<CmdLineOptionInstance> loadValidateAndHandleInstances(
            ApplicationContext appContext, String[] args) throws IOException {

        // parse args
        CmdLineOptionParser parser = new CmdLineOptionParser();
        List<CmdLineOption> supportedOptions = CmdLineOptionUtils
                .getOptions(appContext);
        parser.setValidOptions(supportedOptions);
        List<CmdLineOptionInstance> optionInstances = parser.parse(args);

        // check that required args have been specified
        List<CmdLineOption> reqOptions = CmdLineOptionUtils
                .getRequiredOptions(supportedOptions);
        List<CmdLineOption> unsetReqOptions = CmdLineOptionUtils
                .getRequiredOptionsNotSet(optionInstances, reqOptions);
        for (CmdLineOption unsetReqOption : unsetReqOptions) {
            if (unsetReqOption.getRequiredOptions().size() > 0) {
                for (RequiredOption reqOption : unsetReqOption
                        .getRequiredOptions()) {
                    CmdLineOptionInstance optionInst = getOptionInstanceByName(
                            reqOption.getOptionLongName(), optionInstances);
                    if (optionInst != null
                            && ((reqOption.isRequireAllValues() && optionInst
                                    .getValues().containsAll(
                                            reqOption.getOptionValues())) || (!reqOption
                                    .isRequireAllValues() && !Collections
                                    .disjoint(optionInst.getValues(), reqOption
                                            .getOptionValues()))))
                        throw new IOException("Option "
                                + unsetReqOption.getId() + " is required");
                }
            } else
                throw new IOException("Option " + unsetReqOption.getId()
                        + " is required");
        }

        // validate options
        for (CmdLineOptionInstance optionInst : optionInstances) {
            for (CmdLineOptionValidator validator : optionInst.getOption()
                    .getValidators())
                if (!validator.validate(optionInst))
                    throw new IOException("Option "
                            + optionInst.getOption().getId()
                            + " failed validation");
        }

        // if all looks good . . . handle options
        for (CmdLineOptionInstance optionInst : optionInstances)
            optionInst.getOption().getHandler().handleOption(
                    optionInst.getOption(), optionInst.getValues());

        return optionInstances;
    }

    public static List<CmdLineOption> getRequiredOptionsNotSet(
            List<CmdLineOptionInstance> optionInsts,
            List<CmdLineOption> reqOptions) {
        List<CmdLineOption> nonSetRequiredOptions = new LinkedList<CmdLineOption>();
        TOP: for (CmdLineOption reqOption : reqOptions) {
            for (CmdLineOptionInstance optionInst : optionInsts) {
                if (reqOption.equals(optionInst.getOption()))
                    continue TOP;
            }
            nonSetRequiredOptions.add(reqOption);
        }
        return nonSetRequiredOptions;
    }

    public static List<CmdLineOption> getRequiredOptions(
            List<CmdLineOption> options) {
        List<CmdLineOption> requiredOptions = new LinkedList<CmdLineOption>();
        for (CmdLineOption option : options) {
            if (option.isRequired())
                requiredOptions.add(option);
        }
        return requiredOptions;
    }

}
