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
package org.apache.oodt.cas.workflow.metadata;

//JDK imports
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;

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
public class ControlMetadata {

    private Metadata dynamicMetadata, localMetadata;
    private Metadata staticMetadata;
    
    public static final int DYN = 1;

    public static final int STATIC = 2;

    public static final int LOCAL = 3;

    private static int[] defaultMetadataQueryOrder = new int[] {
    	ControlMetadata.DYN, ControlMetadata.LOCAL, ControlMetadata.STATIC };

    private static int[] defaultMetadataCombineOrder = new int[] {
    	ControlMetadata.STATIC, ControlMetadata.LOCAL, ControlMetadata.DYN };

    private HashMap<String, String> keyLinkMap;
    
    private Set<String> workflowMetKeys;
    
    public static void main(String[] args) {
    	ControlMetadata m = new ControlMetadata();
		m.replaceLocalMetadata("test/IASI", "iasi");
		m.replaceLocalMetadata("test/MHS", "mhs");
		m.replaceLocalMetadata("test/AMSUA", "amsua");
		m.linkKey("test", "test/MHS");
		Metadata m1 = m.asMetadata();
		System.out.println(m1.getAllKeys());
    }
    
    
    public ControlMetadata() {
    	this(null, null);
    }
    
    public ControlMetadata(ControlMetadata ctrlMetadata) {
    	this(null, null);
    	this.replaceControlMetadata(ctrlMetadata);
    }
    
    public ControlMetadata(Metadata staticMetadata, Metadata dynamicMetadata) {
        this.keyLinkMap = new HashMap<String, String>();
        this.workflowMetKeys = new HashSet<String>();
		this.staticMetadata = staticMetadata != null ? new Metadata(staticMetadata) : new Metadata();
        this.dynamicMetadata = dynamicMetadata != null ? new Metadata(dynamicMetadata) : new Metadata();
        this.localMetadata = new Metadata();
	}
    
    public void replaceControlMetadata(ControlMetadata ctrlMetadata) {
    	this.replaceControlMetadata(ctrlMetadata, null);
    }
    
    //Namespaces custom metadata, keyLinkMap keys (if key-ref exists in custom metadata or keyLinkMap) and, workflowMetKeys (if they exist in custom metadata or keyLinkMap)
    public void replaceControlMetadata(ControlMetadata ctrlMetadata, String group) {
    	this.staticMetadata.replaceMetadata(group, ctrlMetadata.staticMetadata);
    	this.dynamicMetadata.replaceMetadata(group, ctrlMetadata.dynamicMetadata);
    	this.localMetadata.replaceMetadata(group, ctrlMetadata.localMetadata);
    	for (String key : ctrlMetadata.keyLinkMap.keySet()) {
    		String keyLink = ctrlMetadata.keyLinkMap.get(key);
    		if (group != null && (ctrlMetadata.localMetadata.containsKey(keyLink) || ctrlMetadata.keyLinkMap.containsKey(keyLink)))
    			keyLink = group + "/" + keyLink;
    		this.linkKey(group != null ? group + "/" + key : key, keyLink);
    	}
    	for (String key : ctrlMetadata.workflowMetKeys)
    		this.setAsWorkflowMetadataKey((group != null && (ctrlMetadata.localMetadata.containsKey(key) || ctrlMetadata.keyLinkMap.containsKey(key))) ? group + "/" + key : key);
    }
    
    public void setAsWorkflowMetadataKey(String... keys) {
        this.workflowMetKeys.addAll(Arrays.asList(keys));
    }
    
    /**
     * If no args, all keys are committed, otherwise only keys given are committed
     */
    public void commitWorkflowMetadataKeys(String... keys) {
    	Set<String> commitKeys = keys.length > 0 ? new HashSet<String>(Arrays.asList(keys)) : new HashSet<String>(this.workflowMetKeys);
        for (String key : commitKeys) {
        	this.dynamicMetadata.replaceMetadata(key, this.localMetadata.getAllMetadata(this.resolveKey(key)));
        	this.localMetadata.removeMetadata(key);
        	this.workflowMetKeys.remove(key);
        }
    }
    
    public void linkKey(String keyName, String linkToKeyName) {
    	this.localMetadata.removeMetadata(keyName);
        this.keyLinkMap.put(keyName, linkToKeyName);
    }
    
    public void unlinkKey(String key) {
    	this.keyLinkMap.remove(key);
    }
    
    public boolean isLink(String key) {
    	return this.keyLinkMap.containsKey(key);
    }
    
    public String getReferenceKey(String key) {
    	return this.resolveKey(key);
    }
    
    public List<String> getReferenceKeyPath(String key) {
    	Vector<String> keyPath = new Vector<String>();
        String useKeyName = key;
        while(this.keyLinkMap.containsKey(useKeyName))
        	keyPath.add(useKeyName = this.keyLinkMap.get(useKeyName));
        return keyPath;
    }

    public void replaceLocalMetadata(String name, String value) {
    	String resolveKey = this.resolveKey(name);
    	if (this.keyLinkMap.containsKey(name) && this.dynamicMetadata.containsKey(resolveKey))
    		this.dynamicMetadata.replaceMetadata(resolveKey, value);
    	else
    		this.localMetadata.replaceMetadata(resolveKey, value);
    }

    public void replaceLocalMetadata(Metadata metadata) {
    	for (String key : metadata.getAllKeys())
    		this.replaceLocalMetadata(key, metadata.getAllMetadata(key));
    }
    
    public void replaceLocalMetadata(String name, List<String> values) {
    	String resolveKey = this.resolveKey(name);
    	if (this.keyLinkMap.containsKey(name) && this.dynamicMetadata.containsKey(resolveKey))
    		this.dynamicMetadata.replaceMetadata(resolveKey, values);
    	else
    		this.localMetadata.replaceMetadata(resolveKey, values);
    }

    // add in order which you want metadata added (will return a copy)
    public Metadata asMetadata(int... types) {
        if (types.length < 1)
            types = defaultMetadataCombineOrder;
        Metadata combinedMetadata = new Metadata();
        for (int type : types) {
            switch (type) {
            case DYN:
                combinedMetadata.replaceMetadata(this.dynamicMetadata);
                break;
            case STATIC:
                combinedMetadata.replaceMetadata(this.staticMetadata);
                break;
            case LOCAL:
                combinedMetadata.replaceMetadata(this.localMetadata);
                for (String key : this.keyLinkMap.keySet())
                    combinedMetadata.replaceMetadata(key, this.getAllMetadata(key));
                break;
            }
        }
        return combinedMetadata;
    }

    public List<String> getAllMetadata(String name, int... types) {
        if (types.length < 1)
            types = defaultMetadataQueryOrder;
        String useKeyName = this.resolveKey(name);
        for (int type : types) {
            switch (type) {
            case DYN:
                if (this.dynamicMetadata.containsKey(useKeyName))
                    return this.dynamicMetadata.getAllMetadata(useKeyName);
                break;
            case STATIC:
                if (this.staticMetadata.containsKey(useKeyName))
                    return this.staticMetadata.getAllMetadata(useKeyName);
                break;
            case LOCAL:
                if (this.localMetadata.containsKey(useKeyName))
                    return this.localMetadata.getAllMetadata(useKeyName);
                break;
            }
        }
        return new Vector<String>();
    }

    public String getMetadata(String name, int... types) {
        List<String> values = this.getAllMetadata(name, types);
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
