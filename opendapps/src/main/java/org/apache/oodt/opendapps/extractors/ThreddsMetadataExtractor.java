/**
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

package org.apache.oodt.opendapps.extractors;

//JDK imports
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.opendapps.util.ProfileUtils;

//Spring imports
import org.springframework.util.StringUtils;

//THREDDS imports
import thredds.catalog.InvAccess;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDocumentation;
import thredds.catalog.InvProperty;
import thredds.catalog.ServiceType;
import thredds.catalog.ThreddsMetadata.Contributor;
import thredds.catalog.ThreddsMetadata.GeospatialCoverage;
import thredds.catalog.ThreddsMetadata.Range;
import thredds.catalog.ThreddsMetadata.Source;
import thredds.catalog.ThreddsMetadata.Vocab;
import ucar.nc2.units.DateType;
import ucar.unidata.geoloc.LatLonRect;

/**
 * Implementation of {@link MetadataExtractor} that extracts metadata from a
 * Thredds dataset.
 * 
 * @author Luca Cinquini
 * 
 */
public class ThreddsMetadataExtractor implements MetadataExtractor {

  private static Logger LOG = Logger.getLogger(ThreddsMetadataExtractor.class
      .getName());

  // constant missing for 4.2 version of NetCDF library
  public final static String SERVICE_TYPE_NCML = "NCML";
  
  /**
   * The source of metadata to be extracted.
   */
  private final InvDataset dataset;

  public ThreddsMetadataExtractor(final InvDataset dataset) {
    this.dataset = dataset;
  }

