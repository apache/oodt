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
import thredds.catalog.ThreddsMetadata.Contributor;
import thredds.catalog.ThreddsMetadata.GeospatialCoverage;
import thredds.catalog.ThreddsMetadata.Range;
import thredds.catalog.ThreddsMetadata.Source;
import thredds.catalog.ThreddsMetadata.Variable;
import thredds.catalog.ThreddsMetadata.Variables;
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
        ProfileUtils.addIfNotNull(met, "Documentation", doc.getInlineContent());
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
      } else {
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
        }
      }

      ProfileUtils.addIfNotNull(met, "GeospatialCoverageLatitudeResolution",
          String.valueOf(dataset.getGeospatialCoverage().getLatResolution()));
      ProfileUtils.addIfNotNull(met, "GeospatialCoverageLongitudeResolution",
          String.valueOf(dataset.getGeospatialCoverage().getLonResolution()));

      if (dataset.getGeospatialCoverage().getNames() != null) {
        for (Vocab gName : dataset.getGeospatialCoverage().getNames()) {
          ProfileUtils.addIfNotNull(met, "GeospatialCoverage", gName.getText());
        }
      }

    }

    ProfileUtils.addIfNotNull(met, "History", dataset.getHistory());
    ProfileUtils.addIfNotNull(met, "ID", dataset.getID());
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
        ProfileUtils.addIfNotNull(met, "Publishers", source.getName());
      }
    }

    ProfileUtils.addIfNotNull(met, "RestrictAccess",
        dataset.getRestrictAccess());
    ProfileUtils.addIfNotNull(met, "Rights", dataset.getRights());
    ProfileUtils.addIfNotNull(met, "Summary", dataset.getSummary());
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
    if (StringUtils.hasText(dataset.getUniqueID())
        && !dataset.getUniqueID().equalsIgnoreCase("null")) {
      // note: globally unique ID, or string "null" if missing authority or ID
      ProfileUtils.addIfNotNull(met, "UniqueID", dataset.getUniqueID());
    } else {
      // dataset ID is typically not null
      ProfileUtils.addIfNotNull(met, "UniqueID", dataset.getID());
    }
    // generate a UUID for each dataset, to be used as profile ID
    ProfileUtils.addIfNotNull(met, "UUID", UUID.randomUUID().toString());

    if (dataset.getVariables() != null) {
      for (Variables vars : dataset.getVariables()) {
        if (vars.getVariableList() != null) {
          for (Variable var : vars.getVariableList()) {
            // store variable names
            ProfileUtils.addIfNotNull(met, "Variables", var.getName());
            // store variable long names
            ProfileUtils.addIfNotNull(met, "Variable Long Names",
                var.getDescription());
            // store CF standard names
            if (StringUtils.hasText(vars.getVocabulary())
                && vars.getVocabulary().startsWith("CF-")) {
              ProfileUtils.addIfNotNull(met, "CF Standard Names",
                  var.getVocabularyName());
            }
          }
        }
      }
    }

    // store access services
    for (InvAccess access : dataset.getAccess()) {
      ProfileUtils.addIfNotNull(met, access.getService().getServiceType()
          .toString(), access.getStandardUrlName());
    }

  }

}
