// Copyright 2002 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: SearchableProfile.java,v 1.1.1.1 2004/03/02 20:53:27 kelly Exp $

package jpl.eda.profile.handlers.lightweight;

import jpl.eda.profile.ObjectFactory;
import jpl.eda.profile.Profile;
import org.w3c.dom.Element;

/**
 * A profile that can be searched.
 *
 * @author Kelly
 * @version $Revision: 1.1.1.1 $
 */
public class SearchableProfile extends Profile {
	public SearchableProfile(Element node, ObjectFactory factory) {
		super(node, factory);
	}

	/**
	 * Search this profile.
	 *
	 * @param expression Statement of what to retrieve.
	 */
	public Result search(WhereExpression expression) {
		return expression.result((SearchableResourceAttributes) resAttr, elements);
	}
}
