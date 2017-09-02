//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package org.apache.oodt.cas.workflow.policy;

//JDK imports
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Provides a utility method to read in a {@link WorkflowConfiguration} from an
 * XML {@link Node}, preserving its envReplace information.
 * </p>.
 */
public class EnvVarSavingConfigReader {

    private EnvVarSavingConfigReader() throws InstantiationException {
        throw new InstantiationException("Don't construct readers!");
    }

    public static EnvSavingConfiguration getConfiguration(Node node) {
        Element configNode = (Element) node;

        NodeList configProperties = configNode.getElementsByTagName("property");

        EnvSavingConfiguration config = null;

        if (configProperties == null) {
            return null;
        }

        config = new EnvSavingConfiguration();
        for (int i = 0; i < configProperties.getLength(); i++) {
            Element propElem = (Element) configProperties.item(i);
            String value = propElem.getAttribute("value");
            boolean doReplace = Boolean.valueOf(
                    propElem.getAttribute("envReplace")).booleanValue();

            config.addConfigProperty(propElem.getAttribute("name"), value,
                    doReplace);
        }

        return config;
    }

}
