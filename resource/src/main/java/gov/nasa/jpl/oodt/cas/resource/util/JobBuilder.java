//Copyright (c) 2006, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.resource.util;

//JDK imports
import java.io.File;
import java.io.FileInputStream;

import org.w3c.dom.Document;

//OODT imports
import gov.nasa.jpl.oodt.cas.commons.xml.XMLUtils;
import gov.nasa.jpl.oodt.cas.resource.structs.JobSpec;

/**
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Describe your class here
 * </p>.
 */
public final class JobBuilder {

    private JobBuilder() throws InstantiationException {
        throw new InstantiationException("Don't construct utility classes!");
    }

    public static JobSpec buildJobSpec(File jobFile) {
        return buildJobSpec(jobFile.getAbsolutePath());
    }

    public static JobSpec buildJobSpec(String jobFilePath) {
        Document doc = null;
        try {
            doc = XMLUtils.getDocumentRoot(new FileInputStream(new File(
                    jobFilePath)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return XmlStructFactory.getJobSpec(doc.getDocumentElement());
    }

}
