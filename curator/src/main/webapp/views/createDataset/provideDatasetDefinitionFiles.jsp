<%--
Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE.txt file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
-->
<%@ page
	import="java.util.List"
	import="java.util.ArrayList"
	import="java.util.Iterator"
	import="java.util.Map"
%>

<div>
	
	<h4>Create a new dataset...</h4>

	<div class="wizardContent">
		
		<p>A dataset consists of three XML files:
		<form action="uploadDatasetDefinitionFiles" method="POST" enctype="multipart/form-data">
		  <ul style="list-style:none;">

		    <li><span style="font-family:Courier New;">product-types.xml</span> - defines dataset names, their
		    archive locations, and the EDRN dataset metadata, metadata that does not change on a per-product
		    basis), such as Data Custodian, ProtocolId, etc.<br/>
		    <input type="file" value="" name="product_types_xml"><br/><br/>
		    </li>

		    <li><span style="font-family:Courier New;">elements.xml</span> - defines product metadata elements,
		    metadata that changes on a per product basis, e.g., Filename, File Location, etc.<br/>
		    <input type="file" value="" name="elements_xml"><br/><br/>
		    </li>

		    <li><span style="font-family:Courier New;">product-type-element-map.xml</span> - maps product metadata
		    elements to datasets.<br/>
		    <input type="file" value="" name="product_type_element_map_xml"><br/><br/>
		    </li>

		    <li>Specify a policyName to uniquely describe this policy<br/><input type="text" name="policyName"> Policy Name <br/>
		    </li> 
		   </ul>		
		</p>
			<input type="submit" name="submit" value="Upload Files" />
		</form>
		<br/><br/>
	</div>	
</div>

