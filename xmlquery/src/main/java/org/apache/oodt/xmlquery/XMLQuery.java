/*
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


/*
	EDA XML Query Class
 */

package org.apache.oodt.xmlquery;

import org.apache.oodt.commons.util.EnterpriseEntityResolver;
import org.apache.oodt.commons.util.XML;
import org.apache.oodt.product.Retriever;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * EDA XML query class. Parses a DIS style keyword query and creates query 
 * SELECT, FROM, and WHERE clauses. An XML DOM structure and an XML document
 * representing the query can also be created. A RESULT set can be added to 
 * and retrieve. 
 *
 */
public class XMLQuery implements java.io.Serializable, Cloneable {
	/** Result mode. */
	private String resultModeId;

	/** Propogation type. */
	private String propogationType;

	/** # of levels to propogate, depending on propogation type. */
	private String propogationLevels;

	/** Max results allowed. */
	private int maxResults = DEFAULT_MAX_RESULTS;

	/** KWQ, as a string. */
	private String kwqString;

	/** List of acceptable MIME types. */
	private List mimeAccept;

	/** List of statistics. */
        private List statistics = new ArrayList();

	/** Number of results so far.
	 *
	 * This could be greater than <code>results.size()</code> because that's limited by maxResults.
	 *
	 * Meh, this isn't even updated by anyone as far as I can tell.
	 */
	protected int numResults;

	private QueryHeader queryHeader;
	private List selectElementSet = new ArrayList();
	private List fromElementSet = new ArrayList();
	private List whereElementSet = new ArrayList();
	private QueryResult result = new QueryResult();

	private transient StreamTokenizer tokens; // Used for parse
	private transient String lpart;           // parser left part
	private transient String opart;           // parser operator
	private transient String rpart;           // parser right part
	private transient String previous_token;  // parser - allows backout
	private transient int qfsc = 0;           // query from set item count
	private transient int qwsc = 0;           // query where set item count
	private transient int lit = 0;            // last query item type ("from" or "where" clause)
	public static final String[] FROM_TOKENS = {};

	/** Constructor.
	 *
	 * @param keywordQuery  The DIS style keyword query string.
	 * @param id A query identifier.
	 * @param title A terse description of the query for display.
	 * @param desc A description of the query.
	 * @param ddId The data dictionary identifier.    
	 * @param resultModeId Indicates the return of INSTANCE, PROFILE, or CLASS.
	 * @param propType Indicates query BROADCAST or PROPOGATE.
	 * @param propLevels Number of propogation levels.
	 * @param maxResults Maximum number of results to be returned.
	 */
	public XMLQuery(String keywordQuery, String id, String title, String desc, String ddId, String resultModeId,
		String propType, String propLevels, int maxResults) {
		this(keywordQuery, id, title, desc, ddId, resultModeId, propType, propLevels, maxResults, null, true);
	}

	/** Constructor.
	 *
	 * @param keywordQuery  The DIS style keyword query string.
	 * @param id A query identifier.
	 * @param title A terse description of the query for display.
	 * @param desc A description of the query.
	 * @param ddId The data dictionary identifier.    
	 * @param resultModeId Indicates the return of INSTANCE, PROFILE, or CLASS.
	 * @param propType Indicates query BROADCAST or PROPOGATE.
	 * @param propLevels Number of propogation levels.
	 * @param maxResults Maximum number of results to be returned.
	 * @param parseQuery Indicates whether query should be parsed
	 */
	public XMLQuery(String keywordQuery, String id, String title, String desc, String ddId, String resultModeId,
		String propType, String propLevels, int maxResults, boolean parseQuery) {
		this(keywordQuery, id, title, desc, ddId, resultModeId, propType, propLevels, maxResults, null, parseQuery);
	}

	/** Constructor.
	 *
	 * @param keywordQuery  The DIS style keyword query string.
	 * @param id A query identifier.
	 * @param title A terse description of the query for display.
	 * @param desc A description of the query.
	 * @param ddId The data dictionary identifier.    
	 * @param resultModeId Indicates the return of INSTANCE, PROFILE, or CLASS.
	 * @param propType Indicates query BROADCAST or PROPOGATE.
	 * @param propLevels Number of propogation levels.
	 * @param maxResults Maximum number of results to be returned.
	 * @param mimeAccept List of acceptable MIME types.
	 */
	public XMLQuery(String keywordQuery, String id, String title, String desc, String ddId, String resultModeId,
		String propType, String propLevels, int maxResults, List mimeAccept) {
		this(keywordQuery, id, title, desc, ddId, resultModeId, propType, propLevels, maxResults, mimeAccept, true);
	}
                        
