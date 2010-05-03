package jpl.eda.profile.handlers.oracle;

import java.sql.*;
import java.lang.*;
import java.util.*;
import jpl.eda.Configuration;
import jpl.eda.util.*;
import jpl.eda.util.DOMParser;
import jpl.eda.xmlquery.*;
import jpl.eda.profile.*;
import jpl.eda.profile.handlers.*;
import java.io.*;
import java.util.Vector;
import org.w3c.dom.*;
import org.xml.sax.*;
import oracle.sql.*;
import oracle.jdbc.driver.*;
/**********************************************************************************
**
** OracleProfileImpl.java
**
** @author Dan Crichton
** 
** date: 11/16/2000
**
** description: Implement profile management for Oracle
**
***********************************************************************************/


public class OracleProfileImpl extends DatabaseProfileManager
{
	Properties local_props;
	
	/** {@inheritDoc} */
	public String getID() {
		return local_props.getProperty("id", "oracle");
	}

//	static String profileServers = "(SELECT R.PROFILE_SEQUENCE from RESOURCE_ATTRIBUTES R WHERE" +
//                 " NOT EXISTS (SELECT PROFILE_SEQUENCE FROM PROFILE_ELEMENT E WHERE "+
//                " E.PROFILE_SEQUENCE = R.PROFILE_SEQUENCE) "+
//                 " and R.RESCLASS='system.profileServer')";

   /**********************************************************************
    **
    ** OracleProfileImpl
    **
    ** constructor classes
    ***********************************************************************/

    public OracleProfileImpl() throws Exception {
                this(System.getProperties());
    }

    public OracleProfileImpl(Properties props) throws Exception
    {
	super(props);
	local_props = props;
    }

    public OracleProfileImpl(Properties props, Connection conn)
    {
	super(props, conn);
    }
    
    public List findProfiles(Connection conn, XMLQuery query) throws DOMException, ProfileException
    {
        try
	{
		System.err.println("Got query: " + query.getKwdQueryString());
		String myProfileList = getProfile(conn, query);
		return Profile.createProfiles(XML.parse(myProfileList).getDocumentElement());
	}
	catch (SQLException e)
	{
		throw new ProfileSQLException(e);	
	}
 	catch (SAXException ex) 
	{
                        throw new IllegalArgumentException("Can't parse profile: "
 			+ ex.getMessage());
	}

    }

   /**********************************************************************
    **
    ** get
    **
    **  Find the profile that matches the profID
    ***********************************************************************/
    public Profile get(Connection conn, String profID) throws ProfileException
    {
	Statement stmt=null;
	ResultSet rs=null;
        try
	{
		if (conn == null) conn = openConnection(local_props);
		stmt = conn.createStatement();
		String cmd = "select getCompleteXMLProfile(profile_sequence) " +
			"from (select profile_sequence from profile where profile_id = '" +
			profID +"' and rownum < 2)";

		rs = stmt.executeQuery(cmd);
    	    	StringBuffer result_str = new StringBuffer(
    	    		"<?xml version=\"1.0\"?>" +
	    		"<!DOCTYPE profile PUBLIC \"" + 
			Profile.PROFILES_DTD_FPI+"\" \"" +
			Profile.PROFILES_DTD_URL + "\">") ;
		StringBuffer result = new StringBuffer();

		java.sql.Clob profileClob = null;
		String profileStr = new String();
		int length = 0;
		while (rs.next())
		{
			profileClob = rs.getClob(1);			
			if(profileClob != null)
	                {
        	                length = (int)profileClob.length();
                	        profileStr = profileClob.getSubString(1,length);
                        	result.append(profileStr);
                	}
		}

		if(result.length() != 0)
		{
			result_str.append(result);
			return (new Profile(result_str.toString()));
		}
		else return (new Profile());
	}
	catch (SQLException e)
	{
		e.printStackTrace();
		throw new ProfileSQLException(e);	
	}
 	catch (SAXException ex) 
	{
	       ex.printStackTrace();
               throw new IllegalArgumentException("Can't parse profile: "
 			+ ex.getMessage());
	}
	catch (Exception e)
	{
		e.printStackTrace();
		throw new ProfileException("Failed to get clob data from getCompleteXMLProfile");
	}
	finally 
	{
            try {
		rs.close();
                stmt.close();
            } catch (SQLException se) {
                throw new ProfileSQLException(se);
            }
        }
    }

   /**********************************************************************
    **
    ** getProfile
    **
    ** Find profiles that match the XML Query Definition.
    ***********************************************************************/

