/*
 * Created on Jun 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package jpl.eda.profile.gui.pstructs;

import jpl.eda.profile.ResourceAttributes;
import java.util.Iterator;

/**
 * @author mattmann
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ResourceAttributesPrinter {
	
	private ResourceAttributes resAttr=null;
	
	/**
	 * <p>Constructs a new Resource Attributes printer from a {@link ResourceAttributes}
	 * object.</p>
	 */
	public ResourceAttributesPrinter(ResourceAttributes r) {
		super();
		// TODO Auto-generated constructor stub
		resAttr = r;
		
	}
	
	public void setResourceAttributes(ResourceAttributes r){resAttr=r;}
	public ResourceAttributes getResourceAttributes(){return resAttr;}
	
	public String toXMLString(){
		String rStr="";
		
		   rStr+="<resAttributes>\n";
		   
		   rStr+="\t<Identifier>"+resAttr.getIdentifier()+"</Identifier>\n";
		   rStr+="\t<Title>"+resAttr.getTitle()+"</Title>\n";
		   rStr+="\t<resClass>"+resAttr.getResClass()+"</resClass>\n";
		   rStr+="\t<resAggregation>"+resAttr.getResAggregation()+"</resAggregation>\n";

		   for(Iterator i = resAttr.getRights().iterator(); i.hasNext(); ){
		   	String theRight = (String)i.next();
		   	rStr+="\t<Right>"+theRight+"</Right>\n";
		   }
		   
		   for(Iterator i = resAttr.getSources().iterator(); i.hasNext(); ){
		   	String theSource = (String)i.next();
		   	rStr+="\t<Source>"+theSource+"</Source>\n";
		   }
		   
		   for(Iterator i = resAttr.getSubjects().iterator(); i.hasNext(); ){
		   	String theSubject = (String)i.next();
		   	rStr+="\t<Subject>"+theSubject+"</Subject>\n";
		   }
		   
		   for(Iterator i = resAttr.getFormats().iterator(); i.hasNext(); ){
		   	String theFormat = (String)i.next();
		   	rStr+="\t<Format>"+theFormat+"</Format>\n";
		   }
		   
		   for(Iterator i = resAttr.getCreators().iterator(); i.hasNext(); ){
		   	String theCreator = (String)i.next();
		   	rStr+="\t<Creator>"+theCreator+"</Creator>\n";
		   }
		   
		   for(Iterator i = resAttr.getPublishers().iterator(); i.hasNext(); ){
		   	String thePublisher = (String)i.next();
		   	rStr+="\t<Publisher>"+thePublisher+"</Publisher>\n";
		   }
		   
		   for(Iterator i = resAttr.getTypes().iterator(); i.hasNext(); ){
		   	String theType = (String)i.next();
		   	rStr+="\t<Type>"+theType+"</Type>\n";
		   }
		   
		   for(Iterator i = resAttr.getResContexts().iterator(); i.hasNext(); ){
		   	String theContext = (String)i.next();
		   	rStr+="\t<resContext>"+theContext+"</resContext>\n";
		   }
		   
		   for(Iterator i = resAttr.getResLocations().iterator(); i.hasNext(); ){
		   	String theLocation = (String)i.next();
		   	rStr+="\t<resLocation>"+theLocation+"</resLocation>\n";
		   }
		   
		   for(Iterator i = resAttr.getContributors().iterator(); i.hasNext(); ){
		   	String theContributor = (String)i.next();
		   	rStr+="\t<Contributor>"+theContributor+"</Contributor>\n";
		   }
		   
		   for(Iterator i = resAttr.getCoverages().iterator(); i.hasNext(); ){
		   	String theCoverage = (String)i.next();
		   	rStr+="\t<Coverage>"+theCoverage+"</Coverage>\n";
		   }
		   
		   for(Iterator i = resAttr.getLanguages().iterator(); i.hasNext(); ){
		   	String theLang = (String)i.next();
		   	rStr+="\t<Language>"+theLang+"</Language>\n";
		   }
		   
		   for(Iterator i = resAttr.getRelations().iterator(); i.hasNext(); ){
		   	String theRelation = (String)i.next();
		   	rStr+="\t<Relation>"+theRelation+"</Relation>\n";
		   }
		   
		   rStr+="</resAttributes>\n";
		   
		return rStr;
	}
}