	/** Constructor.
	 *
	 * @param keywordQuery  The DIS style keyword query string.
	 * @param id A query identifier.
	 * @param title A terse description of the query for display.
	 * @param desc A description of the query.
	 * @param ddId The data dictionary identifier.    
	 * @param resultModeId Indicates the return of INSTANCE, PROFILE, or CLASS.
	 * @param propType Indicates query BROADCAST or PROPOGATE.
	 * @param propLevels Number of propogation levels.
	 * @param maxResults Maximum number of results to be returned.
	 * @param mimeAccept List of acceptable MIME types.
	 * @param parseQuery Indicates whether query should be parsed
	 */
	public XMLQuery(String keywordQuery, String id, String title, String desc, String ddId, String resultModeId,
		String propType, String propLevels, int maxResults, 
		List mimeAccept, boolean parseQuery) {
		if (mimeAccept == null) {
			mimeAccept = new ArrayList();
			mimeAccept.add("*/*");
		}
		if (keywordQuery == null) {
		  keywordQuery = "UNKNOWN";
		}
		// init query header (object attributes)
		if (id == null) {
		  id = "UNKNOWN";
		}
		if (title == null) {
		  title = "UNKNOWN";
		}
		if (desc == null) {
		  desc = "UNKNOWN";
		}
		if (ddId == null) {
		  ddId = "UNKNOWN";
		}
		queryHeader = new QueryHeader(id, title, desc, /*type*/"QUERY", /*status*/"ACTIVE", /*security*/"UNKNOWN",
			/*revision*/"1999-12-12 JSH V1.0 Under Development", ddId);

		// init query attributes
		if (resultModeId == null) {
		  resultModeId = "ATTRIBUTE";
		}
		if (propType == null) {
			propType = "BROADCAST";
			propLevels = "N/A";
		}
		this.resultModeId = resultModeId;
		this.propogationType = propType;
		this.propogationLevels = propLevels;
		this.maxResults = maxResults;
		this.kwqString = keywordQuery;
		this.mimeAccept = mimeAccept;
        
		// parse the keyword query string
		if (! parseQuery) {
			queryHeader.setStatusID("NOTPARSED");
		} else if (!parseKeywordString(keywordQuery)) {
			queryHeader.setStatusID("ERROR");
		}
	}

	/** Get the list of acceptable MIME types.
	 *
	 * @return The list of acceptable MIME types; you may modify the list.
	 */
	public List getMimeAccept() {
		return mimeAccept;
	}

