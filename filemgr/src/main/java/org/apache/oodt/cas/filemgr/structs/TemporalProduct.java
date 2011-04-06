package org.apache.oodt.cas.filemgr.structs;

import java.util.Date;
import java.util.List;

public class TemporalProduct extends Product {

    private Date productReceivedTime = null;

    public TemporalProduct() {
    	super();
    }
    
    public TemporalProduct(String name, ProductType pType, String structure, Date productReceivedTime,
            String transferStatus, List<Reference> refs) {
    	super(name, pType, structure, transferStatus, refs);
        this.productReceivedTime = productReceivedTime;
    }
    
    public Date getProductReceivedTime() {
		return productReceivedTime;
	}

	public void setProductReceivedTime(Date productReceivedTime) {
		this.productReceivedTime = productReceivedTime;
	}
	
}
