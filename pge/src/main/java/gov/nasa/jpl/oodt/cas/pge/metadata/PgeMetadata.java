//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pge.metadata;

//JDK imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * A wrapper class to act as a facade interface to all the different
 * {@link Metadata} sources given to a PGE
 * </p>.
 */
public class PgeMetadata {

    private Metadata dynMetadata, staticMetadata, customMetadata;

    private WorkflowTaskConfiguration staticConfig;

    public static final int DYN = 1;

    public static final int STATIC = 2;

    public static final int CUSTOM = 3;

    private static int[] defaultMetadataQueryOrder = new int[] {
            PgeMetadata.STATIC, PgeMetadata.DYN, PgeMetadata.CUSTOM };

    private static int[] defaultMetadataCombineOrder = new int[] {
            PgeMetadata.CUSTOM, PgeMetadata.DYN, PgeMetadata.STATIC };

    private HashMap<String, String> keyLinkMap;
    
    private Set<String> workflowMetKeys;
    
    public PgeMetadata() throws Exception {
    	this(null, null);
    }
    
    public PgeMetadata(Metadata dynamicMetadata,
            WorkflowTaskConfiguration staticConfig) throws Exception {
        this.keyLinkMap = new HashMap<String, String>();
        this.workflowMetKeys = new HashSet<String>();
        this.customMetadata = new Metadata();
        this.dynMetadata = dynamicMetadata != null ? dynamicMetadata : new Metadata();
		this.staticMetadata = staticConfig != null ? this.convertToMetadata(staticConfig, dynamicMetadata) : new Metadata();
		this.staticConfig = staticConfig != null ? staticConfig : new WorkflowTaskConfiguration();
	}

	private Metadata convertToMetadata(WorkflowTaskConfiguration config,
			Metadata dynMetadata) throws Exception {
		Metadata metadata = new Metadata();
		for (Entry entry : config.getProperties().entrySet())
			metadata.replaceMetadata((String) entry.getKey(), (String) entry.getValue());
		return metadata;
	}

    public void addWorkflowMetadataKey(String key) {
        this.workflowMetKeys.add(key);
    }
    
    public void addPgeMetadata(PgeMetadata pgeMetadata) {
    	this.addPgeMetadata(pgeMetadata, null);
    }
    
    //Namespaces custom metadata, keyLinkMap keys (if key-ref exists in custom metadata or keyLinkMap) and, workflowMetKeys (if they exist in custom metadata or keyLinkMap)
    public void addPgeMetadata(PgeMetadata pgeMetadata, String namespace) {
    	this.dynMetadata.addMetadata(pgeMetadata.dynMetadata.getHashtable(), true);
    	this.staticMetadata.addMetadata(pgeMetadata.staticMetadata.getHashtable(), true);
    	this.staticConfig.getProperties().putAll(pgeMetadata.staticConfig.getProperties());
    	for (Object key : pgeMetadata.customMetadata.getHashtable().keySet())
    		this.addCustomMetadata(namespace != null ? namespace + ":" + (String) key : (String) key, pgeMetadata.customMetadata.getAllMetadata((String) key));
    	for (String key : pgeMetadata.keyLinkMap.keySet()) {
    		String value = pgeMetadata.keyLinkMap.get(key);
    		if (namespace != null && (pgeMetadata.customMetadata.containsKey(value) || pgeMetadata.keyLinkMap.containsKey(value)))
    			value = namespace + ":" + value;
    		this.linkKey(namespace != null ? namespace + ":" + key : key, value);
    	}
    	for (String key : pgeMetadata.workflowMetKeys)
    		this.addWorkflowMetadataKey((namespace != null && (pgeMetadata.customMetadata.containsKey(key) || pgeMetadata.keyLinkMap.containsKey(key))) ? namespace + ":" + key : key);
    }
    
    public void commitWorkflowMetadataKeys() {
        for (String key : this.workflowMetKeys) 
            this.dynMetadata.replaceMetadata(key, this.getMetadataValues(key));
        this.workflowMetKeys.clear();
    }
    
    public void linkKey(String keyName, String linkToKeyName) {
    	this.customMetadata.removeMetadata(keyName);
        this.keyLinkMap.put(keyName, linkToKeyName);
    }
    
    /**
     * Included for backwards compatibility with oco-pge
     * 
     * @return
     */
    @Deprecated
    public WorkflowTaskConfiguration getWorkflowTaskConfiguration() {
        return this.staticConfig;
    }

    /**
     * Included for backwards compatibility with oco-pge
     * 
     * @return
     */
    @Deprecated
    public Metadata getDynamicMetadata() {
        return this.dynMetadata;
    }

    public void addDynamicMetadata(String name, String value) {
        this.dynMetadata.replaceMetadata(name, value);
    }

    public void addDynamicMetadata(String name, List<String> values) {
        this.dynMetadata.replaceMetadata(name, values);
    }

    public void addCustomMetadata(String name, String value) {
    	this.keyLinkMap.remove(name);
        this.customMetadata.replaceMetadata(name, value);
    }

    public void addCustomMetadata(Metadata metadata) {
    	for (Object key : metadata.getHashtable().keySet())
    		this.addCustomMetadata((String) key, metadata.getAllMetadata((String) key));
    }
    
    public void addCustomMetadata(String name, List<String> values) {
    	this.keyLinkMap.remove(name);
        this.customMetadata.replaceMetadata(name, values);
    }

    // add in order which you want metadata added (will return a copy)
    public Metadata getMetadata(int... types) {
        if (types.length < 1)
            types = defaultMetadataCombineOrder;
        Metadata combinedMetadata = new Metadata();
        for (int type : types) {
            switch (type) {
            case DYN:
                combinedMetadata.addMetadata(this.dynMetadata.getHashtable(),
                        true);
                break;
            case STATIC:
                combinedMetadata.addMetadata(
                        this.staticMetadata.getHashtable(), true);
                break;
            case CUSTOM:
                combinedMetadata.addMetadata(
                        this.customMetadata.getHashtable(), true);
                for (Iterator<String> iter = this.keyLinkMap.keySet().iterator(); iter.hasNext(); ) {
                    String key = iter.next();
                    combinedMetadata.replaceMetadata(key, this.getMetadataValues(key));
                }
                break;
            }
        }
        return combinedMetadata;
    }

    public List<String> getMetadataValues(String name, int... types) {
        if (types.length < 1)
            types = defaultMetadataQueryOrder;
        String useKeyName = this.resolveKey(name);
        for (int type : types) {
            List<String> value = null;
            switch (type) {
            case DYN:
                if ((value = this.dynMetadata.getAllMetadata(useKeyName)) != null)
                    return value;
                break;
            case STATIC:
                if ((value = this.staticMetadata.getAllMetadata(useKeyName)) != null)
                    return value;
                break;
            case CUSTOM:
                if ((value = this.customMetadata.getAllMetadata(useKeyName)) != null)
                    return value;
                break;
            }
        }
        return new Vector<String>();
    }

    public String getMetadataValue(String name, int... types) {
        List<String> values = this.getMetadataValues(name, types);
        if (values.size() > 0)
            return values.get(0);
        else
            return null;
    }
    
    public String resolveKey(String keyName) {
        String useKeyName = keyName;
        while(this.keyLinkMap.containsKey(useKeyName))
            useKeyName = this.keyLinkMap.get(useKeyName);
        return useKeyName;
    }

}
