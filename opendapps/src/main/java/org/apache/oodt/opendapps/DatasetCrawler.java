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

package org.apache.oodt.opendapps;

//JDK imports
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

//OODT imports
import org.apache.oodt.cas.metadata.Metadata;

//Spring imports
import org.springframework.util.StringUtils;

//OPeNDAP/THREDDS imports
import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalogRef;
import thredds.catalog.InvDataset;
import thredds.catalog.InvDocumentation;
import thredds.catalog.InvProperty;
import thredds.catalog.InvService;
import thredds.catalog.ServiceType;
import thredds.catalog.ThreddsMetadata.Contributor;
import thredds.catalog.ThreddsMetadata.GeospatialCoverage;
import thredds.catalog.ThreddsMetadata.Range;
import thredds.catalog.ThreddsMetadata.Source;
import thredds.catalog.ThreddsMetadata.Variable;
import thredds.catalog.ThreddsMetadata.Variables;
import thredds.catalog.ThreddsMetadata.Vocab;
import thredds.catalog.crawl.CatalogCrawler;
import ucar.nc2.units.DateType;
import ucar.unidata.geoloc.LatLonRect;

/**
 * Crawls a catalog and returns all the datasets and their references.
 * 
 */
public class DatasetCrawler implements CatalogCrawler.Listener {

  private static Logger LOG = Logger.getLogger(DatasetCrawler.class.getName());

  private List<String> urls = new Vector<String>();

  private Map<String, Metadata> datasetMet;

  private String datasetURL = null;

  public DatasetCrawler(String datasetURL) {
    this.datasetURL = datasetURL.endsWith("/") ? datasetURL : datasetURL + "/";
    this.datasetMet = new HashMap<String, Metadata>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * thredds.catalog.crawl.CatalogCrawler.Listener#getCatalogRef(thredds.catalog
   * .InvCatalogRef, java.lang.Object)
   */
  public boolean getCatalogRef(InvCatalogRef dd, Object context) {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * thredds.catalog.crawl.CatalogCrawler.Listener#getDataset(thredds.catalog
   * .InvDataset, java.lang.Object)
   */
  public void getDataset(InvDataset dd, Object context) {
  	String url = this.datasetURL + dd.getCatalogUrl().split("#")[1];
    String id = dd.getID();    
    LOG.log(Level.INFO, url + " is the computed access URL for this dataset");
    // look for an OpenDAP access URL, only extract metadata if it is found
    List<InvAccess> datasets = dd.getAccess();
    if (dd.getAccess() != null && dd.getAccess().size() > 0) {
      Iterator<InvAccess> sets = datasets.iterator();
      while (sets.hasNext()) {
        InvAccess single = sets.next();
        InvService service = single.getService();
        // note: select the OpenDAP access URL based on THREDDS service type
        if (service.getServiceType()==ServiceType.OPENDAP) {
          LOG.log(Level.INFO, "Found OpenDAP access URL: "+ single.getUrlPath());
          String opendapurl = this.datasetURL + single.getUrlPath();
          this.datasetMet.put(opendapurl, this.extractDatasetMet(dd));
          this.urls.add(opendapurl);
          break;
        }
      }
    }
  }

  /**
   * Gets the set of String {@link URL}s crawled.
   * 
   * @return A {@link List} of {@link String} representations of {@link URL}s.
   */
  public List<String> getURLs() {
    return this.urls;
  }

  /**
   * Returns the exracted THREDDS {@link InvDataset} metadata. The dataset
   * metadata is mapped to the unique THREDDS dataset URL.
   * 
   * @return the exracted THREDDS {@link InvDataset} metadata.
   */
  public Map<String, Metadata> getDatasetMet() {
    return this.datasetMet;
  }