    public  String  getProfile(Connection conn, XMLQuery query) throws SQLException, ProfileException
    {
	StringBuffer result_str = new StringBuffer(
    	    		"<?xml version=\"1.0\"?>" +
	    		"<!DOCTYPE profiles PUBLIC \"" + 
			Profile.PROFILES_DTD_FPI+"\" \"" +
			Profile.PROFILES_DTD_URL + "\">" + "<profiles>"); 

	PreparedStatement stmt=null;
	ResultSet rs=null;
	try
	{
	    java.sql.Clob profiles_clob = null;
            String profileStr = new String();
            int len = 0;
 	    char [] buf=null;
	    BufferedReader result = null;

            if(conn== null) conn = openConnection(local_props);
            stmt = getSQLString(query, conn);
            rs = stmt.executeQuery();

	    while (rs.next())
            {
                // The getXMLProfile and getCompleteXMLProfile stored functions
                // return the results in CLOB
                profiles_clob = rs.getClob(1);
                result = new BufferedReader(profiles_clob.getCharacterStream());
                len = (int)profiles_clob.length();
                buf = new char[len];
                result.read(buf,0,len);
                result.close();
                result_str.append(buf);
            }

	    result_str.append("</profiles>");
//System.err.println(result_str.toString());
	    return(result_str.toString());
	}
	catch (Exception e)
	{
		e.printStackTrace();
		throw new ProfileException("Failed to get clob data from getCompleteXMLProfile");
	}
	finally 
	{
		rs.close();
                stmt.close();
        }
    }


    /**********************************************************************
    **
    ** getSQLString
    **
    ** Convert XML Query into SQL String
    ***********************************************************************/	

