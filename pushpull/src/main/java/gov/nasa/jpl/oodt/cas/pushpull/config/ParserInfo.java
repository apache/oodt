//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.config;

//JDK imports
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.ConfigException;
import gov.nasa.jpl.oodt.cas.pushpull.filerestrictions.Parser;
import gov.nasa.jpl.oodt.cas.pushpull.retrievalmethod.RetrievalMethod;
import gov.nasa.jpl.oodt.cas.metadata.util.PathUtils;
import gov.nasa.jpl.oodt.cas.commons.xml.XMLUtils;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public class ParserInfo implements ConfigParserMetKeys{

    private HashMap<String, String> parserToRetrievalMethodMap;

    /* our log stream */
    private static final Logger LOG = Logger.getLogger(ParserInfo.class
            .getName());

    public ParserInfo() {
        parserToRetrievalMethodMap = new HashMap<String, String>();
    }

    public void loadParserInfo(File xmlFile) throws ConfigException {
        try {
            NodeList rmList = XMLUtils.getDocumentRoot(new FileInputStream(xmlFile))
                    .getElementsByTagName(RETRIEVAL_METHOD_TAG);
            for (int i = 0; i < rmList.getLength(); i++) {

                // get rm element
                Node rmNode = rmList.item(i);

                // get classpath for this rm
                String rmClasspath = PathUtils
                        .replaceEnvVariables(((Element) rmNode)
                                .getAttribute(CLASS_ATTR));

                // get all login info for this source
                NodeList parserList = ((Element) rmNode)
                        .getElementsByTagName(PARSER_TAG);
                for (int j = 0; j < parserList.getLength(); j++) {

                    // get a single login info
                    Node parserNode = parserList.item(j);
                    String parserClasspath = PathUtils
                            .replaceEnvVariables(((Element) parserNode)
                                    .getAttribute(CLASS_ATTR));

                    LOG.log(Level.INFO, "Assiging parser '" + parserClasspath
                            + "' with retrievalmethod '" + rmClasspath + "'");
                    this.parserToRetrievalMethodMap.put(parserClasspath,
                            rmClasspath);
                }
            }
        } catch (Exception e) {
            throw new ConfigException("Failed to load Parser info : "
                    + e.getMessage());
        }
    }

    public Class<RetrievalMethod> getRetrievalMethod(Parser parser)
            throws ClassNotFoundException {
        System.out.println(parser.getClass().getCanonicalName());
        return (Class<RetrievalMethod>) Class
                .forName(this.parserToRetrievalMethodMap.get(parser.getClass()
                        .getCanonicalName()));
    }

}
