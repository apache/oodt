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

package org.apache.oodt.cas.filemgr.tools;

//JDK imports
import org.apache.oodt.cas.filemgr.structs.exceptions.ValidationLayerException;
import org.apache.oodt.cas.filemgr.util.XmlStructFactory;
import org.apache.oodt.cas.filemgr.validation.DataSourceValidationLayerFactory;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

//OODT imports

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * A Simple program to dump XML elements out of a DataSourceValidationLayer and
 * into an XML file.
 * </p>
 * 
 */
public final class DumpDbElementsToXml {

    private DumpDbElementsToXml() throws InstantiationException {
        throw new InstantiationException(
                "Don't construct private constructors!");
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException, ValidationLayerException {
        String propFile = null, outXmlFile = null;
        String usage = "DumpDbElementsToXml --propFile </path/to/propFile> --out </path/to/xml/file>\n";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--propFile")) {
                propFile = args[++i];
            } else if (args[i].equals("--out")) {
                outXmlFile = args[++i];
            }
        }

        if (propFile == null || outXmlFile == null) {
            System.err.println(usage);
            System.exit(1);
        }

        System.out.println("Loading properties file: [" + propFile + "]");
        System.getProperties().load(new FileInputStream(new File(propFile)));

        ValidationLayer dbLayer = new DataSourceValidationLayerFactory()
                .createValidationLayer();

        List elementList = dbLayer.getElements();
        XmlStructFactory.writeElementXmlDocument(elementList, outXmlFile);

    }

}
