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

package org.apache.oodt.cas.metadata.extractors;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.exceptions.MetExtractionException;
import org.apache.oodt.cas.metadata.exceptions.MetExtractorConfigReaderException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Assigns a ProductType based on a filename pattern, while simultaneously assigning values to metadata elements
 * embedded in the filename pattern.
 * <p/>
 * Suppose I have files in the staging area ready to be ingested. These files usually have information encoded into the
 * filename in order to distinguish the contents of one file from other files. For example book-1234567890.txt might be
 * the contents of a book with ISBN 1234567890. Or page-1234567890-12.txt might be the text on page 12 of book with ISBN
 * 1234567890.
 * <p/>
 * It would be useful to generate metadata from the information encoded in the filename (think: filename => metadata).
 * The {@link ProdTypePatternMetExtractor} allows this in a flexible manner using regular expressions. Let's take a look
 * at the config file for this met extractor.
 * <p/>
 * <pre>
 * product-type-patterns.xml
 *
 * {@code
 * <config>
 *   <!-- <element> MUST be defined before <product-type> so their patterns can be resolved -->
 *   <!-- name MUST be an element defined in elements.xml (also only upper and lower case alpha chars) -->
 *   <!-- regexp MUST be valid input to java.util.regex.Pattern.compile() -->
 *   <element name="ISBN" regexp="[0-9]{10}"/>
 *   <element name="Page" regexp="[0-9]*"/>
 *
 *   <!-- name MUST be a ProductType name defined in product-types.xml -->
 *   <!-- metadata elements inside brackets MUST be mapped to the ProductType,
 *        as defined in product-type-element-map.xml -->
 *   <product-type name="Book" template="book-[ISBN].txt"/>
 *   <product-type name="BookPage" template="page-[ISBN]-[Page].txt"/>
 * </config>
 * }
 * </pre>
 * <p/>
 * <p/>
 * This file defines a regular expression for the "ISBN" metadata element, in this case, a 10-digit number. Also, the
 * "Page" metadata element is defined as a sequence of 0 or more digits.
 * <p/>
 * Next, the file defines a filename pattern for the "Book" product type. The pattern is compiled into a regular
 * expression, substituting the previously defined regexes as capture groups. For example, "book-[ISBN].txt" compiles to
 * "book-([0-9]{10}).txt", and the ISBN met element is assigned to capture group 1. When the filename matches this
 * pattern, 2 metadata assignments occur: (1) the ISBN met element is set to the matched regex group, and (2) the
 * ProductType met element is set to "Book".
 * <p/>
 * Similarly, the second pattern sets ISBN, Page, and ProductType for files matching "page-([0-9]{10})-([0-9]*).txt".
 * <p/>
 * This achieves several things: <ol> <li>assigning met elements based on regular expressions</li> <li>assigning product
 * type based on easy-to-understand pattern with met elements clearly indicated</li> <li>reuse of met element regular
 * expressions</li> </ol>
 * <p/>
 * Differences from {@link FilenameTokenMetExtractor}:
 * <ol>
 *     <li>Allows dynamic length metadata (does not rely on offset and length of metadata)</li>
 *     <li>Assigns ProductType</li>
 * </ol>
 * <p/>
 * Differences from {@link org.apache.oodt.cas.crawl.AutoDetectProductCrawler}:
 * <ol>
 *     <li>Does not require definition of custom MIME type and MIME-type regex. Really, all you want is to assign a
 *     ProductType, rather than indirectly assigning a custom MIME type that maps to a Product Type.</li>
 * </ol>
 * <p/>
 * Differences from {@link org.apache.oodt.cas.filemgr.metadata.extractors.examples.FilenameRegexMetExtractor}:
 * <ol>
 *     <li>Assigns ProductType. FilenameRegexMetExtractor runs after ProductType is already determined.</li>
 *     <li>Runs on the client-side (crawler). FilenameRegexMetExtractor runs on the server-side (filemgr).</li>
 *     <li>Different patterns for different ProductTypes. FilenameRegexMetExtractor config applies the same pattern to
 *     all files.</li>
 * </ol>
 * <p/>
 * Prerequisites:
 * <ol>
 *     <li>{@code <element>} tag occurs before {@code <product-type>} tag</li>
 *     <li>{@code <element> @name} attribute <strong>MUST</strong> be defined in FileManager policy elements.xml</li>
 *     <li>{@code <element> @regexp} attribute <strong>MUST</strong> be valid input to
 *     {@link java.util.regex.Pattern#compile(String)}</li>
 *     <li>{@code <product-type> @name} attribute <strong>MUST</strong> be a ProductType name (not ID) defined in
 *     product-types.xml</li>
 *     <li>met elements used in {@code <product-type> @template} attribute <strong>MUST</strong> be
 *     mapped to the ProductType, as defined in product-type-element-map.xml</li>
 * </ol>
 * <p/>
 * <strong>Words of Caution</strong>
 * <ul>
 *     <li><strong>Does not support nested met elements.</strong></li>
 *     <li><strong>Each pattern should map to one product type.</strong> Watch out for similar patterns. Don't do this:
 * <pre>
 * {@code
 * <element name="Page" regexp="[0-9]*"/>
 * <element name="Chapter" regexp="[0-9]*"/>
 *
 * <product-type name="Page" template="data-[Page].txt"/>
 * <product-type name="Chapter" template="data-[Chapter].txt"/>
 * }</pre>
 * Instead, encode the product type information into the filename, for example:
 * <pre>
 * {@code
 * <element name="Page" regexp="[0-9]*"/>
 * <element name="Chapter" regexp="[0-9]*"/>
 *
 * <product-type name="Page" template="page-[Page].txt"/>
 * <product-type name="Chapter" template="chapter-[Chapter].txt"/>
 * }</pre>
 * </li>
 * </ul>
 *
 * @author rickdn (Ricky Nguyen)
 */
public class ProdTypePatternMetExtractor extends CmdLineMetExtractor {

    static class ConfigReader extends AbstractSAXConfigReader {
        private static final String ELEMENT_TAG = "element";
        private static final String ELEMENT_NAME_ATTR = "name";
        private static final String ELEMENT_REGEXP_ATTR = "regexp";
        private static final String PRODUCT_TYPE_TAG = "product-type";
        private static final String PRODUCT_TYPE_NAME_ATTR = "name";
        private static final String PRODUCT_TYPE_TEMPLATE_ATTR = "template";
        private static final Pattern MET_TOKEN = Pattern.compile("\\[([A-Za-z]*)\\]");

        /*
         * full file name reg exp => prod type
         */
        private final Map<Pattern, String> prodTypePatterns = new ConcurrentHashMap<Pattern, String>();

        /*
         * prod type => list of met elements in the file name
         */
        private final Map<String, List<String>> prodTypeElements = new ConcurrentHashMap<String, List<String>>();

        /*
         * met elements => element reg exp patterns
         */
        private final Map<String, Pattern> elementPatterns = new ConcurrentHashMap<String, Pattern>();


        Map<Pattern, String> getProdTypePatterns() {
            return prodTypePatterns;
        }

        Map<String, List<String>> getProdTypeElements() {
            return prodTypeElements;
        }

        void addProductType(String id, String template) {
            template = template.replaceAll("\\.", "\\\\.");
            Matcher m = MET_TOKEN.matcher(template);
            List<String> elemList = prodTypeElements.get(id);
            if (elemList == null) {
                elemList = new ArrayList<String>();
                prodTypeElements.put(id, elemList);
            }
            String newTemplate = template;
            while (m.find()) {
                String elem = m.group(1);
                String regex = elementPatterns.get(elem).toString();
                newTemplate = newTemplate.replaceAll("\\[" + elem + "\\]", "(" + regex + ")");
                elemList.add(elem);
            }
            prodTypePatterns.put(Pattern.compile(newTemplate), id);
        }

        void addElement(String name, String regexp) {
            elementPatterns.put(name, Pattern.compile(regexp));
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals(ELEMENT_TAG)) {
                String name = attributes.getValue(ELEMENT_NAME_ATTR);
                String regexp = attributes.getValue(ELEMENT_REGEXP_ATTR);
                addElement(name, regexp);
            } else if (qName.equals(PRODUCT_TYPE_TAG)) {
                String id = attributes.getValue(PRODUCT_TYPE_NAME_ATTR);
                String template = attributes.getValue(PRODUCT_TYPE_TEMPLATE_ATTR);
                addProductType(id, template);
            }
        }

        @Override
        public AbstractSAXConfigReader parseConfigFile(File configFile) throws MetExtractorConfigReaderException {
            // reset internal state whenever parsing a new config file
            prodTypePatterns.clear();
            prodTypeElements.clear();
            elementPatterns.clear();
            return super.parseConfigFile(configFile);
        }
    }

    private static final String PRODUCT_TYPE_MET_KEY = "ProductType";

    public ProdTypePatternMetExtractor() {
        super(new ConfigReader());
    }

    @Override
    protected Metadata extrMetadata(File file) throws MetExtractionException {
        Metadata met = new Metadata();
        ConfigReader mConfig = (ConfigReader) config;

        for (Pattern p : mConfig.getProdTypePatterns().keySet()) {
            Matcher m = p.matcher(file.getName());
            if (m.matches()) {
                String prodType = mConfig.getProdTypePatterns().get(p);
                met.addMetadata(PRODUCT_TYPE_MET_KEY, prodType);
                List<String> elemList = mConfig.getProdTypeElements().get(prodType);
                for (int i = 0; i < m.groupCount(); i++) {
                    met.addMetadata(elemList.get(i), m.group(i + 1));
                }
            }
        }

        return met;
    }

    public static void main(String[] args) throws Exception {
        processMain(args, new ProdTypePatternMetExtractor());
    }

}