    /**
     * Instantiates an XMLQuery instance from an XML query structure in string format.
     *
     * @param xmlQueryString  The XML query structure in string format.
     */
	public XMLQuery (String xmlQueryString) throws SAXException {
		if (xmlQueryString == null) {
		  xmlQueryString = BLANK;
		}
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setCoalescing(false);
			factory.setNamespaceAware(false);
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(ENTITY_RESOLVER);
			Document doc = builder.parse(new ByteArrayInputStream(xmlQueryString.getBytes()));
			loadXMLQueryStruct(doc.getDocumentElement());
		} catch (IOException ex) {
			System.err.println("Unexpected no good bloody IOException while parsing doc: " + xmlQueryString);
			throw new IllegalStateException("Unexpected no good bloody IOException: " + ex.getMessage() + ", during"
				+ " parse of " + xmlQueryString);
		} catch (ParserConfigurationException ex) {
			throw new IllegalStateException("Unexpected ParserConfigurationException: " + ex.getMessage());
		}
   	}

    /**
     * Instantiates an XMLQuery instance from an XML query structure in DOM node format.
     *
     * @param node  The XML &lt;query&gt; node.
     */
	public XMLQuery (Node node) {
		loadXMLQueryStruct(node);
   	}

    /**
     * Gets the original DIS style keyword query string.
     *
     * @return The DIS style keyword query string.
     */
	public String getKwdQueryString () {
		return kwqString;
	}

    /**
     * Gets the max # of results
     *
     * @return the results
     */
	public int getMaxResults() {
		return maxResults;
	}

    /**
     * @return Gets the select elements
     *
     */
	public List getSelectElementSet() {
	   return(selectElementSet);
	}

    /**
     * @return Gets the from element set from the query
     *
     */
	public List getFromElementSet()
	{
	   return(fromElementSet);
	}


    /**
     * @return Gets the resultModeID
     *
     */
	public String getResultModeID()
	{
           return(resultModeId);
        }
	
    /**
     * @return Gets the where set
     *
     */
	public List getWhereElementSet() {
	   return(whereElementSet);
	}
	
    /**
     * Set query where element set.
     * @param whereElementSet Thw where element set of XMLQuery.
     */
	public void setWhereElementSet(List whereElementSet) {
		this.whereElementSet = whereElementSet;
	}
    /**
     * @return Gets the result list
     *
     */
	public QueryResult getResult() {
	    return result;
	}

	public List getResults() {
		return result.getList();
	}
	
	public void setRetriever(Retriever retriever) {
		result.setRetriever(retriever);
	}

    /**
     * @return The query as an XML DOM structure.
     */
	public Document getXMLDoc () {
		Document doc = createDocument();
		doc.appendChild(doc.createElement("query"));
   		createXMLDoc(doc);
		return doc;
    }

    /** Get the list of stasitics of this query.
    *
    * @return A list of {@link Statistic} objects.
    */
    public List getStatistics() {
      return statistics;
    }

    /**
     * Gets query as an XML document in string format.
     *
     * @return The query as an XML document in string format.
     */
	public String getXMLDocString () {
		StringWriter writer = new StringWriter();
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "text/xml");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
			transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
			transformer.transform(new DOMSource(getXMLDoc()), new StreamResult(writer));
		} catch (TransformerException ex) {
			throw new IllegalStateException("Unexpected TransformerException: " + ex.getMessage());
		} finally {
			try {
				writer.close();
			} catch (IOException ignore) {}
		}
		return writer.toString();
	}

	/**
	 * Get the propagation type.
	 *
	 * @return a {@link String} value.
	 */
	public String getPropagationType() {
		return propogationType;
	}

	/**
	 * Get the propagation levels.
	 *
	 * @return a {@link String} value.
	 */
	public String getPropagationLevels() {
		return propogationLevels;
	}

    /**
     * Parse keyword query string (simple top down recursion).
     */
    private boolean parseKeywordString (String kwdQueryString) {
	    previous_token = "";
	    lpart = "";
	    rpart = "";

            tokens = new java.io.StreamTokenizer(new StringReader(kwdQueryString));
            tokens.resetSyntax();
            tokens.ordinaryChar('/');
            tokens.wordChars('#', '#');
            tokens.wordChars(':', ':');
            tokens.wordChars('0', '9');
            tokens.wordChars('.', '.');
            tokens.wordChars('-', '-');
            tokens.wordChars('[', '[');
            tokens.wordChars(']', ']');
            tokens.wordChars('a', 'z');
            tokens.wordChars('A', 'Z');
            tokens.wordChars('_', '_');
            tokens.wordChars('/', '/');
            tokens.eolIsSignificant(true);
            tokens.whitespaceChars(0, ' ');
            tokens.quoteChar('"');
            tokens.quoteChar('\'');

	  return kqOrParse();
	}

    /**
     * Parse OR logical operator.
     */
	private boolean kqOrParse ()
	{
	    boolean lflag, rflag;

	    lflag = kqAndParse();
	    while (isTokenEqual("OR") | isTokenEqual("or") | isTokenEqual("|")) {
            rflag = kqAndParse();
            lflag &= rflag;
            appendLogOperator("LOGOP", "OR");
        }
        return lflag;
	}

    /**
     * Parse AND logical operator.
     */
   	private boolean kqAndParse ()
	{
	    boolean lflag, rflag;

	    lflag = kqNotParse();
	    while (isTokenEqual("AND") | isTokenEqual("and") | isTokenEqual("&")) {
            rflag = kqNotParse();
            lflag &= rflag;
            appendLogOperator("LOGOP", "AND");
        }
        return lflag;
	}

    /**
     * Parse NOT logical operator.
     */
	private boolean kqNotParse ()
	{
	    boolean lflag, rflag;

	    if (isTokenEqual("NOT") | isTokenEqual("not") | isTokenEqual("!")) {
            lflag = kqFactorParse();
            appendLogOperator("LOGOP", "NOT");
        } else {
            lflag = kqFactorParse ();
        }
        return lflag;
	}

    /**
     * Parse logical operator operand and handle nesting (parens)
     * also logic for differentiating SELECT, WHERE, and FROM
     */
	private boolean kqFactorParse ()
	{
	    String ropn, enm;
	    if (isTokenEqual("(")) {
	        return (kqOrParse () & isTokenEqual(")"));
	    } else {
            lpart = getNextToken ();
            if (lpart.compareTo("") != 0) {
                 ropn = getNextToken ();
                 if (ropn.compareTo("EQ") == 0 || ropn.compareTo("LT") == 0 || ropn.compareTo("LE") == 0 || ropn.compareTo("GT") == 0 || ropn.compareTo("GE")== 0 || ropn.compareTo("NE") == 0 || ropn.compareToIgnoreCase("LIKE") == 0 || ropn.compareToIgnoreCase("NOTLIKE") == 0 || ropn.compareTo("IS") == 0 || ropn.compareTo("ISNOT") == 0) {
                    rpart = getNextToken ();
                    if (rpart.compareTo("") != 0) {
                        lit = 0;
                        enm = lpart;
                        if (enm.compareTo("RETURN") == 0) {
			    selectElementSet.add(new QueryElement("elemName", rpart));
                        } else if (isFromToken(lpart)) {
                            qfsc++;
                            lit = 1;
			    fromElementSet.add(new QueryElement("elemName", lpart));
			    fromElementSet.add(new QueryElement("LITERAL", rpart));
			    fromElementSet.add(new QueryElement("RELOP", ropn));
                        } else {
                            lit = 2;
                            qwsc++;
			    whereElementSet.add(new QueryElement("elemName", lpart));
			    whereElementSet.add(new QueryElement("LITERAL", rpart));
			    whereElementSet.add(new QueryElement("RELOP", ropn));
                        }
                        return true;
                    }
                }
            }
        }
        return false;
	}

    /**
     * Create Logical Operator DOM node for WHERE clause.
     */
    private boolean appendLogOperator (String tt, String ts)
	{
	    if (lit == 1 & qfsc > 1) {
		    fromElementSet.add(new QueryElement(tt, ts));

    	} else if (lit == 2 & qwsc > 1) {
		whereElementSet.add(new QueryElement(tt, ts));
    	}
	    return true;
	}

    /**
     * Lexical analyzer - find a FROM token.
     */
    private static boolean isFromToken (String s1)
	{
	  for (String FROM_TOKEN : FROM_TOKENS) {
		if (s1.compareTo(FROM_TOKEN) == 0) {
		  return true;
		}
	  }
	    return false;
	}

    /**
     * Lexical analyzer - return next token - allows lookahead.
     */
    private String getNextToken ()
	{
	    String ts;

    	    ts = previous_token;
            if (previous_token.compareTo("") == 0) {
                    ts = getNextTokenFromStream();
        	    return ts;
            } else {
        	    previous_token = "";
        	    return ts;
            }
	}

    /**
     * Lexical analyzer - check if next token equal to argument.
     */
    private boolean isTokenEqual (String s1) {
	    String ts;
	    boolean rc;

	  if (previous_token.compareTo("") == 0) {
       	        ts = getNextTokenFromStream ();
		if (ts.compareTo("") == 0) {
                    rc = false;
                } else {
           	    if (ts.compareTo(s1) == 0) {
     	                previous_token = "";
       	                rc = true;
       	            } else {
       	                previous_token = ts;
       	                rc = false;
                    }
       	        }
      	    } else {
           	    if (previous_token.compareTo(s1) == 0) {
       	            previous_token = "";
       	            rc = true;
       	        } else {
       	            rc = false;
       	        }
     	    }
	    return rc;
	}  

    /**
     * Lexical analyzer - return next token from instance of StreamTokenizer.
     */
   private String getNextTokenFromStream () {
            int c, c2;
	    String rc;
            try {
                switch (c=tokens.nextToken()) {
                    case StreamTokenizer.TT_EOF:
                        rc = "";
			break;
                    case StreamTokenizer.TT_EOL:
                        rc = "";
			break;
                    case StreamTokenizer.TT_NUMBER: // not currently set
			rc = "";
			break;
                    case StreamTokenizer.TT_WORD:
                        rc = tokens.sval;
			break;
                    case '(':
                    case ')':
			rc = String.valueOf((char)c);
			break;
                    case '"':
                    case '\'':
                        rc = tokens.sval;
			break;
                    case '=':
                        rc = "EQ";
			break;
                    case '&':
                        rc = "AND";
			break;
                    case '|':
                        rc = "OR";
			break;
                    case '<':
                        c2 = tokens.nextToken();
                        if (c2 == '=') {
				rc = "LE";
                        } else {
				tokens.pushBack();
				rc = "LT";
                        }
			break;
                    case '>':
                        c2 = tokens.nextToken();
                        if (c2 == '=') {
				rc = "GE";
                        } else {
                                tokens.pushBack();
				rc = "GT";
                        }
			break;
                    case '!':
                        c2 = tokens.nextToken();
                        if (c2 == '=') {
				rc = "NE";
                        } else {
                                tokens.pushBack();
				rc = "NOT";
                        }
			break;
                    default:
                        rc = "";
                    }
                } catch (IOException e1) {
			rc = "";
		}
	    return rc;
   }

    /**
     * Replace character with string
     */
    private String replaceCharWithString (String s1, char c, String rs)
	{
        int p1, p2, s1l;
    	StringBuilder s2;
    	
    	p1 = 0;
	  s1l = s1.length();
    	s2 = new StringBuilder();
    	p2 = s1.indexOf(c, p1);
    	while (p2 >= 0) {
    	    s2.append(s1.substring(p1, p2)).append(rs);
            p1 = p2 + 1;
            p2 = s1.indexOf(c, p1);
        }
        if (p1 < s1l) {
            s2.append(s1.substring(p1, s1l));
        }
        return s2.toString();
	}       
       
    /**
     * Creates a XML DOM structure from a keyword query string.
     * Set up the basic structure then calls the query parser.
     */
	private void createXMLDoc (Document doc) {
	    int loc;
	    String kwd;
	    String type;
	    String val;
	    Element item;
	    Element elem;
	    
	    Element query = doc.getDocumentElement();
   	        
        // create query header (object attributes)
	    query.appendChild(queryHeader.toXML(doc));
	        
	    // create query attributes
	    XML.add(query, "queryResultModeId", resultModeId);
	    XML.add(query, "queryPropogationType", propogationType);
	    XML.add(query, "queryPropogationLevels", propogationLevels);
	    XML.add(query, "queryMimeAccept", mimeAccept);
	    XML.add(query, "queryMaxResults", String.valueOf(maxResults));
	    XML.add(query, "queryResults", String.valueOf(numResults));
	    XML.add(query, "queryKWQString", kwqString);

	    // create and load queryStatistics
            elem = doc.createElement("queryStatistics");
            query.appendChild(elem);
	  for (Object statistic : statistics) {
		Statistic s = (Statistic) statistic;
		elem.appendChild(s.toXML(doc));
	  }

	    // create and load querySelectSet
	    elem = doc.createElement("querySelectSet");
	    query.appendChild(elem);

	  for (Object aSelectElementSet : selectElementSet) {
		QueryElement queryElement = (QueryElement) aSelectElementSet;
		elem.appendChild(queryElement.toXML(doc));
	  }

	    // create and load queryFromSet
	    elem = doc.createElement("queryFromSet");
	    query.appendChild(elem);

	  for (Object aFromElementSet : fromElementSet) {
		QueryElement queryElement = (QueryElement) aFromElementSet;
		elem.appendChild(queryElement.toXML(doc));
	  }

	    // create and load queryWhereSet
	    elem = doc.createElement("queryWhereSet");
	    query.appendChild(elem);
	  for (Object aWhereElementSet : whereElementSet) {
		QueryElement queryElement = (QueryElement) aWhereElementSet;
		elem.appendChild(queryElement.toXML(doc));
	  }

	    query.appendChild(result.toXML(doc));
    }

    /**
     * Creates a leaf (end node - TEXT) in the DOM structure.
     */
    private Element createLeaf (Element child, String value) {
        child.appendChild(child.getOwnerDocument().createTextNode(value));
        return child;
    }

    /**
     * Load the internal query structure (dictionary) from an XML DOM structure
     */
    private void loadXMLQueryStruct (Node root) {
        Node node;
        String nodeName;
        
        initNodes();
        for (node = root.getFirstChild();
            node != null;
            node = node.getNextSibling()) {
                if (node instanceof Element) {
                    nodeName = node.getNodeName();
                    if (nodeName.compareTo("queryAttributes") == 0) {
		        queryHeader = new QueryHeader(node);
                    } else if (nodeName.compareTo("queryResultModeId") == 0) {
			    resultModeId = XML.unwrappedText(node);
		    } else if (nodeName.compareTo("queryPropogationType") == 0) {
			    propogationType = XML.unwrappedText(node);
		    } else if (nodeName.compareTo("queryPropogationLevels") == 0) {
			    propogationLevels = XML.unwrappedText(node);
		    } else if (nodeName.equals("queryMimeAccept")) {
			    mimeAccept.add(XML.unwrappedText(node));
		    } else if (nodeName.compareTo("queryMaxResults") == 0) {
			    maxResults = Integer.parseInt(XML.unwrappedText(node));
		    } else if (nodeName.compareTo("queryResults") == 0) {
			    numResults = Integer.parseInt(XML.unwrappedText(node));
		    } else if (nodeName.compareTo("queryKWQString") == 0) {
			    kwqString = XML.unwrappedText(node);
                    } else if (nodeName.compareTo("querySelectSet") == 0) {
			scanQueryElements(selectElementSet, node);
                    } else if (nodeName.compareTo("queryFromSet") == 0) {
			scanQueryElements(fromElementSet, node);
                    } else if (nodeName.compareTo("queryWhereSet") == 0) {
			scanQueryElements(whereElementSet, node);
                    } else if (nodeName.compareTo("queryResultSet") == 0) {
			    result = new QueryResult(node);
                    } else if (nodeName.equals("queryStatistics")) {
                        NodeList children = node.getChildNodes();
                        for (int i = 0; i < children.getLength(); ++i) {
				Node statisticNode = children.item(i);
				if (statisticNode.getNodeType() == Node.ELEMENT_NODE) {
					Statistic s = new Statistic(statisticNode);
					statistics.add(s);
				}
			}
                    }
                }
            }
	}

    /**
     * Replace the dictionary keyword value with the DOM text node value.
     */
    private void replaceKwdVal (Map map, Node node, String nodeName) {
        Node childNode;
        
        childNode = node.getFirstChild();
        if (childNode instanceof Text) {
                map.put(nodeName, childNode.getNodeValue());
        } else {
                map.put(nodeName, "UNKNOWN");            
        }
	}

    /**
     * Scan the DOM structure for the SELECT, FROM, or WHERE set.
     *
     * @param list To what list to add the query elements.
     * @param node Where to find the query elements. 
     */
    private static void scanQueryElements(List list, Node node) {
	    NodeList children = node.getChildNodes();
	    for (int i = 0; i < children.getLength(); ++i) {
		    Node n = children.item(i);
		    if (n.getNodeType() == Node.ELEMENT_NODE) {
			  list.add(new QueryElement(n));
			}
	    }
    }

    /**
     * Initialize the Query and Resource attributes in the dictionary.
     */
    private void initNodes () {
	queryHeader = new QueryHeader();
	resultModeId = "ATTRIBUTE";
	propogationType = "BROADCAST";
	propogationLevels = "N/A";
	maxResults = DEFAULT_MAX_RESULTS;
	kwqString = "UNKNOWN";
	mimeAccept = new ArrayList();
    }    

	public int hashCode() {
		return resultModeId.hashCode() ^ propogationType.hashCode() ^ propogationLevels.hashCode()
			^ (maxResults << 8) ^ kwqString.hashCode() ^ numResults ^ queryHeader.hashCode()
			^ selectElementSet.hashCode() ^ fromElementSet.hashCode() ^ whereElementSet.hashCode()
			^ result.hashCode();
	}

	public boolean equals(Object rhs) {
		if (rhs == this) {
		  return true;
		}
		if (rhs == null || !(rhs instanceof XMLQuery)) {
		  return false;
		}
		XMLQuery obj = (XMLQuery) rhs;
		return resultModeId.equals(obj.resultModeId) && propogationType.equals(obj.propogationType)
			&& propogationLevels.equals(obj.propogationLevels) && maxResults == obj.maxResults
			&& kwqString.equals(obj.kwqString) && numResults == obj.numResults
			&& queryHeader.equals(obj.queryHeader) && selectElementSet.equals(obj.selectElementSet)
			&& fromElementSet.equals(obj.fromElementSet) && whereElementSet.equals(obj.whereElementSet)
			&& result.equals(obj.result);
	}

	public Object clone() {
		Object rc = null;
		try {
			rc = super.clone();
		} catch (CloneNotSupportedException ignored) {}
		return rc;
	}

	public String toString() {
		return getClass().getName() + "[kwqString=" + kwqString + "]";
	}

	/** Create an XML DOM document using the query DTD.
	 *
	 * @return An XML DOM document, with a query root element, using the query DTD.
	 */
	public static Document createDocument() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setCoalescing(true);
			factory.setNamespaceAware(false);
			factory.setValidating(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.newDocument();
		} catch (ParserConfigurationException ex) {
			throw new IllegalStateException("Unexpected ParserConfigurationException: " + ex.getMessage());
		}
	}

	public static void main(String[] argv) throws Exception {
		if (argv.length < 2 || (!argv[0].equals("-expr") && !argv[0].equals("-file"))
			|| (argv[0].equals("-file") && argv.length > 2)) {
			System.err.println("Usage: -expr <expr>...");
			System.err.println("   or: -file <file>");
			System.exit(1);
		}

		XMLQuery q;
		if (argv[0].equals("-expr")) {
			StringBuilder expr = new StringBuilder();
			for (int i = 1; i < argv.length; ++i) {
			  expr.append(argv[i]).append(' ');
			}
			q = new XMLQuery(expr.toString().trim(), "expr1", "Command-line Expression Query",
				"The expression for this query came from the command-line", /*ddId*/ null,
				/*resultModeId*/ null, /*propType*/ null, /*propLevels*/ null, XMLQuery.DEFAULT_MAX_RESULTS);
		} else if (argv[0].equals("-file")) {
			BufferedReader reader = new BufferedReader(new FileReader(argv[1]));
			StringBuilder str = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
			  str.append(line).append('\n');
			}
			reader.close();
			q = new XMLQuery(str.toString());
		} else {
		  throw new IllegalStateException("Can't get here; only -expr or -file allowed, but got \"" + argv[0] + "\"");
		}
				
		System.out.println("kwdQueryString: " + q.getKwdQueryString());
		System.out.println("fromElementSet: " + q.getFromElementSet());
		System.out.println("results: " + q.getResult());
		System.out.println("whereElementSet: " + q.getWhereElementSet());
		System.out.println("selectElementSet: " + q.getSelectElementSet());

		System.out.println("======doc string=======");
		System.out.println(q.getXMLDocString());
	}

	public QueryHeader getQueryHeader() {
		return queryHeader;
	}

	/** Maximum number results to get (default). */
	public static final int DEFAULT_MAX_RESULTS = 100;

	/** The Formal Public Identifier of the query DTD. */
	public static final String QUERY_FPI = "-//JPL//DTD OODT Query 1.0//EN";
	
	/** The System Identifier of the query DTD. */
	public static final String QUERY_URL = "http://oodt.jpl.nasa.gov/edm-query/query.dtd"; // FIXME: Move to apache.org

	/** A blank query document, as a string. */
	private static String BLANK = "<?xml version='1.0' encoding='UTF-8'?>\n<!DOCTYPE query PUBLIC '" + QUERY_FPI
		+ "' '" + QUERY_URL + "'>\n<query/>";

	/** Resolver for the query DTD. */
	private static final EntityResolver ENTITY_RESOLVER = new EnterpriseEntityResolver();

	/** Serial version unique ID. */
	static final long serialVersionUID = -7638068782048963710L;
}
