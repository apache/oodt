//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pge.writers.metlist;

//JDK imports
import static gov.nasa.jpl.oodt.cas.pge.config.PgeConfigMetKeys.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;
import gov.nasa.jpl.oodt.cas.commons.xml.XMLUtils;
import gov.nasa.jpl.oodt.cas.pge.writers.PcsMetFileWriter;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A {@link PcsMetFileWriter} that generates PCS met files based on a MetList
 * XML document
 * </p>.
 */
public class MetadataListPcsMetFileWriter extends PcsMetFileWriter {

    @Override
    protected Metadata getSciPgeSpecificMetadata(File sciPgeCreatedDataFile,
            Metadata inputMetadata, Object... customArgs) throws Exception {
        Metadata metadata = new Metadata();
        for (Object arg : customArgs) {
            Element root = XMLUtils.getDocumentRoot(
                    new FileInputStream(new File((String) arg)))
                    .getDocumentElement();
            NodeList metadataNodeList = root.getElementsByTagName(METADATA_TAG);
            for (int i = 0; i < metadataNodeList.getLength(); i++) {
                Element metadataElement = (Element) metadataNodeList.item(i);
                String key = metadataElement.getAttribute(KEY_ATTR);
                if (key.equals(""))
                	key = PathUtils.doDynamicReplacement(metadataElement.getAttribute(KEY_GEN_ATTR), inputMetadata);
                String val = metadataElement.getAttribute(VAL_ATTR);
            	if (val.equals("")) 
            		val = metadataElement.getTextContent();
                if (val != null && !val.equals("")) {
                    if (!metadataElement.getAttribute(ENV_REPLACE_ATTR).toLowerCase().equals("false"))
                        val = PathUtils.doDynamicReplacement(val, inputMetadata);
                    String[] vals = null;
                    if (metadataElement.getAttribute(SPLIT_ATTR).toLowerCase().equals("false")) {
                        vals = new String[] { val };
                    } else {
                        String delimiter = metadataElement.getAttribute("delimiter");
                        if (delimiter == null || delimiter.equals(""))
                            delimiter = ",";
                        vals = (val + delimiter).split(delimiter);
                    }
                    metadata.replaceMetadata(key, Arrays.asList(vals));
                    inputMetadata.replaceMetadata(key, Arrays.asList(vals));
                } else if (inputMetadata.getMetadata(key) != null
                        && !inputMetadata.getMetadata(key).equals("")) {
                    metadata.replaceMetadata(key, inputMetadata
                            .getAllMetadata(key));
                }
            }
        }
        return metadata;
    }

}
