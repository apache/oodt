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

package org.apache.oodt.cas.filemgr.metadata.extractors.examples;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

//OODT imports
import org.apache.oodt.cas.filemgr.metadata.extractors.AbstractFilemgrMetExtractor;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;

//TIKA imports
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.*;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class TikaAutoDetectExtractor extends AbstractFilemgrMetExtractor  {

    public void doConfigure() {
    }

    public Metadata doExtract(Product product, Metadata met) throws MetExtractionException {
        Metadata outMetadata = new Metadata();

        merge(met, outMetadata);
        Metadata tikaMetadata = getMetadataFromTika(product);
        merge(tikaMetadata, outMetadata);

        return outMetadata;
    }

    private Metadata getMetadataFromTika(Product product) throws MetExtractionException {
        try {
            File file = getProductFile(product);
            FileInputStream inputStream = new FileInputStream(file);
            org.apache.tika.metadata.Metadata tikaMetadata = new org.apache.tika.metadata.Metadata();
            Parser parser = new AutoDetectParser();
            parser.parse(inputStream, new DefaultHandler(), tikaMetadata, new ParseContext());
            return transform(tikaMetadata);

        } catch (FileNotFoundException e) {
            throw new MetExtractionException(
                    "Unable to find file: Reason: " + e.getMessage());
        } catch (TikaException e) {
            throw new MetExtractionException(
                    "Unable to parse the document: Reason: " + e.getMessage());
        } catch (SAXException e) {
            throw new MetExtractionException(
                    " Unable to process the SAX events : Reason: " + e.getMessage());
        } catch (IOException e) {
            throw new MetExtractionException(
                    "Unable to read the document stream: Reason: " + e.getMessage());
        }
    }

    private Metadata transform(org.apache.tika.metadata.Metadata tikaMetadata){
        Metadata metadata = new Metadata();

        String[] names = tikaMetadata.names();
        for (String name : names){
            metadata.addMetadata(name, Arrays.asList(tikaMetadata.getValues(name)));
        }

        return metadata;
    }
}