  public void extract(Metadata met) {

    LOG.log(Level.INFO, "Crawling catalog URL=" + dataset.getCatalogUrl()
        + " dataset ID=" + dataset.getID());

    ProfileUtils.addIfNotNull(met, "Authority", dataset.getAuthority());
    ProfileUtils.addIfNotNull(met, "CatalogUrl", dataset.getCatalogUrl());
    try {
    	ProfileUtils.addIfNotNull(met, "Host", (new URL(dataset.getCatalogUrl())).getHost() );
    } catch(MalformedURLException e) {
    	LOG.log(Level.WARNING, e.getMessage());
    }
    ProfileUtils.addIfNotNull(met, "DatasetFullName", dataset.getFullName());
    if (dataset.getContributors() != null) {
      for (Contributor contributor : dataset.getContributors()) {
        ProfileUtils.addIfNotNull(met, "Contributor", contributor.getName());
      }
    }

    if (dataset.getCreators() != null) {
      for (Source source : dataset.getCreators()) {
        ProfileUtils.addIfNotNull(met, "Creator", source.getName());
      }
    }

    if (dataset.getDataFormatType() != null) {
      ProfileUtils.addIfNotNull(met, "DataFormatType", dataset
          .getDataFormatType().toString());
    }

    if (dataset.getDataType() != null) {
      ProfileUtils.addIfNotNull(met, "DataType", dataset.getDataType()
          .toString());
    }

    if (dataset.getDates() != null) {
      for (DateType dateType : dataset.getDates()) {
        String dateString = null;
        try {
          dateString = ProfileUtils.toISO8601(dateType.getDate());
        } catch (Exception e) {
          LOG.log(Level.WARNING,
              "Error converting date: [" + dateType.getDate() + "]: Message: "
                  + e.getMessage());
        }
        ProfileUtils.addIfNotNull(met, "Dates", dateString);
      }
    }

    if (dataset.getDocumentation() != null) {
      for (InvDocumentation doc : dataset.getDocumentation()) {
      	// textual documentation
      	if (StringUtils.hasText(doc.getInlineContent())) {
      		if (StringUtils.hasText(doc.getType())) {
      			// use specific documentation type, when available
      			ProfileUtils.addIfNotNull(met, doc.getType(), doc.getInlineContent());
      		} else {
      			// otherwise use generic "Documentation" tag
      			ProfileUtils.addIfNotNull(met, "Documentation", doc.getInlineContent());
      		}
      	}
      	// hyperlinked documentation
      	if (StringUtils.hasText(doc.getXlinkHref())) {
      		String tuple = this.encodeXlinkTuple(doc.getXlinkHref(), doc.getXlinkTitle(), doc.getType());
      		ProfileUtils.addIfNotNull(met, "Xlink", tuple);
      	}
      	
      }
    }

    ProfileUtils.addIfNotNull(met, "FullName", dataset.getFullName());
    GeospatialCoverage geoCoverage = dataset.getGeospatialCoverage();
    
    if (geoCoverage != null) {
    	
      LatLonRect bbox = geoCoverage.getBoundingBox();
      if (bbox != null) {
        ProfileUtils.addIfNotNull(met, "SouthwestBC", bbox.getLowerLeftPoint()
            .toString());
        ProfileUtils.addIfNotNull(met, "NorthwestBC", bbox.getUpperLeftPoint()
            .toString());
        ProfileUtils.addIfNotNull(met, "NortheastBC", bbox.getUpperRightPoint()
            .toString());
        ProfileUtils.addIfNotNull(met, "SoutheastBC", bbox.getLowerRightPoint()
            .toString());
      }
      
      // try north south, east west
      if (geoCoverage.getNorthSouthRange() != null) {        	
        Range nsRange = geoCoverage.getNorthSouthRange();
        ProfileUtils.addIfNotNull(met, "NorthSouthRangeStart",
            String.valueOf(nsRange.getStart()));
        ProfileUtils.addIfNotNull(met, "NorthSouthRangeResolution",
            String.valueOf(nsRange.getResolution()));
        ProfileUtils.addIfNotNull(met, "NorthSouthRangeSize",
            String.valueOf(nsRange.getSize()));
        ProfileUtils.addIfNotNull(met, "NorthSouthRangeUnits",
            nsRange.getUnits());
        ProfileUtils.addIfNotNull(met, "NorthSouthRangeStop",
            String.valueOf(nsRange.getStart()+nsRange.getSize()));
      }

      if (geoCoverage.getEastWestRange() != null) {
        Range nsRange = geoCoverage.getEastWestRange();
        ProfileUtils.addIfNotNull(met, "EastWestRangeStart",
            String.valueOf(nsRange.getStart()));
        ProfileUtils.addIfNotNull(met, "EastWestRangeResolution",
            String.valueOf(nsRange.getResolution()));
        ProfileUtils.addIfNotNull(met, "EastWestRangeSize",
            String.valueOf(nsRange.getSize()));
        ProfileUtils.addIfNotNull(met, "EastWestRangeUnits",
            nsRange.getUnits());
        ProfileUtils.addIfNotNull(met, "EastWestRangeStop",
            String.valueOf(nsRange.getStart()+nsRange.getSize()));
      }      

      ProfileUtils.addIfNotNull(met, "GeospatialCoverageLatitudeResolution",
          String.valueOf(dataset.getGeospatialCoverage().getLatResolution()));
      ProfileUtils.addIfNotNull(met, "GeospatialCoverageLongitudeResolution",
          String.valueOf(dataset.getGeospatialCoverage().getLonResolution()));
      
      // add geo-spatial coverage alternative form
      ProfileUtils.addIfNotNull(met, "GeospatialCoverageLatSouth", String.valueOf(dataset.getGeospatialCoverage().getLatSouth()));
      ProfileUtils.addIfNotNull(met, "GeospatialCoverageLatNorth", String.valueOf(dataset.getGeospatialCoverage().getLatNorth()));   
      ProfileUtils.addIfNotNull(met, "GeospatialCoverageLonWest", String.valueOf(dataset.getGeospatialCoverage().getLonWest()));
      ProfileUtils.addIfNotNull(met, "GeospatialCoverageLonEast", String.valueOf(dataset.getGeospatialCoverage().getLonEast()));
      
      if (dataset.getGeospatialCoverage().getNames() != null) {
        for (Vocab gName : dataset.getGeospatialCoverage().getNames()) {
          ProfileUtils.addIfNotNull(met, "GeospatialCoverage", gName.getText());
        }
      }

    }

    ProfileUtils.addIfNotNull(met, "History", dataset.getHistory());
    if (dataset.getKeywords() != null) {
      for (Vocab vocab : dataset.getKeywords()) {
        ProfileUtils.addIfNotNull(met, "Keywords", vocab.getText());
      }
    }
    ProfileUtils.addIfNotNull(met, "Name", dataset.getName());
    ProfileUtils.addIfNotNull(met, "Processing", dataset.getProcessing());
    if (dataset.getProjects() != null) {
      for (Vocab vocab : dataset.getProjects()) {
        ProfileUtils.addIfNotNull(met, "Projects", vocab.getText());
      }
    }

    if (dataset.getProperties() != null) {
      for (InvProperty prop : dataset.getProperties()) {
        ProfileUtils.addIfNotNull(met, prop.getName(), prop.getValue());
      }
    }

    if (dataset.getPublishers() != null) {
      for (Source source : dataset.getPublishers()) {
      	// Note: use "Publisher" (singular) as from the OODT profile specification
        ProfileUtils.addIfNotNull(met, "Publisher", source.getName());
      }
    }

    ProfileUtils.addIfNotNull(met, "RestrictAccess",
        dataset.getRestrictAccess());
    if (dataset.getTimeCoverage() != null) {
      String startDateTimeStr = null, endDateTimeStr = null;
      try {
        startDateTimeStr = ProfileUtils.toISO8601(dataset.getTimeCoverage()
            .getStart().getDate());
        endDateTimeStr = ProfileUtils.toISO8601(dataset.getTimeCoverage()
            .getEnd().getDate());
      } catch (Exception e) {
        LOG.log(
            Level.WARNING,
            "Error converting start/end date time strings: Message: "
                + e.getMessage());
      }

      ProfileUtils.addIfNotNull(met, "StartDateTime", startDateTimeStr);
      ProfileUtils.addIfNotNull(met, "EndDateTime", endDateTimeStr);
    }

    if (dataset.getTimeCoverage() != null
        && dataset.getTimeCoverage().getResolution() != null) {
      ProfileUtils.addIfNotNull(met, "TimeCoverageResolution", dataset
          .getTimeCoverage().getResolution().getText());
    }
    // dataset unique ID
    ProfileUtils.addIfNotNull(met, "UniqueID", dataset.getUniqueID());

    // dataset ID is typically not null
    ProfileUtils.addIfNotNull(met, "ID", dataset.getID());

    // generate a UUID for each dataset, to be used as profile ID
    ProfileUtils.addIfNotNull(met, "UUID", UUID.randomUUID().toString());

    // store access services - only the OpenDAP endpoint for now
    for (InvAccess access : dataset.getAccess()) {    	
      String url = access.getStandardUri().toString();
    	String type = access.getService().getServiceType().toString();
    	String name = access.getService().getName();
    	
    	// add opendap access URL
    	if (type.equalsIgnoreCase(ServiceType.OPENDAP.toString())) {
    	  // note: special processing of opendap endpoints since URL in thredds catalog is unusable without a suffix
    		ProfileUtils.addIfNotNull(met,"Access", this.encodeAccessTuple(url+".html", ProfileUtils.MIME_TYPE_OPENDAP_HTML, type));
    	} 
    }
    // add TREDDS XML catalog URL
    String url = dataset.getCatalogUrl(); // catalog_url.xml#dataset_id
    ProfileUtils.addIfNotNull(met,"Access", this.encodeAccessTuple(url, ProfileUtils.MIME_TYPE_THREDDS, "Catalog/XML"));
    ProfileUtils.addIfNotNull(met,"Access", this.encodeAccessTuple(url.replaceAll("\\.xml#", ".html?dataset="), ProfileUtils.MIME_TYPE_HTML, "Catalog/HTML"));

  }
  