    public PreparedStatement getSQLString(XMLQuery query, Connection conn)
	throws SQLException
    {
	String sql_str;
	List from = query.getFromElementSet();
	boolean foundRange = false;


	/*
	** Approach
	** 1. Process FROM, WHERE, and then SELECT
	** 2. Each clause has 1 or more elements separated by operators
	** 
	** The WHERE clause maps to ELEMENT.Element_Name and ELEMENT_VALUE.Value_Instance
	** The SELECT clause specifies what ELEMENT.Element_Name to include in the
	** returned profile.  The element names will be passed to the getMatchXMLProfile stored function.
	*/

	/*
	** Process the where constraint
	*/

	Stack where_stack = new Stack();
	Stack alias_stack = new Stack();
	List where = query.getWhereElementSet();
	String whereElementList = "";
	int cur_alias = 0;

	for (Iterator i = where.iterator(); i.hasNext();) {
		QueryElement queryElement = (QueryElement) i.next();
		String keyword = queryElement.getValue();
		String ktype = queryElement.getRole();

		/*
		** Look at the keyword type.
                ** If it is a element, then push 
                ** If it is a literal, then push with single quotes
                ** If it is an operator, then 
 		**   pop once, concat the operator, pop once 
		*/

		if (ktype.equals("elemName"))
		{
			/*
			** This is a value in the profile database
			*/

			where_stack.push("'"+keyword+ "'");

			if (whereElementList.equals("")) {
				whereElementList += "(";
			} else {
				whereElementList += ",";
			}
       		        whereElementList += "'" + keyword + "'";
		}
		else if(ktype.equals("LITERAL"))
		{
			if (jpl.eda.util.Utility.isNumeric(keyword)) {
				where_stack.push(keyword);
			} else {
				where_stack.push("'"+ keyword +"'");
			}

		}
		else if(ktype.equals("RELOP"))
		{
			String elemSqlStr = "";

			String valueStr = (String) where_stack.pop();
			String op = getOperatorSQLMap(keyword);
			String elemStr = (String) where_stack.pop();
				
			if (! jpl.eda.util.Utility.isNumeric(valueStr)) {
				elemSqlStr = "(select PROFILE_SEQUENCE from " +
					"ELEMENT_VALUE V" + ++cur_alias + " where " +
					" ELEMENT_NAME="+elemStr+
					" and VALUE_INSTANCE "+op+" "+valueStr;
				alias_stack.push("V" + cur_alias);
			} else {

			/*
			** The following will check if the value_element in the query structure
			** is numeric. If it is then it will add a check  to look at the range
			** values (min/max) and the enumerated values in the profile
			*/
				elemSqlStr = "(select PROFILE_SEQUENCE from ((" +
					"select PROFILE_SEQUENCE from " +
					"(select PROFILE_SEQUENCE, VALUE_INSTANCE from " +
					"ELEMENT_VALUE where " +
					" ELEMENT_NAME="+elemStr+
					" and IS_NUMERIC='Y') " +
					"where VALUE_INSTANCE "+op+" "+valueStr;

				String elemSqlStr2 = " select E.PROFILE_SEQUENCE "+
					"from ELEMENT E where ";

				if ((op.equals("<")) || (op.equals("<=")))
				{
					elemSqlStr2 +=
                     	                " E.ELEMENT_NAME="+elemStr+
					" and E.ELEMENT_MIN_VALUE <= "+valueStr+
					" and E.ELEMENT_MIN_VALUE IS NOT NULL ";
				}
				else if ((op.equals(">")) || (op.equals(">=")))
				{
					elemSqlStr2 +=
                     	                " E.ELEMENT_NAME="+elemStr+
					" and E.ELEMENT_MAX_VALUE >= "+valueStr+
					" and E.ELEMENT_MAX_VALUE IS NOT NULL";
				}
				else
				{
					elemSqlStr2 +=
                     	                " E.ELEMENT_NAME="+elemStr+
					" and E.ELEMENT_MIN_VALUE <= "+valueStr+
					" and E.ELEMENT_MAX_VALUE >= "+valueStr+
					" and E.ELEMENT_MIN_VALUE IS NOT NULL "+
					" and E.ELEMENT_MAX_VALUE IS NOT NULL";
				}
				
				elemSqlStr += ") UNION (" +
					elemSqlStr2+")) P" + ++cur_alias + " where 0 = 0 ";
				alias_stack.push("P" + cur_alias);
				// P alias stands for pseudo table
			}
			where_stack.push(elemSqlStr);

		}
		else if(ktype.equals("LOGOP"))
		{
			// Sean sez: there's a bug here ... what if the logical operator is a NOT?
			// You end up popping two operands off the stack, but a NOT applies to only
			// one operand at a time.
			String temp_str = (String)where_stack.pop(); 
			String alias_str = (String)alias_stack.pop();

			if (keyword.equals("AND")) { 
				temp_str += " and exists " + where_stack.pop() + " and " +
					alias_stack.pop() + ".PROFILE_SEQUENCE" +
					"= " + alias_str + ".PROFILE_SEQUENCE) ";
				
			}
			else if (keyword.equals("OR")) {
				alias_stack.pop();
				alias_str = "P" + ++cur_alias;
				temp_str = "(select PROFILE_SEQUENCE from (" +
					temp_str + ") UNION " + where_stack.pop() + ")) " +
						alias_str + " where 0 = 0 ";
			}
			else { // not sure what to do here yet
				temp_str += where_stack.pop() + " " + keyword + " " + temp_str;
			}

			where_stack.push(temp_str);
			alias_stack.push(alias_str);

		}
		else
			System.err.println("bogus keyword type found in where clause="+ktype);
		

     	} 

	if (! whereElementList.equals("")) {
		whereElementList += ")";
	}

	/*
	** Add the where and from stack constraints
	*/


	/*
	** Get the select elements  and put it in a list.
	*/
        List select = query.getSelectElementSet();
	ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor("NAMEARRAY", conn);
	String [] elements = new String[select.size()];

	for(int i=0;i<elements.length;i++) 
		elements[i]=((QueryElement)select.get(i)).getValue();
	oracle.sql.ARRAY elementList = new oracle.sql.ARRAY(descriptor,conn, elements);

	String resultMethod;
	boolean setElementList = false;
	if (query.getResultModeID().equals("profileFull")) {
	   	resultMethod = "getCompleteXMLProfile(PROFILE_SEQUENCE)";
        }
	else if (elementList.length()<=0) {
		resultMethod = "getXMLProfile(PROFILE_SEQUENCE)";
	}
	else {
		resultMethod = "getMatchXMLProfile(PROFILE_SEQUENCE,elementList)";
		setElementList = true;
	}


	if (where_stack.size() > 0)
		sql_str = "SELECT " + resultMethod + " from ("+
			 "SELECT distinct PROFILE_SEQUENCE from ( "+
			where_stack.pop()+")";
	else
		sql_str = "SELECT " + resultMethod + " from ("+
			 "SELECT distinct E.PROFILE_SEQUENCE from ELEMENT E";

	// this is to get all profile servers with no specified elements
//	sql_str += " UNION " + profileServers;

	// this is to get all profile servers that know something about the specified elements
	// but no element values exist
/*
	if (! whereElementList.equals("")) {
		sql_str += " UNION (SELECT distinct R.PROFILE_SEQUENCE from " +
			"RESOURCE_ATTRIBUTES R WHERE " +
			"R.RESCLASS='system.profileServer' AND " +
                 	"EXISTS (SELECT PROFILE_SEQUENCE FROM ELEMENT E WHERE "+
	                "E.PROFILE_SEQUENCE = R.PROFILE_SEQUENCE AND " +
			"E.ELEMENT_NAME IN " + whereElementList +  
			" AND NOT EXISTS (SELECT ELEMENT_SEQUENCE FROM ELEMENT_VALUE V WHERE " +
			"V.ELEMENT_SEQUENCE = E.ELEMENT_SEQUENCE))) ";
	}
*/
	sql_str += ")";
	if(query.getMaxResults()>0) sql_str += " WHERE rownum < "+query.getMaxResults();
	sql_str += ")";
System.err.println(sql_str);
	PreparedStatement stmt = conn.prepareStatement(sql_str);
        if (setElementList) 
	        ((OraclePreparedStatement)stmt).setARRAY(1, elementList);

	return(stmt);
    }


    /**********************************************************************
    **
    ** getOperatorSQLMap
    **
    ** Map the operator in the XML Query Str to SQL
    **********************************************************************/
    private String getOperatorSQLMap(String operator)
    {

        if (operator.equals("EQ"))
                return("like");
        else if (operator.equals("NE"))
                return("<>");
        else if (operator.equals("GT"))
                return(">");
        else if (operator.equals("LT"))
                return("<");
        else if (operator.equals("GE"))
                return(">=");
        else if (operator.equals("LE"))
                return("<=");
	return(null);
    }

