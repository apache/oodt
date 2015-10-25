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

/*
 * Created on Jun 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.apache.oodt.profile.gui.pstructs;

import org.apache.oodt.profile.ResourceAttributes;
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

	  for (String theRight : resAttr.getRights()) {
		rStr += "\t<Right>" + theRight + "</Right>\n";
	  }

	  for (String theSource : resAttr.getSources()) {
		rStr += "\t<Source>" + theSource + "</Source>\n";
	  }

	  for (String theSubject : resAttr.getSubjects()) {
		rStr += "\t<Subject>" + theSubject + "</Subject>\n";
	  }

	  for (String theFormat : resAttr.getFormats()) {
		rStr += "\t<Format>" + theFormat + "</Format>\n";
	  }

	  for (String theCreator : resAttr.getCreators()) {
		rStr += "\t<Creator>" + theCreator + "</Creator>\n";
	  }

	  for (String thePublisher : resAttr.getPublishers()) {
		rStr += "\t<Publisher>" + thePublisher + "</Publisher>\n";
	  }

	  for (String theType : resAttr.getTypes()) {
		rStr += "\t<Type>" + theType + "</Type>\n";
	  }

	  for (String theContext : resAttr.getResContexts()) {
		rStr += "\t<resContext>" + theContext + "</resContext>\n";
	  }

	  for (String theLocation : resAttr.getResLocations()) {
		rStr += "\t<resLocation>" + theLocation + "</resLocation>\n";
	  }

	  for (String theContributor : resAttr.getContributors()) {
		rStr += "\t<Contributor>" + theContributor + "</Contributor>\n";
	  }

	  for (String theCoverage : resAttr.getCoverages()) {
		rStr += "\t<Coverage>" + theCoverage + "</Coverage>\n";
	  }

	  for (String theLang : resAttr.getLanguages()) {
		rStr += "\t<Language>" + theLang + "</Language>\n";
	  }

	  for (String theRelation : resAttr.getRelations()) {
		rStr += "\t<Relation>" + theRelation + "</Relation>\n";
	  }
		   
		   rStr+="</resAttributes>\n";
		   
		return rStr;
	}
}
