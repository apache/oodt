<?xml version="1.0" encoding="UTF-8"?>
<!-- 
    Copyright (c) 2008, California Institute of Technology.
    ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
-->
<xsl:stylesheet version="2.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:saxon="http://icl.com/saxon" 
    xmlns:atomic-int="java:java.util.concurrent.atomic.AtomicInteger" 
    xmlns:cas="http://oodt.jpl.nasa.gov/1.0/cas"
    exclude-result-prefixes="cas atomic-int saxon">    

    <xsl:output encoding="UTF-8"/>
    <xsl:output method="text"/>    
    
    <xsl:variable name="newline"><xsl:text>&#x0A;</xsl:text></xsl:variable>
    <xsl:variable name="tab"><xsl:text>&#x09;</xsl:text></xsl:variable>
    <xsl:variable name="instrument_name"><xsl:value-of select="/cas:metadata/keyval/val[../key='GranMap/L1FileTypeName']"/></xsl:variable>

    <xsl:template match="/">
        <xsl:text>NOMINALDAY</xsl:text><xsl:value-of select="$tab"/><xsl:value-of select="cas:metadata/keyval/val[../key='GranMap/NOMINALDAY']"/><xsl:value-of select="$newline"/>
        <xsl:text>OUTPUTPATH</xsl:text><xsl:value-of select="$tab"/><xsl:value-of select="cas:metadata/keyval/val[../key='GranMap/OUTPUTPATH']"/><xsl:value-of select="$newline"/>
        <xsl:text>OUTPUTFILE</xsl:text><xsl:value-of select="$tab"/><xsl:value-of select="cas:metadata/keyval/val[../key='GranMap/OUTPUTFILE']"/><xsl:value-of select="$newline"/><xsl:value-of select="$newline"/>
        
        <xsl:text>LOGFILE</xsl:text><xsl:value-of select="$tab"/><xsl:value-of select="cas:metadata/keyval/val[../key='GranMap/LOGFILE']"/><xsl:value-of select="$newline"/>
        <xsl:text>LOGLEVEL</xsl:text><xsl:value-of select="$tab"/><xsl:value-of select="cas:metadata/keyval/val[../key='GranMap/LOGLEVEL']"/><xsl:value-of select="$newline"/><xsl:value-of select="$newline"/>
        
        <xsl:text>DEBUG</xsl:text><xsl:value-of select="$tab"/><xsl:value-of select="cas:metadata/keyval/val[../key='GranMap/DEBUG']"/><xsl:value-of select="$newline"/><xsl:value-of select="$newline"/>
        
        <xsl:for-each select="cas:metadata/keyval">
            <xsl:if test="key='L1FormattedMetadata'">
                <xsl:value-of select="$instrument_name"/><xsl:text>_FILES</xsl:text><xsl:value-of select="$tab"/><xsl:value-of select="count(val)"/><xsl:value-of select="$newline"/>
                <xsl:variable name="atomicInt" select="atomic-int:new()"/>
                <xsl:for-each select="val">
                    <xsl:value-of select="$instrument_name"/><xsl:value-of select="atomic-int:incrementAndGet($atomicInt)"/><xsl:value-of select="$tab"/><xsl:value-of select="."/><xsl:value-of select="$newline"/>
                </xsl:for-each>
                <xsl:value-of select="$newline"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>