    /**********************************************************************
    **
    ** getProfileSequence
    **
    ** @desc Retrieve a sequence number from the db
    **********************************************************************/
    int getProfileSequence(Connection conn) throws SQLException, ProfileException
    {
	Statement stmt=null;
	ResultSet rs=null;

	try {
		if(conn == null) conn= openConnection(local_props);
		stmt = conn.createStatement();
        	rs = stmt.executeQuery("select Profile_Seq.nextval from dual");
        	int profSeq =0;

        	while (rs.next())profSeq = rs.getInt(1);
		return(profSeq);
	} catch ( Exception e) {
                throw new ProfileException(e.getMessage());
        }
	finally {
        	rs.close(); 
	       	stmt.close();
        }
    }

    /**********************************************************************
    **
    ** loadProfileAttributes
    **
    ** @desc Insert profile attributes into the database from the profile
    **********************************************************************/
    void loadProfileAttributes(
                Connection conn, 
                Profile profile ,
		int profSeq) throws ProfileException
    {
	ProfileAttributes profAttr = profile.getProfileAttributes();
	
	// add profile attributes 
	try {
		loadAttribute(conn, "profId", profAttr.getID(), profSeq, profile);
		loadAttribute(conn, "profVersion", profAttr.getVersion(), profSeq, profile);
		loadAttribute(conn, "profType", profAttr.getType(), profSeq, profile);
		loadAttribute(conn, "profStatusId", profAttr.getStatusID(), profSeq, profile);
		loadAttribute(conn, "profSecurityType", profAttr.getSecurityType(), profSeq, profile);
		loadAttribute(conn, "profParentId", profAttr.getParent(), profSeq, profile);
		loadAttribute(conn, "profChildId", profAttr.getChildren(), profSeq, profile);
		loadAttribute(conn, "profRegAuthority", profAttr.getRegAuthority(), profSeq, profile);
		loadAttribute(conn, "profRevisionNote", profAttr.getRevisionNotes(), profSeq, profile);		
	} catch(Exception se) {
                throw new ProfileException(se.getMessage());
	}
    }

    /**********************************************************************
    **
    ** loadResourceAttributes
    **
    ** @desc Insert resource attributes into the database from the profile
    **********************************************************************/
    void loadResourceAttributes(
                Connection conn, 
                Profile profile,
		int profSeq ) throws ProfileException
    {
	ResourceAttributes resAttr = profile.getResourceAttributes();

	try {
	// add resource attributes 
        loadAttribute(conn, "Identifier", resAttr.getIdentifier(), profSeq, profile);
	loadAttribute(conn, "Title", resAttr.getTitle(), profSeq, profile);
	loadAttribute(conn, "Format", resAttr.getFormats(), profSeq, profile);
	loadAttribute(conn, "Description", resAttr.getDescription(), profSeq, profile);
	loadAttribute(conn, "Creator", resAttr.getCreators(), profSeq, profile);
	loadAttribute(conn, "Subject", resAttr.getSubjects(), profSeq, profile);
	loadAttribute(conn, "Publisher", resAttr.getPublishers(), profSeq, profile);
	loadAttribute(conn, "Contributor", resAttr.getContributors(), profSeq, profile);
	loadAttribute(conn, "Date", resAttr.getDates(), profSeq, profile);
	loadAttribute(conn, "Type", resAttr.getTypes(), profSeq, profile);
	loadAttribute(conn, "Source",resAttr.getSources(), profSeq, profile);
	loadAttribute(conn, "Language",resAttr.getLanguages(), profSeq, profile);
	loadAttribute(conn, "Relation",resAttr.getRelations(), profSeq, profile);
	loadAttribute(conn, "Coverage", resAttr.getCoverages(), profSeq, profile);
	loadAttribute(conn, "Rights", resAttr.getRights(), profSeq, profile);
	loadAttribute(conn, "resContext", resAttr.getResContexts(), profSeq, profile);
	loadAttribute(conn, "resAggregation", resAttr.getResAggregation(), profSeq, profile);
	loadAttribute(conn, "resClass", resAttr.getResClass(), profSeq, profile);
	loadAttribute(conn, "resLocation", resAttr.getResLocations(), profSeq, profile);
	} catch(Exception se) {
                throw new ProfileException(se.getMessage());
	}
    }

    /**********************************************************************
    **
    ** loadAttribute
    ** 
    ** @desc Load the attribute into the database.
    **********************************************************************/
    void  loadAttribute(Connection conn, String attribute, String value, 
		int profSeq, Profile profile) throws SQLException, Exception, ProfileException
    {
	List values = new ArrayList();

	if(value != null && !value.equals(""))
	{
		values.add(value);
		loadAttribute(conn, attribute, values, profSeq, profile);
	}
	else
	if("profType".equals(attribute) || "profStatusId".equals(attribute)
		|| "Identifier".equals(attribute) || "resClass".equals(attribute))
	{
		values.add("");
                loadAttribute(conn, attribute, values, profSeq, profile);
	}
    }

