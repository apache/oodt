// Copyright 2005 California Institute of Technology.
// ALL RIGHTS RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id: Retrieval.java,v 1.1 2005-01-05 18:22:26 shardman Exp $

package jpl.eda.activity;

import java.sql.SQLException;
import java.util.List;

/**
   Retrieval is a vehicle for getting activities and their associated incidents from {@link Storage}.

   @author S. Hardman
   @version $Revision: 1.1 $
*/
public interface Retrieval {

   /**
      Retrieve the list of activities.

      @return A list of {@link StoredActivity} classes.
      @throws ActivityException If an error occurs accessing the Storage.
   */
   List retrieve() throws ActivityException;
}
