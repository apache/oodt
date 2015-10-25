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

package org.apache.oodt.xmlps.mapping.funcs;

//JDK imports
import java.util.Properties;

//OODT imports
import org.apache.oodt.xmlps.structs.CDEValue;

/**
 * 
 * <p>
 * A function to convert {@link CDEValue}s from a CDE domain vocabulary into a
 * SDE domain vocabulary
 * </p>.
 */
public interface MappingFunc {

    /**
     * Translates the original CDE {@link CDEValue} into a local site's SDE
     * {@link CDEValue}.
     * 
     * @param orig
     *            The original {@link CDEValue} to translate.
     * @return The translated {@link CDEValue}.
     */
    CDEValue translate(CDEValue orig);

    /**
     * Translates the local site's SDE {@link CDEValue} back into its CDE
     * {@link CDEValue}.
     * 
     * @param orig
     *            The local site's SDE {@link CDEValue} to translate back into
     *            a CDE.
     * @return The translated {@link CDEValue}.
     */
    CDEValue inverseTranslate(CDEValue orig);

    /**
     * Configures the MappingFunc with the appropriate {@link Properties}.
     * 
     * @param props
     *            The {@link Properties} to use to configure the MappingFunc.
     */
    void configure(Properties props);

}