    void loadAttribute(Connection conn, String attribute, List values, 
		int profSeq, Profile profile)throws SQLException, Exception, ProfileException
    {
	if("resContext".equals(attribute) && values==null) values.add("");

	// add the attribute into the profile element map
	if(values != null)
	{
		ProfileElement element 
			= new EnumeratedProfileElement(profile, attribute, 
				attribute/*id*/, ""/*desc*/, ""/*type*/,
				""/*unit*/, new ArrayList()/*synonyms*/, 
				false/*obligation*/, 0/*maxOccurrence*/,
				""/*comment*/, values);
		loadElement(conn, element, profSeq);
	}
    }

    /**********************************************************************
    **
    ** getElementSequence
    **
    ** @desc Get the next element sequence nubmer from the database
    **********************************************************************/
    int getElementSequence(Connection conn) throws SQLException,Exception
    {
	Statement stmt=null;
	ResultSet rs=null;

	try {
		if(conn==null)conn=openConnection(local_props);

		stmt = conn.createStatement();
        	rs = stmt.executeQuery("select Element_Seq.nextval from dual");

        	int elementSeq =0;
	        while (rs.next())
	                elementSeq = rs.getInt(1);
		return(elementSeq);
	}
        catch (Exception e) {
                throw e;
        }
	finally {
		rs.close();
                stmt.close();
        }
    }

    /**********************************************************************
    **
    ** loadProfileElements
    **
    ** @desc Load the profile elements into the database
    **********************************************************************/
    void loadProfileElements(
                Connection conn, 
                Profile profile, 
                int profSeq) throws Exception, ProfileException
    {
	for (Iterator eachElem = profile.getProfileElements().values().iterator(); eachElem.hasNext();)
        {
                        ProfileElement element = (ProfileElement) eachElem.next();
			loadElement(conn, element, profSeq);
	}
    }

    void loadElement(
		Connection conn,
                ProfileElement element,
                int profSeq) throws Exception, ProfileException
    {
        String sql = null;
	Statement stmt=null;
	int elementSeq;

	try {
                if(conn == null) conn= openConnection(local_props);
		stmt = conn.createStatement();

                elementSeq = getElementSequence(conn);

		//insert into element
                // Warning: elements may have multiple synonyms, but there's room for just one in the DB:
                sql = "insert into Element(Profile_Sequence,"+
                        "Element_Sequence, Element_Name, Element_Description, Element_Type,"+
                        "Element_Unit, Element_ID, Element_Synonym, Element_Min_Value,"+
                        "Element_Max_Value, Element_Obligation, Element_Max_Occurrence,"+
                        "Element_Comment) values (" +
                        profSeq + 
                        "," + elementSeq + ",'" +
                	XML.escape(element.getName()==null? "":element.getName()) +"','"+
			XML.escape(element.getDescription()==null? "":element.getDescription()) +"','" +
			XML.escape(element.getType()==null? "":element.getType()) +"','"+
                        XML.escape(element.getUnit()==null? "":element.getUnit()) +"','"+
                        XML.escape(element.getID()==null? "":element.getID()) +"','" +
			(element.getSynonyms().isEmpty() ? "" :XML.escape((String)element.getSynonyms().get(0)))+"','" +
                        XML.escape(element.getMinValue()==null? "":element.getMinValue()) +"','"+
                        XML.escape(element.getMaxValue()==null? "":element.getMaxValue()) +"','" + 
                	(element.isObligatory()? "T" : "F") +"','" +
                	element.getMaxOccurrence()+"','"+
                	XML.escape(element.getComments()==null? "":element.getComments())+ "')";

                	stmt.executeUpdate(sql);

		//insert into element_value
                List values = element.getValues();
                for (Iterator eachValue = values.iterator(); eachValue.hasNext();)
               	{
			String instance_value = (String)eachValue.next();
                        sql = "insert into Element_Value(Profile_Sequence,"+
                              	"Element_Sequence, Value_Sequence, Value_Instance, " +
				"Element_Name, Is_Numeric) values ("+
                                profSeq + 
                                ","+ elementSeq+","+
                                "Value_Seq.nextval," +
                                "'" + XML.escape(instance_value) + "'," +
  				"'" + XML.escape(element.getName()) + "'," +
				(jpl.eda.util.Utility.isNumeric(instance_value)? "'Y'" : "'N'")+")";

                        stmt.executeUpdate(sql);
	        }
	} catch ( Exception e) {
                throw new ProfileException(e.getMessage());
        }
	finally {
                stmt.close();   
        }
    }