	/**
	 * Utility method that joins the parts of an xlink tuple (href, title, type) with a delimiting character.
	 * @param href : the xlink URL, must be not null
	 * @param title : the xlink title, may be null
	 * @param type : the xlink type, may be null
	 * @return
	 */
	private String encodeXlinkTuple(final String href, final String title, final String type) {
	    final StringBuilder tuple = new StringBuilder();
      tuple.append(href)
           .append(ProfileUtils.CHAR)
           .append(StringUtils.hasText(title) ? title : "Reference" )
           .append(ProfileUtils.CHAR)
           .append(StringUtils.hasText(type) ? type : "HTML" );
      return tuple.toString();
	}
	
	/**
	 * Utility method that joins the part of A THREDDS access point (url, service type, service name) with a delimiting character
	 * @param url : the access URL
	 * @param type : the service type, mapped to a mime type
	 * @param name : the service name
	 * @return
	 */
	private String encodeAccessTuple(final String url, final String type, final String name) {
    final StringBuilder tuple = new StringBuilder();
    tuple.append(url)
         .append(ProfileUtils.CHAR)
         .append(StringUtils.hasText(type) ? type : "")
         .append(ProfileUtils.CHAR)
         .append(StringUtils.hasText(name) ? name : "");
    return tuple.toString();
	}

}