  private Metadata extractDatasetMet(InvDataset dataset) {
  	
  	LOG.log(Level.INFO, "Crawling catalog URL=" + dataset.getCatalogUrl()+" dataset ID="+dataset.getID());
  	
    Metadata met = new Metadata();
    this.addIfNotNull(met, "Authority", dataset.getAuthority());
    this.addIfNotNull(met, "CatalogUrl", dataset.getCatalogUrl());
    this.addIfNotNull(met, "DatasetFullName", dataset.getFullName());
    if (dataset.getContributors() != null) {
      for (Contributor contributor : dataset.getContributors()) {
        this.addIfNotNull(met, "Contributor", contributor.getName());
      }
    }

    if (dataset.getCreators() != null) {
      for (Source source : dataset.getCreators()) {
        this.addIfNotNull(met, "Creator", source.getName());
      }
    }

    if (dataset.getDataFormatType() != null){
    	this.addIfNotNull(met, "DataFormatType", dataset.getDataFormatType()
    			.toString());
    }
    
    if (dataset.getDataType() != null){
    	this.addIfNotNull(met, "DataType", dataset.getDataType().toString());
    }
    
    if (dataset.getDates() != null) {
      for (DateType dateType : dataset.getDates()) {
        String dateString = null;
        try {
          dateString = toISO8601(dateType.getDate());
        } catch (Exception e) {
          LOG.log(Level.WARNING, "Error converting date: ["
              + dateType.getDate() + "]: Message: " + e.getMessage());
        }
        this.addIfNotNull(met, "Dates", dateString);
      }
    }

    if (dataset.getDocumentation() != null) {
      for (InvDocumentation doc : dataset.getDocumentation()) {
        this.addIfNotNull(met, "Documentation", doc.getInlineContent());
      }
    }

    this.addIfNotNull(met, "FullName", dataset.getFullName());
    GeospatialCoverage geoCoverage = dataset.getGeospatialCoverage();
    if (geoCoverage != null) {
      LatLonRect bbox = geoCoverage.getBoundingBox();
      if (bbox != null) {
        this.addIfNotNull(met, "SouthwestBC", bbox.getLowerLeftPoint()
            .toString());
        this.addIfNotNull(met, "NorthwestBC", bbox.getUpperLeftPoint()
            .toString());
        this.addIfNotNull(met, "NortheastBC", bbox.getUpperRightPoint()
            .toString());
        this.addIfNotNull(met, "SoutheastBC", bbox.getLowerRightPoint()
            .toString());
      } else {
        // try north south, east west
        if (geoCoverage.getNorthSouthRange() != null) {
          Range nsRange = geoCoverage.getNorthSouthRange();
          this.addIfNotNull(met, "NorthSouthRangeStart", String.valueOf(nsRange
              .getStart()));
          this.addIfNotNull(met, "NorthSouthRangeResolution", String
              .valueOf(nsRange.getResolution()));
          this.addIfNotNull(met, "NorthSouthRangeSize", String.valueOf(nsRange
              .getSize()));
          this.addIfNotNull(met, "NorthSouthRangeUnits", nsRange.getUnits());
        }

        if (geoCoverage.getEastWestRange() != null) {
          Range nsRange = geoCoverage.getEastWestRange();
          this.addIfNotNull(met, "EastWestRangeStart", String.valueOf(nsRange
              .getStart()));
          this.addIfNotNull(met, "EastWestRangeResolution", String
              .valueOf(nsRange.getResolution()));
          this.addIfNotNull(met, "EastWestRangeSize", String.valueOf(nsRange
              .getSize()));
          this.addIfNotNull(met, "EastWestRangeUnits", nsRange.getUnits());
        }
      }

      this.addIfNotNull(met, "GeospatialCoverageLatitudeResolution", String
          .valueOf(dataset.getGeospatialCoverage().getLatResolution()));
      this.addIfNotNull(met, "GeospatialCoverageLongitudeResolution", String
          .valueOf(dataset.getGeospatialCoverage().getLonResolution()));
      
      if(dataset.getGeospatialCoverage().getNames() != null){
        for(Vocab gName: dataset.getGeospatialCoverage().getNames()){
           this.addIfNotNull(met, "GeospatialCoverage", gName.getText());
        }
      }
      
    }

    this.addIfNotNull(met, "History", dataset.getHistory());
    this.addIfNotNull(met, "ID", dataset.getID());
    if (dataset.getKeywords() != null) {
      for (Vocab vocab : dataset.getKeywords()) {
        this.addIfNotNull(met, "Keywords", vocab.getText());
      }
    }
    this.addIfNotNull(met, "Name", dataset.getName());
    this.addIfNotNull(met, "Processing", dataset.getProcessing());
    if (dataset.getProjects() != null) {
      for (Vocab vocab : dataset.getProjects()) {
        this.addIfNotNull(met, "Projects", vocab.getText());
      }
    }

    if (dataset.getProperties() != null) {
      for (InvProperty prop : dataset.getProperties()) {
        this.addIfNotNull(met, prop.getName(), prop.getValue());
      }
    }

    if (dataset.getPublishers() != null) {
      for (Source source : dataset.getPublishers()) {
        this.addIfNotNull(met, "Publishers", source.getName());
      }
    }

    this.addIfNotNull(met, "RestrictAccess", dataset.getRestrictAccess());
    this.addIfNotNull(met, "Rights", dataset.getRights());
    this.addIfNotNull(met, "Summary", dataset.getSummary());
    if (dataset.getTimeCoverage() != null) {
      String startDateTimeStr = null, endDateTimeStr = null;
      try {
        startDateTimeStr = toISO8601(dataset.getTimeCoverage()
            .getStart().getDate());
        endDateTimeStr = toISO8601(dataset.getTimeCoverage()
            .getEnd().getDate());
      } catch (Exception e) {
        LOG.log(Level.WARNING,
            "Error converting start/end date time strings: Message: "
                + e.getMessage());
      }

      this.addIfNotNull(met, "StartDateTime", startDateTimeStr);
      this.addIfNotNull(met, "EndDateTime", endDateTimeStr);
    }

    if (dataset.getTimeCoverage() != null && dataset.getTimeCoverage().getResolution() != null) {
      this.addIfNotNull(met, "TimeCoverageResolution", dataset
          .getTimeCoverage().getResolution().getText());
    }
    // dataset unique ID
    if (StringUtils.hasText(dataset.getUniqueID()) && !dataset.getUniqueID().equalsIgnoreCase("null")) {
    	// note: globally unique ID, or string "null" if missing authority or ID
    	this.addIfNotNull(met, "UniqueID", dataset.getUniqueID());
    } else {
    	// dataset ID is typically not null
    	this.addIfNotNull(met, "UniqueID", dataset.getID());
    }

    if (dataset.getVariables() != null) {
      for (Variables vars : dataset.getVariables()) {
        if (vars.getVariableList() != null) {
          for (Variable var : vars.getVariableList()) {
            this.addIfNotNull(met, "Variables", var.getName());
          }
        }
      }
    }
    return met;
  }

  private void addIfNotNull(Metadata met, String field, String value) {
    if (value != null && !value.equals("")) {
      met.addMetadata(field, value);
    }
  }
  
  // inspired from ASLv2 code at:
  // http://www.java2s.com/Code/Java/Data-Type/ISO8601dateparsingutility.htm
  private String toISO8601(Date date) {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    TimeZone tz = TimeZone.getTimeZone("UTC");
    df.setTimeZone(tz);
    String output = df.format(date);
    return output;
  }

}