    /**********************************************************************
    **
    ** loadProfile
    **
    ** @desc Load a profile into the database
    **********************************************************************/
    void loadProfile(Connection conn, 
		Profile profile, int profSeq) throws SQLException,ProfileException
    {	
	Statement stmt=null;
	try {
                if(conn == null) conn= openConnection(local_props);
		stmt = conn.createStatement();
		ProfileAttributes profAttr = profile.getProfileAttributes();

		String id = profAttr.getID();
		//if profId is null or empty, construct it from resource
  	      	// context and resource identifier
        	if(id == null || id.equals(""))
        	{
	                String identifier =  profile.getResourceAttributes().getIdentifier();
        	        if(identifier ==null || identifier.equals(""))
                	        throw new ProfileException("The following profile does not have profile id and resource id :\n"
                                +XML.escape(profile.toString()));
                	else
                        	id = identifier;

                	List context = profile.getResourceAttributes().getResContexts();
                	if(context.size() >0)
                        	id = (String)context.get(0)+"."+id;
			profAttr.setID(id);
        	}
		id= XML.escape(id);
		
		String type = profAttr.getType();
		if(type != null) type = XML.escape(type);

		String version = profAttr.getVersion();
		if( version==null || version.equals("")) version= null;
		if(version != null) version = XML.escape(version);
	
		String sql = "insert into Profile (Profile_Sequence,  Profile_Id, Doc_Type, Version)"+
                	" values ("+profSeq+",'"+ id +"', '" + type +
			"'," + (version == null || version.equals("") ? null:"'"+version+"'")+")";

		stmt.executeUpdate(sql);

	} catch ( Exception e) {
                throw new ProfileException(e.getMessage());
        } finally {
                stmt.close();
        }
    }

    /**********************************************************************
    **    
    ** addALL
    **  
    **  Add a collection of profiles to the Oracle db server
    *********************************************************************/
    public void addAll(Connection conn, Collection collection) throws ProfileException
    {
	Object [] profObject = collection.toArray();
	String err = new String();
//	String errMess ="";
//        ProfileValidate pv = new ProfileValidate();

	for(int i=0;i<profObject.length;i++)
	{
		Profile profile = (Profile) profObject[i];
		String version = profile.getProfileAttributes().getVersion();
	        String id = profile.getProfileAttributes().getID();

        	try
        	{
			//validate the profile
//			if(!(errMess=pv.validate(profile)).equals(""))
//	                        err += "\n\nError occured when profile (profileId="
//      	                        + id + ", version="+version+") is registered."+errMess;
//			else add(conn,profile);

			add(conn,profile);
        	}
        	catch (ProfileException e) {
			err += "\n\nError occured when profile (profileId="
                                 + id + ", version="+version+") is registered.\n" 
				 + e.getMessage();
        	}
	}
	
	if(!err.equals(""))
               throw new ProfileException(err);
    }

    
    /**********************************************************************
    **
    ** add
    **
    **  Add a profile to the Oracle db server
    **********************************************************************/
    public void add(Connection conn, Profile profile) throws ProfileException
    {
	Statement stmt = null;
	ResultSet rs = null;

	try
	{
	  if(conn==null)conn=openConnection(local_props);
          stmt = conn.createStatement();

	  ProfileAttributes profAttr = 
		profile.getProfileAttributes();

	  String version = profAttr.getVersion();
		if(version != null) version = XML.escape(version);

	  String id = profAttr.getID();
		if(id != null) id = XML.escape(id);
	
	 /*
	  ** Check and see if profile is already in the db
          */

          String cmd;

	  if (version != null && !version.equals(""))
          	cmd = new String(
	  		 "select count(*) from profile where "+
	   		 " version = '"+version+"' and " +
           		 " Profile_Id = '"+id+"'"); 
	  else
          	cmd = new String(
	  		 "select count(*) from profile where "+
			 " version is null and " +
           		 " Profile_Id = '"+id+"'"); 

	  int cnt = 0;

	  rs = stmt.executeQuery(cmd);

	  while (rs.next())
            	cnt = rs.getInt(1);

          if (cnt > 0)
		throw new ProfileException("Profile Already Exists.  profile_id ="+id+"   version = "+version);

      	  int profSeq = getProfileSequence(conn);
       	  loadProfile(conn, profile, profSeq);
          loadProfileAttributes(conn, profile, profSeq);
          loadResourceAttributes(conn, profile, profSeq);
          loadProfileElements(conn, profile, profSeq);
	}
        catch (Exception e)
        {
		throw new ProfileException(e.getMessage());
        }
	finally {
            try {
                rs.close();  
		stmt.close();
            } catch (SQLException se) {
                throw new ProfileSQLException(se);
            }
        }
    }

    /**********************************************************************
    **
    ** size
    **
    **  Return the number of profiles managed by the server
    **********************************************************************/
    public int size(Connection conn) throws ProfileException
    {
	int size = 0;
	Statement stmt=null;
	ResultSet rs=null;
	try
	{
		if(conn==null)conn=openConnection(local_props);
	        stmt = conn.createStatement();

		String cmd = "select count(*) from element";
		rs = stmt.executeQuery(cmd);

		while (rs.next())
			size = rs.getInt(1);
		return(size);
	}
        catch (SQLException e)  {
		throw new ProfileSQLException(e);
        }
	finally {
            try {
		rs.close();
                stmt.close();
            } catch (SQLException se) {
                throw new ProfileSQLException(se);   
            }    
        }
    }

