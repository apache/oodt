/**********************************************************************************
**
** @name: HTTPBasedProfileHandler.java
**
** @author: Shu Liu
**
** @date: 06/11/2001 
**
** @description: Implement profile service for extracting information from document
** 		 systems. 
**
***********************************************************************************/

package jpl.eda.profile.handlers;

import java.io.*;
import java.util.*;
import java.net.*;
import org.w3c.dom.*;
import jpl.eda.profile.*;
import jpl.eda.util.*;
import jpl.eda.xmlquery.*;

public abstract class HTTPBasedProfileHandler implements ProfileHandler
{

    /**
     * constructors
     */
    public HTTPBasedProfileHandler(){}

    /*
     *find the profiles for the query
     */
    public abstract List findProfiles(XMLQuery xmlQuery)throws DOMException,ProfileException;


    /* send a query to url by HTTP and get method
     */
    public String search(String protocol, URLConnection connection, String query, String method,
			Map requestProperty) throws ProfileException
    {
	String line=new String();
        StringBuffer response = new StringBuffer();
	if(query == null) query = new String();

//	if(method.toUpperCase().equals("GET"))
//                if(urlString.indexOf("?") == -1)urlString=urlString+"?"+query;

	try
	{        
          	// set connection to URL
        	HttpURLConnection con;

		if(protocol.toUpperCase().equals("HTTP")) 
			con =(HttpURLConnection) connection;
		else 
		if(protocol.toUpperCase().equals("HTTPS"))
			con = (javax.net.ssl.HttpsURLConnection)connection;
		else
			throw new ProfileException("Unsupported protocol. ");

		// set request properties
		String acceptType = new String();
		String contentLength = new String();
		Set keySet = requestProperty.keySet();
		for(Iterator i=keySet.iterator(); i.hasNext();)
		{
			String key = (String) i.next();
			String property = (String) requestProperty.get(key);
			if(property != null && !property.equals(""))
			{
				con.setRequestProperty(key, property);
				if("Accept".equals(key))acceptType = property;
				if("Content_Length".equals(key)) contentLength = property;
			}
			
		}

		if(method.toUpperCase().equals("POST"))
                {
                        byte q[]=query.getBytes();
                        con.setRequestMethod("POST");
			if(contentLength.equals(""))
                        	con.setRequestProperty("Content-Length",  String.valueOf(q.length));
			else	con.setRequestProperty("Content-Length",  contentLength);
                 	con.setDoInput(true);
		        con.setDoOutput(true);

                        OutputStream out = con.getOutputStream();
                        out.write(q);
                        out.flush();
                }
		else
		{
			con.setRequestMethod("GET");
			con.connect();
		}

		// set input stream and get results from JPL rulus
        	BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        	while ((line = reader.readLine()) != null)
		{
                        if(acceptType.indexOf("xml") != -1 ) line = validateResult(line); 
                	response.append(line).append('\n');     // add the line into string buffer
		}
	}
	catch (Exception e)
	{
		throw new ProfileException(e.getMessage());
	}
        return(response.toString());
    } 
  

    /*
     * check and remove the invalid characters from the DDM search result
     */
                   
    public String validateResult(String line)
    {
        int len = line.length();
        String newLine = new String();
        String  temp = new String();  
        byte ch[] = new byte[1];
     
        for(int i=0; i<len; i++)
        {
                temp = line.substring(i,i+1);
                ch = temp.getBytes();

                // remove the invalid XML characters
                if(!((ch[0]< ' ' && ch[0] != '\t' && ch[0] != '\n' && ch[0] != '\r')
                   || ch[0] > 0x7e || ch[0] == 0xf7))
                   newLine += temp;                     // keep the valid XML character
        }
        // return the new consructed string
        return(newLine);
    }


    /*
     * get a profile with required profID
     */

    public abstract Profile get(String profID) throws ProfileException;
}











