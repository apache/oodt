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
		StringBuilder rStr=new StringBuilder();
		
		   rStr.append("<resAttributes>\n");
		   
		   rStr.append("\t<Identifier>").append(resAttr.getIdentifier()).append("</Identifier>\n");
		   rStr.append("\t<Title>").append(resAttr.getTitle()).append("</Title>\n");
		   rStr.append("\t<resClass>").append(resAttr.getResClass()).append("</resClass>\n");
		   rStr.append("\t<resAggregation>").append(resAttr.getResAggregation()).append("</resAggregation>\n");

	  for (String theRight : resAttr.getRights()) {
		rStr.append("\t<Right>").append(theRight).append("</Right>\n");
	  }

	  for (String theSource : resAttr.getSources()) {
		rStr.append("\t<Source>").append(theSource).append("</Source>\n");
	  }

	  for (String theSubject : resAttr.getSubjects()) {
		rStr.append("\t<Subject>").append(theSubject).append("</Subject>\n");
	  }

	  for (String theFormat : resAttr.getFormats()) {
		rStr.append("\t<Format>").append(theFormat).append("</Format>\n");
	  }

	  for (String theCreator : resAttr.getCreators()) {
		rStr.append("\t<Creator>").append(theCreator).append("</Creator>\n");
	  }

	  for (String thePublisher : resAttr.getPublishers()) {
		rStr.append("\t<Publisher>").append(thePublisher).append("</Publisher>\n");
	  }

	  for (String theType : resAttr.getTypes()) {
		rStr.append("\t<Type>").append(theType).append("</Type>\n");
	  }

	  for (String theContext : resAttr.getResContexts()) {
		rStr.append("\t<resContext>").append(theContext).append("</resContext>\n");
	  }

	  for (String theLocation : resAttr.getResLocations()) {
		rStr.append("\t<resLocation>").append(theLocation).append("</resLocation>\n");
	  }

	  for (String theContributor : resAttr.getContributors()) {
		rStr.append("\t<Contributor>").append(theContributor).append("</Contributor>\n");
	  }

	  for (String theCoverage : resAttr.getCoverages()) {
		rStr.append("\t<Coverage>").append(theCoverage).append("</Coverage>\n");
	  }

	  for (String theLang : resAttr.getLanguages()) {
		rStr.append("\t<Language>").append(theLang).append("</Language>\n");
	  }

	  for (String theRelation : resAttr.getRelations()) {
		rStr.append("\t<Relation>").append(theRelation).append("</Relation>\n");
	  }
		   
		   rStr.append("</resAttributes>\n");
		   
		return rStr.toString();
	}
}