    /**********************************************************************
    **
    ** remove
    **
    **  Remove the profile with given profile id and version 
    **********************************************************************/

    public boolean remove(Connection conn, String profId, String version) throws ProfileException
    {
	if(version !=null && version.equals("")) version=null;
	Statement stmt=null;
	ResultSet rs=null;

	try
	{
	  if(conn==null)conn=openConnection(local_props);
	  stmt = conn.createStatement();

          String cmd = new String(
	  		 "select profile_sequence from profile where "+
	   		" version "+(version==null ? "is null":"='"+XML.escape(version) + "'")+" and " +
           		" Profile_Id = '"+XML.escape(profId==null? "":profId)+"'"); 

	  String profSeq=null;

	  rs = stmt.executeQuery(cmd);

	  while (rs.next())
            profSeq = rs.getString(1);

          if (profSeq == null)
		return(false);

	  cmd = "delete from element_value where profile_sequence = "+profSeq;
	  stmt.executeUpdate(cmd);

	  cmd = "delete from element where profile_sequence = "+profSeq;
	  stmt.executeUpdate(cmd);

	  cmd = "delete from profile where profile_sequence = "+profSeq;
	  stmt.executeUpdate(cmd);

	  return(true);
	}
	catch (SQLException e) {
	    try {
		conn.rollback();
	    } catch (SQLException se) {
		throw new ProfileSQLException(se);
	    }
		throw new ProfileSQLException(e);
	}
	finally {
            try {
                rs.close();
                stmt.close();
            } catch (SQLException se) {
                throw new ProfileSQLException(se);
            }
        }
    }

    public boolean remove(Connection conn, String profId) throws ProfileException
    {
	String version=null;
  	int cnt =0;
	String version_list[];
	Statement stmt=null;
	ResultSet rs=null;

	try
	{
	  if(conn==null)conn=openConnection(local_props);
	  stmt = conn.createStatement();
          String cmd = new String(
	  		 "select count(*) from profile where "+
           		" Profile_Id = '"+ XML.escape(profId==null? "":profId) +"'"); 
	  int version_cnt = 0;

	  rs = stmt.executeQuery(cmd);

	  while (rs.next())
            version_cnt = rs.getInt(1);

	  version_list = new String[version_cnt];

          cmd = new String(
	  		 "select version from profile where "+
           		" Profile_Id = '"+ XML.escape(profId==null? "":profId)+"'"); 
	  
	  rs = stmt.executeQuery(cmd);

	  while (rs.next())
	  {
	    version_list[cnt] = rs.getString(1); 
            version = rs.getString(1);
            cnt++;
	  }

	  for (int i = 0; i < cnt; i++)
	  {
	    boolean status = remove(conn, profId, version_list[i]);
	  }
          if (cnt == 0)
		return(false);

	  return(true);
	}
	catch (SQLException e)
	{
		try {
			conn.rollback();
		} catch (SQLException se) {
			throw new ProfileSQLException(se);
		}
		throw new ProfileSQLException(e);
	}
	finally {
            try {
                rs.close();
                stmt.close();
            } catch (SQLException se) {
                throw new ProfileSQLException(se);
            }
        }
    }


 
    /**********************************************************************
    **
    ** replace
    **
    **  Replace a profile in the Oracle db server
    **********************************************************************/
    public void replace(Connection conn, Profile profile) throws ProfileException
    {
	Statement stmt=null;
	ResultSet rs=null;
	try
	{
	  if(conn==null) conn= openConnection(local_props);
          stmt = conn.createStatement();
	  ProfileAttributes profAttr = profile.getProfileAttributes();
	  String version = profAttr.getVersion();
	  String id = profAttr.getID();

//	  ProfileValidate pv = new ProfileValidate();
//	  String errMess ="";
//          if(!(errMess=pv.validate(profile)).equals(""))
//		throw new ProfileException("\nError occured when profile (profileId="
//			+ id + ", version="+version+") is replaced."+errMess);

	  /*
	  ** Check and see if profile is already in the db
          */

          String cmd;
	  if (version != null && !version.equals(""))
          	cmd = new String(
	  		 "select count(*) from profile where "+
	   		" version = '"+XML.escape(version)+"' and " +
           		" Profile_Id = '"+XML.escape(id==null? "":id)+"'"); 
	  else
          	cmd = new String(
	  		 "select count(*) from profile where "+
	    		 " Profile_Id = '"+XML.escape(id==null? "":id)+"'"); 
	  int cnt = 0;
	  rs = stmt.executeQuery(cmd);
	  while (rs.next()) cnt = rs.getInt(1);

          if (cnt > 0)
	  {
     		/*
		** Remove the profile
		*/
		 if (version != null)
			remove(conn, id, version);
		 else
			remove(conn, id);
	  }

      	  int profSeq = getProfileSequence(conn);
       	  loadProfile(conn, profile, profSeq);
          loadProfileAttributes(conn, profile,profSeq);
          loadResourceAttributes(conn, profile,profSeq);
          loadProfileElements(conn, profile, profSeq);
	  conn.commit();
	}
        catch (Exception e)
        {
	    try {
		conn.rollback();
	    } catch (SQLException se) {
		throw new ProfileSQLException(se);
	    }	
 	    throw new ProfileException(e.getMessage());
        }
	finally {
	    try {
		rs.close();
		stmt.close();
	    } catch (SQLException se) {
                throw new ProfileSQLException(se);
            }
	}
    }

