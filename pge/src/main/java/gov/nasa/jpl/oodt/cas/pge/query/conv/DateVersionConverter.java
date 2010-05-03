//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pge.query.conv;

//OODT imports
import gov.nasa.jpl.oodt.cas.commons.date.DateUtils;
import gov.nasa.jpl.oodt.cas.filemgr.structs.query.conv.VersionConverter;

//JDK imports
import java.text.ParseException;
import java.util.Calendar;

/**
 * 
 * @author bfoster
 * @version $Revision$
 *
 */
public class DateVersionConverter implements VersionConverter {

    public double convertToPriority(String version) throws ParseException {
        Calendar cal = DateUtils.toCalendar(version,
                DateUtils.FormatType.UTC_FORMAT);
        return (cal.get(Calendar.YEAR) * 10000)
                + ((cal.get(Calendar.MONTH) + 1) * 100)
                + (cal.get(Calendar.DAY_OF_MONTH));
    }

}
