<%@ page
	import="java.util.List"
	import="java.util.ArrayList"
	import="java.util.Iterator"
	import="java.util.Map"
	import="gov.nasa.jpl.edrn.ecas.curation.policymgr.CurationPolicyManager"
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