    /** Dump profiles in the database.
     * 
     * This returns a list containing the contents of the profiles.
     * 
     * @return The profiles in the database, as a list.
     * @throws ProfileException If an error occurs.
     */
    public List getProfiles(Connection conn) throws ProfileException
    {
	Statement stmt=null;
        ResultSet rs=null;

        try
        {
       		if(conn==null) conn= openConnection(local_props);
          	stmt = conn.createStatement();
		String cmd;
		
		// get all profile sequences from table PROFILE
		cmd = "select profile_id, version from profile";
		rs = stmt.executeQuery(cmd);

		// get all profiles from database
		cmd = "";
int count=0;
	        while (rs.next())
		{
System.err.println(count++);
			String profId = rs.getString("profile_id");
			String version = rs.getString("version"); 
			if(profId==null || profId.equals(""))
			{
System.err.println("********* got one profile with profId=null or empty");
				continue;
			}
			else
			{
				if(!cmd.equals("")) cmd += " or ";
				cmd += " profId ='%"+profId+"%'";
				if(version !=null && !version.equals(""))
					cmd += " and profVersion='%"+version+"%'";
			}
		}

		XMLQuery q = 
			new XMLQuery(/*keywordQuery*/"("+ cmd +") AND RETURN=FILE_SPECIFICATION_NAME",
                              	     /*id*/"EDA_XML_QUERY_V0.1",
                        	     /*title*/"EDA_XML_QUERY - Bean Query", 
				     /*desc*/"This query can be handled by the EDA System",
                        	     /*ddId*/null, /*resultModeId*/"profileFull", 
				     /*propType*/"BROADCAST",/*propLevels*/"N/A",
                        	     1000);

		return findProfiles(q);	
	}
	catch(Exception e){
		throw new ProfileException(e.getMessage());
	}
    }

    /**********************************************************************
    **
    ** clear
    **
    **  Removes all profiles from the database
    **********************************************************************/
    public void clear(Connection conn) throws ProfileException
    {
	Statement stmt = null;
	ResultSet rs=null;
	try
	{
	  String version[];
	  String id[];
	  if(conn==null) conn = openConnection(local_props);
	  stmt = conn.createStatement();

	  /*
	  ** Get all profile ids and then remove
          */

          String cmd;

          cmd = new String(
	 	 "select profile_id, version from profile ");

	  rs = stmt.executeQuery(cmd);
	  
	  int profile_count = 0;
	  if(rs != null)
		while (rs.next())
                        profile_count = rs.getInt(1);

	  id = new String[profile_count];
          version = new String[profile_count];
	  
	  rs = stmt.executeQuery(cmd);
	  int cnt = 0;

	  while (rs.next())
	  {
            id[cnt] = rs.getString(1);
            version[cnt] = rs.getString(2);
	    cnt++; 
          }

	  for (int i = 0; i < cnt; i++)
		remove(conn, id[i], version[i]);

	  conn.commit();
	}
        catch (Exception e)
        {
	   try {
		conn.rollback();
	   } catch (SQLException se) {
	   	throw new ProfileSQLException(se);
	   }

	   throw new ProfileException(e.getMessage());
        }
	 finally {
            try {
                stmt.close();
                rs.close();  
            } catch (SQLException se) {
                throw new ProfileSQLException(se);
            }
        }
    }


    /**********************************************************************
    **
    ** install
    **
    **  Install the default data model
    **********************************************************************/
    public void install() throws ProfileException
    {
       String command = null;
       try
       {
       	  java.lang.Runtime r = java.lang.Runtime.getRuntime();
       	  command = local_props.getProperty("oracle_install");
          if (command == null)
             throw new ProfileException("oracle_install property not defined.");

          Process p = r.exec(command);
          int ret = p.waitFor();
          if (p.exitValue() != 0) 
             throw new ProfileException("Installation Failed for command "+command);
      }
      catch (Exception e)
      {
             throw new ProfileException("Installation Failed for command "+command);
      }
   }  

	public static void main(String[] argv) throws Throwable {
		OracleProfileImpl opi = new OracleProfileImpl();
		List profiles = opi.getProfiles(/*connection*/null);
		System.out.println("<?xml version='1.0' encoding='UTF-8'?>\n<profiles>");
		for (Iterator i = profiles.iterator(); i.hasNext();)
			System.out.println(i.next());
		System.out.println("</profiles>");
	}
		
}
