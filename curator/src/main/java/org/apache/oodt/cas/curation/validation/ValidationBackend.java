package org.apache.oodt.cas.curation.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.oodt.cas.curation.validation.ValidationException;
import org.apache.commons.lang.StringUtils;
import org.apache.oodt.cas.filemgr.repository.RepositoryManager;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Element;
import org.apache.oodt.cas.filemgr.util.GenericFileManagerObjectFactory;
import org.apache.oodt.cas.filemgr.validation.ValidationLayer;

/**
 * The validation logic supporting cas-curation services
 * 
 * @author starchmd
 *
 */
public class ValidationBackend {
    private static final Logger LOG = Logger.getLogger(ValidationBackend.class.getName());

    RepositoryManager rm = null;
    ValidationLayer vl = null;

    //Holds errors caused when loading
    private Exception error = null;

    private static final String REPO_DIRS_KEY="org.apache.oodt.cas.filemgr.repositorymgr.dirs";
    private static final String VALD_DIRS_KEY="org.apache.oodt.cas.filemgr.validation.dirs";
    /**
     * Constructor
     * @param fmProps - filemanager properties file path
     * @throws ValidationException - Exception thrown while loading
     */
    public ValidationBackend(String fmProps) throws ValidationException {
        try {

            //Set the needed props from fm properties
            Properties props = loadPropertiesFile(fmProps);
            System.setProperty(REPO_DIRS_KEY, props.getProperty(REPO_DIRS_KEY));
            System.setProperty(VALD_DIRS_KEY, props.getProperty(VALD_DIRS_KEY));
            
            this.vl = GenericFileManagerObjectFactory.getValidationLayerFromFactory(props.getProperty("filemgr.validationLayer.factory"));
            this.rm = GenericFileManagerObjectFactory.getRepositoryManagerServiceFromFactory(props.getProperty("filemgr.repository.factory"));
            LOG.log(Level.INFO,"Finished setting up Validation Layer and Resource Manager");
        } catch (Exception e) {
            String message = "Failed to load filemanager validation and repository information";
            LOG.log(Level.SEVERE, message, e);
            ValidationException ve = new ValidationException(message,e);
            error = ve;
            throw ve;
        }
    }
    /**
     * Function to return validation information back up the stack
     * @return filled validation information
     */
    public Map<String,List<Element>> getValidation() throws ValidationException {
        try {
            Map<String,List<Element>> vi = new HashMap<String,List<Element>>();
            List<ProductType> prods = this.rm.getProductTypes();
            List<String> join = new LinkedList<String>();
            for (ProductType pt : prods) {
                String name = pt.getName();
                List<Element> elems = vl.getElements(pt);
                Collections.sort(elems,new ElementOrderingComparator());
                vi.put(name,elems);
                join.add(name);
            }
            vl.getElementByName("ProductType").getAttachments().put("values",StringUtils.join(join, ","));
            String def = vl.getElementByName("ProductType").getAttachments().get("default");
            //Setup defaults
            if (def != null)
                vi.put("default",vl.getElements(this.rm.getProductTypeByName(def)));
            return vi;
        } catch(NullPointerException e) {
            String message = "Validation backend in erroneous state";
            LOG.log(Level.SEVERE, message, this.error);
            throw new ValidationException(message,this.error);     
        } catch(Exception e) {
            String message = "Failed to got validation information";
            throw new ValidationException(message, e);
        }
    }
    /**
     * Loads a properties file (filemanager properties)
     * @param file - file to load properties from
     * @return loaded properties object
     * @throws IOException
     */
    private Properties loadPropertiesFile(String file) throws IOException {
        LOG.log(Level.INFO,"Loading properties file: "+file);
        Properties props = new Properties();
        props.load(new FileInputStream(new File(file)));
        return props;
    }
    /**
     * Allows sorting of elements based on "ordering" attachment
     * 
     * @author starchmd
     */
    class ElementOrderingComparator implements Comparator<Element> {
        public static final String ORDERING = "ordering";
        @Override
        public int compare(Element elem1, Element elem2) {
            if (elem1.getAttachments().containsKey(ORDERING) && elem2.getAttachments().containsKey(ORDERING)) {
                return elem1.getAttachments().get(ORDERING).compareTo(elem2.getAttachments().get(ORDERING));
            } else if (elem2.getAttachments().containsKey(ORDERING)) {
                return 1;
            } else if (elem1.getAttachments().containsKey(ORDERING)) {
                return -1;
            }
            return 0;
        }
        
    }
}
