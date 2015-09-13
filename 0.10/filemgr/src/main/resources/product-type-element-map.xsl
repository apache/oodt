<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!-- 
	Description: To use this stylesheet in your product-type-element map, add the following
	line below the <?xml... ?> declaration in the map file:
	
	<?xml-stylesheet type="text/xsl" href="product-type-element-map.xsl"?>
-->
<!DOCTYPE xsl:stylesheet  [
	<!ENTITY nbsp   "&#160;">
	<!ENTITY copy   "&#169;">
	<!ENTITY reg    "&#174;">
	<!ENTITY trade  "&#8482;">
	<!ENTITY mdash  "&#8212;">
	<!ENTITY ldquo  "&#8220;">
	<!ENTITY rdquo  "&#8221;"> 
	<!ENTITY pound  "&#163;">
	<!ENTITY yen    "&#165;">
	<!ENTITY euro   "&#8364;">
]>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:cas="http://oodt.jpl.nasa.gov/1.0/cas">
	<xsl:output method="html" encoding="ISO-8859-1"
		doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" />
	<xsl:template match="/cas:producttypemap">

		<html xmlns="http://www.w3.org/1999/xhtml">
			<head>
				<meta http-equiv="Content-Type"
					content="text/html; charset=ISO-8859-1" />
				<title>CAS Objects</title>
			</head>

			<xsl:variable name="elementList"
				select="document('elements.xml')" />

			<body>
				<table border="0" style="font-family:Sans-Serif;">
					<tr style="font-weight:bold">
						<td>Object</td>
						<td>Data Elements</td>
					</tr>
					<xsl:for-each select="type">
						<tr valign="top">
							<td height="40"
								style="background-color:#dfd389;">
								<xsl:value-of
									select="substring(@id, 10)" />
							</td>
							<td rowspan="3">
								<table
									style="background-color:#889989;text-decoration:italic;">
									<xsl:choose>
										<xsl:when test="element">
											<xsl:for-each
												select="element">
												<xsl:variable
													name="elemId" select="@id" />
												<tr>
													<td>
														<xsl:value-of
															select="substring(@id, 10)" />
													</td>
													<td
														style="font-size:11px;">
														<xsl:value-of
															select="$elementList/cas:elements/element[@id=$elemId]" />
													</td>
												</tr>
											</xsl:for-each>
										</xsl:when>
										<xsl:otherwise>
											<tr>
												<td colspan="2">
													No declared
													elements: All
													elements inherited
												</td>
											</tr>
										</xsl:otherwise>
									</xsl:choose>
								</table>
							</td>
						</tr>
						<tr valign="top" height="40">
							<td align="left"
								style="font-style:italic;">
								Parent:
								<xsl:value-of
									select="substring(@parent, 10)" />
							</td>
						</tr>
						<tr valign="top">
							<td>
								&nbsp;
							</td>
						</tr>
					</xsl:for-each>
				</table>

			</body>
		</html>

	</xsl:template>
</xsl:stylesheet>
