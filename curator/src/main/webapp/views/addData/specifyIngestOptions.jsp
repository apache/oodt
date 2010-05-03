<%@	page 
	import="org.springframework.context.ApplicationContext"
	import="org.springframework.context.support.FileSystemXmlApplicationContext"
	import="gov.nasa.jpl.oodt.cas.crawl.action.CrawlerAction"
%>	
<%
// Obtain the explanations for the various ingest options
FileSystemXmlApplicationContext appContext = 
	new FileSystemXmlApplicationContext(CuratorConfMetKeys.CRAWLER_CONF_FILE);
String uniqueHint = ((CrawlerAction) appContext.getBean("Unique")).getDescription();
String deleteHint = ((CrawlerAction) appContext.getBean("DeleteDataFile")).getDescription();
%>


<%@page import="gov.nasa.jpl.oodt.cas.curation.servlet.CuratorConfMetKeys"%><div class="wizardContent">
	<h4>Pre-Ingestion Summary: </h4>
	<div>
			<table style="margin-bottom:15px;">
			  <tr><td style="width:190px;">Selected Dataset Collection: </td><td><%=session.getAttribute("dsCollection") %> <a class="actionlink" href="ingestData.jsp">change</a></td></tr>
			  <tr><td>Selected Dataset:</td><td><%=session.getAttribute("ds") %> <a class="actionlink" href="addData.jsp">change</a></td></tr>
			  <tr><td>Selected Metadata Extractor:</td><td><%=session.getAttribute("metextPrettyName") %></td></tr>
			</table>
	</div>
	<div>
	<h4>Specify Ingest Options</h4>
		<form action="beginIngestionTask" method="POST">
			<input type="hidden" name="dsCollection" value="<%=session.getAttribute("dsCollection") %>"/>
			<input type="hidden" name="ds"           value="<%=session.getAttribute("ds") %>"/>
			<input type="hidden" name="metext"       value="<%=session.getAttribute("metext") %>"/>
			<input type="hidden" name="metextConfigFilePath" value="<%=session.getAttribute("metextConfigFilePath") %>"/>
			<table cellspacing="0" cellpadding="0">
			  <tr>
			    <td colspan="2"><h5 style="margin-top:10px;">Pre-Ingest Actions</h5></td>
			  </tr>
			  <tr>
			  	<td><input type="checkbox" name="ingestAction" value="Unique">Unique</td>
			  	<td><span class="hint"><%=uniqueHint %></span></td>
			  </tr>
			  <tr>
			    <td colspan="2"><h5>Post-Ingest Actions</h5></td>
			  </tr>
			  <tr>
			    <td><input type="checkbox" name="ingestAction" value="DeleteDataFile">Delete Data Files</td>
			    <td><span class="hint"><%=deleteHint %></span></td>
			  </tr>
			</table>
		
			<h4>Specify Ingestion Root Path</h4>
			<p>Please specify the ingestion root path:</p>
			/data/ingest/<input type="text" name="ingestionRootPath" size="60" id="directory" style="border:solid 1px #888;padding:2px;" />
			<input type="submit" value="Begin Ingestion" style="padding:2px;"/>
		</form>
		<br/><br/>
	</div>
</div>


