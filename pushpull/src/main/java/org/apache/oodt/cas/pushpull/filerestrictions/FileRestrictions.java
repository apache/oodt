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


package org.apache.oodt.cas.pushpull.filerestrictions;

//OODT imports
import org.apache.oodt.cas.pushpull.protocol.ProtocolPath;

//JDK imports
import java.io.InputStream;
import java.util.LinkedList;


/**
 * This class allows the creation of restrictions for files and directories created below an actual directory which is passed
 * into the constructor.  These restriction are loaded by passing a FileInputStream which contains a XML File
 * into the {@link #loadRestrictions(InputStream)} method and can be tested against by using the {@link #isAllowed(VirtualFile)} method.
 * 
 * <pre>
 * The XML file schema is:
 *	{@literal <root>
 *	   <variables>
 *	      <variable name="variable-name">
 *	         <type>INT-or-STRING</type>
 *	         <value>variable-value</value>
 *	         <precision>
 *	            <locations>number-of-fill-locations</locations>
 *	            <fill>fill-value</fill>
 *	            <side>front-or-back</side>
 *	         </precision>
 *	      </variable>
 *	      ...
 *	      ...
 *	   </variables>
 *
 *	   <methods>
 *	      <method name="method-name">
 *	         <args>
 *	            <arg name="argument-name">
 *	               <type>INT-or-STRING</type>
 *	            </arg>
 *	            ...
 *	            ...
 *	         </args>
 *	         <action>method-behavior</action>
 *	      </method>
 *	      ...
 *	      ...
 *	   </methods>
 *   
 *	   <dirstruct name="root-directory-name">
 *	      <nodirs/>
 *	      <nofiles/>
 *	      <file name="file-name"/>
 *	      <dir name="directory-name">
 *	         <nodirs/>
 *	         <nofiles/>
 *	         <file name="file-name"/>
 *	         <dir name="directory-name">
 *	            ...
 *	            ...
 *	         </dir>
 *	      </dir>
 *	      ...
 *	      ...
 *	   </dirstruct>
 *	</root>}
 * </pre>
 *
 * <i>{@literal <variables>}</i> and <i>{@literal <methods>}</i> can be created in this XML file so that they can be used in the <i>{@literal <dirstruct>}</i>
 * portion of the XML file.  These <i>{@literal <variables>}</i> and <i>{@literal <methods>}</i> can be used inside the <i>{@literal <dir>}</i> and
 * <i>{@literal <file>}</i> elements within the <i>{@literal <dirstruct>}</i> element to allow for varrying directory and file names beyond the capability
 * of regular expressions (which are also allowed).  
 * 
 * <h2>VARIABLES (OPTIONAL):</h2>
 * Let's start with describing the <i>{@literal <variables>}</i> portion of the XML file.  As many <i>{@literal <variable>}</i> elements as you would like
 * can be specified inside the <i>{@literal <variables>}</i> tag.  The <i>{@literal <variable>}</i> element must have a parameter, 'name', which is the name
 * of this <i>{@literal <variable>}</i>.  Every <i>{@literal <variable>}</i> is a global variable (that is, global in the scope of the XML file it is declared
 * in -- it is not usable in other XML file, unless redeclared) so variable names are unique (however, are case sensitive) so thus a name can only
 * be applied to one <i>{@literal <variable>}</i>.  Within the <i>{@literal <variable>}</i> element there are three possible sub-elements that can be included.
 * <i>{@literal <type>}</i> and <i>{@literal <value>}</i> are required and <i>{@literal <precision>}</i> is optional.  <i>{@literal <type>}</i> can be either (and it must be
 * in all UPPERCASE) INT or STRING (sorry, floating point numbers are not supported as of yet).  This specifies what type of value will be given in 
 * <i>{@literal <value>}</i>.  This allows you to both use numerical values as either an integer or a string.  <i>{@literal <precision>}</i> can also be specified
 * for each <i>{@literal <variable>}</i>.  This allows you to insure that an integer or string will take up a certain amount of space.  This is especially
 * useful when dealing with dates.  For instance, say you had the following in your XML file: 
 * 
 * <pre>
 *	{@literal <variable name="myVariable">
 *	   <type>INT</type>
 *	   <value>3</value>
 *	</variable>}
 * </pre>
 *
 * When myVariable was finally returned it would look like 3, however many times for dates you would like 03 returned.  You can specify this by adding
 * precision to the following XML:
 *
 * <pre>
 *	{@literal <variable name="myVariable">
 *	   <type>INT</type>
 *	   <value>3</value>
 *	   <precision>
 *	      <locations>2</locations>
 *	      <fill>0</fill>
 *	      <side>front</side>
 *	   </precision>
 *	</variable>}
 * </pre>
 *
 * This insures that the number is always printed with 2 digits and if the number does not take up 2 digits worth of space a fill value 0 will be added
 * to the front side of the integer, thus, in this example would give us 03.  Note: <i>{@literal <value>03</value>}</i> would NOT accomplish the same!!!!
 * 
 * <h2>METHODS (OPTIONAL):</h2>
 * Next let's look at the <i>{@literal <methods>}</i> portion of the XML file.  <i>{@literal <method>}</i> elements must have a 'name' parameter, which is the name of the
 * <i>{@literal <method>}</i>.  Every <i>{@literal <method>}</i> is also global in the same way as is every <i>{@literal <variable>}</i> and are also case-sensitive, thus method 
 * names must be unique.  A <i>{@literal <method>}</i> element may contain an <i>{@literal <args>}</i> sub-element, however this is optional and only needed if the method
 * is to take any arguments.  If an <i>{@literal <args>}</i> element is given, then it should contain at least one <i>{@literal <arg>}</i> element.  A <i>{@literal <method>}</i>
 * may contain as many <i>{@literal <arg>}</i> elements as it needs.  What is being specified by a <i>{@literal <method>}</i> element is what would be known in java code 
 * as the method signature.  Thus all we are going to specify is each argument's name and type.  Thus each <i>{@literal <arg>}</i> element must contain
 * a 'name' parameter, which is the name of the argument and must contain a <i>{@literal <type>}</i> sub-element, so it is known how to treat the arguments when 
 * the method is used within the <i>{@literal <dirstruct>}</i> section of the XML file.  Another sub-element, which is required, for the <i>{@literal <method>}</i>
 * element is the <i>{@literal <action>}</i> element.  This element contains the behavior of the <i>{@literal <method>}</i>.  Before going into detail about what can be
 * placed within the <i>{@literal <action>}</i> element let's first cover some syntax requirements for the XML file.
 * 
 * <h2>SYNTAX REQUIREMENTS:</h2>
 * When a <i>{@literal <variable>}</i> is used it must be preceded by $ and inclosed in {} (e.g. ${myVariable}).<br>
 * When a <i>{@literal <method>}</i> is used it must be preceded by % and end with () (e.g. %myMethod(), however if arguments are given then %myMethod(12,9)).<br>
 * When a <i>{@literal <method>}</i> argument (<i>{@literal <arg>}</i> element) is used is must be preceded by $ (e.g. $myArg).<br>
 * When a literal integer is used it must be preceded by # (e.g. #234).<br>
 * When a literal string is used it must be inclosed in " (e.g. "my age is 56 -- no not really").<br>
 *	
 *	<p>NOTE: When passing arguments into methods the string and integer literal rules do not need to be followed because you have already defined what each
 *	   argument type should be and they will be evaluated as such.</p>
 *	<p>NOTE*: Also note that at present a <i>{@literal <variable>}</i> cannot be passed as an argument to the methods.  Just use the <i>{@literal <variable>}</i> where needed
 *	   inside the <i>{@literal <action>}</i> element.  This feature should hopefully be added in a later release.</p>
 *
 * <h2>METHOD'S ACTION ELEMENT USAGE:</h2>
 * The <i>{@literal <action>}</i> element will evaluate expressions that contain both integers and strings.  It obeys the rules of mathematical precedence and will
 * also handle parentheses.  It also, like Java, still follows the order of precedence when strings are present.  That is, if you have the expression:<br>
 * &nbsp&nbsp	<i>#2+#4+" years old, going on "+#2+#4</i><br>
 * It would evaluate to:<br>
 * &nbsp&nbsp	<i>6 years old, going on 24</i><br>
 * You may use any <i>{@literal <variable>}</i> declared within the same XML file and may also use any argument (<i>{@literal <arg>}</i> element) declared within that 
 * <i>{@literal <method>}</i>.  Also string and integer literals may be used.  Currently the only operators supported are +,-,*,/ (which are respectively:
 * addition, subtraction, multiplication, and division).  Parentheses, (), and embedded parentheses, (()()), are also all allowed.
 * 
 * <h2>DIRSTRUCT:</h2>
 * The final section of the XML file is the actual main purpose of the XML file.  This is the XML that controls which directories the crawler will be allowed
 * to crawl and which files will be allowed.  The <i>{@literal <dirstruct>}</i> element requires a 'name' parameter which is the path to the root
 * directory that is to be considered (that is, all other directories below the given directory are unimportant and will not be crawled).  You want your root
 * directory path to stop at the first directory in which you are interested in more than one of its sub-directories or want file(s) inside it.
 * For example, let say we want to crawl a remote site that has the following directory structure:
 * 
 * <pre>
 *	-parent
 *	   -child1
 *	      -grandChild1
 *	         -greatGrandChild1
 *              -file1
 *	         -greatGrandChild2
 *	      -grandChild2
 *	      -file1
 *	   -child2 
 *	      -file1
 * 	   -child3
 * 	      -file1
 *	      -file2
 * 	      -grandChild1
 * 	         -file1
 *	         -file2
 * 	   -child4
 * </pre>
 * 
 * Now, say, we only are interested in directories and files below the two shown 'grandChild1' directories.  This would mean that for our <i>{@literal <dirstruct>}</i>
 * 'name' parameter we would put name="parent".  This is because we need access to both 'child1' and 'child3' subdirectories.  Now in order to avoid crawling
 * 'child2' and 'child4' directories we have to specify <i>{@literal <dir>}</i> elements.  This would give us the following XML:
 * 
 * <pre>
 * 	{@literal <dirstruct name="/parent">
 *	   <dir name="child1"/>
 *	   <dir name="child3"/>
 *	</dirstruct>}
 * </pre>
 *
 * This would restrict the directories allowed under 'parent' to only be directories with names either 'child1' or 'child3', all other directory names will
 * be rejected.  However, more must be added to this example because we have not yet specified any restrictions on files allowed beneath 'parent',
 * we have to add the <i>{@literal <nofiles/>}</i> element:
 * 
 * <pre>
 *	{@literal <dirstruct name="/parent">}
 *	   <i><b>{@literal <nofiles/>}</b></i>
 *	   {@literal <dir name="child1"/>
 *	   <dir name="child3"/>
 *	</dirstruct>} 
 * </pre>
 *
 * Now the only thing acceptable below parent is 'child1' and 'child3'.  We have to still further our restrictions under 'child1' and 'child3'.  Since under 
 * 'child1' we only want 'grandChild1' we would have to make another <i>{@literal <dir>}</i> element and also add a <i>{@literal <nofiles/>}</i> element:
 * 
 * <pre>
 *	{@literal <dirstruct name="/parent">
 *	   <nofiles/>
 *	   <dir name="child1">}
 *	      <i><b>{@literal <nofiles/>
 *	      <dir name="grandChild1"/>}</b></i>
 *	   {@literal </dir>
 *	   <dir name="child3"/>
 *	</dirstruct>}  
 * </pre>
 * 
 * We have to do the same also for 'child3', giving us:
 * 
 * <pre>
 *	{@literal <dirstruct name="/parent">
 *	   <nofiles/>
 *	   <dir name="child1">
 *	      <nofiles/>
 *	      <dir name="grandChild1"/>
 *	   </dir>
 *	   <dir name="child3">}
 *	      <i><b>{@literal <nofiles/>
 *	      <dir name="grandChild1"/>}</b></i>
 *	   {@literal </dir>
 *	</dirstruct>}  
 * </pre>
 *
 * From the example directory structure above, with this XML file specified, that directory structure would be limited to:
 * 
 * <pre>
 *	-parent
 *	   -child1
 *	      -grandChild1
 *	         -greatGrandChild1
 *              -file1
 *	         -greatGrandChild2
 * 	   -child3
 * 	      -grandChild1
 * 	         -file1
 *	         -file2
 * </pre>
 * 
 * Say we now decide that we only want files below the two 'grandChild1' directories -- that is, no directories.  So we would change or XML by adding 
 * in the <i>{@literal <nodir/>}</i> element:
 * 
 * <pre>
 *	{@literal <dirstruct name="/parent">
 *	   <nofiles/>
 *	   <dir name="child1">
 *	      <nofiles/>
 *	      <dir name="grandChild1">}
 *	          <i><b>{@literal <nodirs/>}</b></i>
 *	      {@literal </dir>
 *	   </dir>
 *	   <dir name="child3">
 *	      <nofiles/>
 *	      <dir name="grandChild1">}
 *	         <i><b>{@literal <nodirs/>}</b></i>
 *	      {@literal </dir>
 *	   </dir>
 *	</dirstruct>}   
 * </pre>
 *
 * Which now restricts our directory structure to:
 * 
 * <pre>
 *	-parent
 *	   -child1
 *	      -grandChild1
 * 	   -child3
 * 	      -grandChild1
 * 	         -file1
 *	         -file2
 * </pre>
 * 
 * Let's further specify now that we only want 'file1' in the '/parent/child3/grandChild1' directory.  This would change the XML to:
 * 
 * <pre>
 *	{@literal <dirstruct name="/parent">
 *	   <nofiles/>
 *	   <dir name="child1">
 *	      <nofiles/>
 *	      <dir name="grandChild1">
 *	          <nodirs/>
 *	      </dir>
 *	   </dir>
 *	   <dir name="child3">
 *	      <nofiles/>
 *	      <dir name="grandChild1">
 *	         <nodirs/>}
 *	         <i><b>{@literal <file name="file1"/>}</b></i>
 *	      {@literal </dir>
 *	   </dir>
 *	</dirstruct>}
 * </pre>
 *
 * Our new allowed directory structure would now be:
 * 
 * <pre>
 *	-parent
 *	   -child1
 *	      -grandChild1
 * 	   -child3
 * 	      -grandChild1
 * 	         -file1
 * </pre>
 * 
 *	NOTES:
 * 	-You would not want to use the <i>{@literal <nofiles/>}</i> and <i>{@literal <file>}</i> elements in the same directory (same goes for the
 * 	   <i>{@literal <nodirs/>}</i> and <i>{@literal <dir>}</i> elements) because you would be specifying that you don't want any files in that
 * 	   directory, and then contradict yourself by specifying a <i>{@literal <file>}</i> element that is okay to have.  The <i>{@literal <file>}</i> 
 *	   element states that no other file but the file I specified is allowed.  The only exception is if you have two or more <i>{@literal <file>}</i> 
 *	   elements in the same directory -- this is allowed.  It follows the same rules as the <i>{@literal <dir>}</i> element in the example 
 *	   given above where only 'child1' and 'child3' were allow.  The two don't cancel each other out. 
 * 
 * <h2>ADVANCED USAGES OF DIRSTRUCT:</h2>
 * Regular expressions are allowed in the 'name' parameter of both <i>{@literal <dir>}</i> and <i>{@literal <file>}</i> elements.  Also any 
 * <i>{@literal <method>}</i> or <i>{@literal <variable>}</i> element declared can be used within the 'name' parameter of both <i>{@literal <dir>}</i> 
 * and <i>{@literal <file>}</i> elements.  There are also several predefined variables that can be used.
 * 
 * <h4>REGULAR EXPRESSIONS:</h4>
 * The regular expressions are parsed by the <a href=http://java.sun.com/j2se/1.5.0/docs/api/java/util/regex/Pattern.html>Pattern</a> class 
 * (See its documentation on rule for specifying regular expressions).  Here is an example use of a regular expression:
 * 
 * <pre>
 *	{@literal <dirstruct name="/.../temp/test">
 *	   <nofiles/>
 *	   <dir name="\d{4}-\d{2}-\d{2}">
 *	      <nodirs/>
 *	   </dir>
 *	</dirstruct>}
 * </pre>
 *
 * This would restrict the directory files in directories below /.../temp/test to only directories whose names are dates of the
 * format: YYYY-MM-DD.
 * 
 * <h4>PREDEFINED DATE VARIABLES:</h4>
 * There are several predefined date variables than can be put as the <i>{@literal <value>}</i> of a <i>{@literal <variable>}</i> and then used.
 * 
 * These variables are:
 * <pre>
 * 	[DATE.DAY]	- day of today's date
 * 	[DATE.MONTH]	- month of today's date
 * 	[DATE.YEAR]	- year of today's date
 * 	[DATE-N.DAY]	- the day of the date N days ago
 * 	[DATE-N.MONTH]	- the month of the date N days ago
 * 	[DATE-N.YEAR]	- the year of the date N days ago
 * 	[DATE+N.DAY]	- the day of the date N days from now
 * 	[DATE+N.MONTH]	- the month of the date N days from now
 * 	[DATE+N.YEAR]	- the year of the date N days from now
 * 
 * 	-sorry, no DayOfYear implemented yet -- hopefully in a later release
 * </pre>
 * 
 * Usage:
 * <pre>
 * 	{@literal <root>
 * 	   <variables>
 * 	      <variable name="todaysDay">
 * 	         <type>INT</type>
 * 	         <value>[DATE.DAY]</value>
 *	         <precision>
 *	            <locations>2</locations>
 *	            <fill>0</fill>
 *	            <side>front</side>
 *	         </precision>
 *	      </variable>
 *	   </variabls>
 *	   <dirstruct name="/path/to/parent/dir">
 *	      <nofiles/>
 *	      <dir name="MyFiles">
 *	         <nodirs/>
 *	         <file name="MyPaper_${todaysDay}"/>
 *	      </dir>
 *	   </dirstruct>
 *	</root>}
 *  </pre>
 *  
 *  This would allow only a file in /path/to/parent/dir/MyFiles which had the name which started with MyPaper_ and ended with the
 *  day of the current day of the month.  For example, if to days date was 03/23/2005, then the file name allowed would be
 *  MyPaper_23.
 * 
 * <h4>METHOD AND VARIABLE USAGE IN DIRSTRUCT:</h4>
 * Here is an example of using <i>{@literal <variables>}</i> and <i>{@literal <methods>}</i>:
 * 
 * <pre>
 *	{@literal <root>
 *	   <variables>
 *	      <variable name="DAY">
 *	         <type>INT</type>
 *	         <value>[DATE.DAY]</value>
 *	         <precision>
 *	            <locations>2</locations>
 *	            <fill>0</fill>
 *	            <side>front</side>
 *	         </precision>
 *	      </variable>
 *	      <variable name="MONTH">
 *	         <type>INT</type>
 *	         <value>[DATE.MONTH]</value>
 *	         <precision>
 *	            <locations>2</locations>
 *	            <fill>0</fill>
 *	            <side>front</side>
 *	         </precision>
 *	      </variable>
 *	      <variable name="YEAR">
 *	         <type>INT</type>
 *	         <value>[DATE.YEAR]</value>
 *	      </variable>
 *	   </variables>
 *   
 *	   <methods>
 *	      <method name="ADD">
 *	         <args>
 *	            <arg name="1">
 *	               <type>INT</type>
 *	            </arg>
 *	         </args>
 *	         <action>"THE_YEAR_PLUS_"+$1+": "+(${YEAR}+$1)</action>
 *	      </method>
 *	      <method name="HOW_OLD_AM_I">
 *	         <action>${YEAR}-#1984</action>
 *	      </method>
 *	      <method name="DATE">
 *	         <action>${YEAR}+"-"+${MONTH}+"-"+${DAY}</action>
 *	      </method>
 *	   </methods>
 *
 *	   <dirstruct name="/path/to/parent/dir">
 *	      <nofiles/>
 *	      <dir name="AGE_%HOW_OLD_AM_I()"/>
 *	      <dir name="DATE">
 *	         <nodirs/>
 *	         <file name="%ADD(5)"/>
 *	      </dir>
 *	   </dirstruct>
 *	</root>}
 * </pre>
 *
 * This would accept only the directories under /path/to/parent/dir which had the name (given today is 9/7/2007) 'AGE_23' or '2007-09-07'.
 * This would allow any file or directory in under 'AGE_23', but would only allow a file with the name 'THE_YEAR_PLUS_5: 2012' in the 
 * directory '2007-09-07'.
 *  
 * 
 * @author bfoster
 *
 */
public class FileRestrictions {

    private FileRestrictions() throws InstantiationException{
        throw new InstantiationException("Don't construct FileRestrictions!");
    }

    /**
     * 
     * @param path
     * @return The initial cd directory which needs to be changed to (in order
     *         to take care of possible auto-mounted directories)
     */
    public static boolean isAllowed(ProtocolPath path, VirtualFile root) {
        return (isValidPath(path) && isAllowed(new VirtualFile(path
                .getPathString(), path.isDirectory()), root));
    }

    public static boolean isAllowed(VirtualFile file, VirtualFile root) {
        boolean vfDoesNotExist = false, lastFileIsDir = file.isDir();
        VirtualFile vf = null;
        while ((vf = root.getChildRecursive(file)) == null) {
            vfDoesNotExist = true;
            lastFileIsDir = file.isDir();
            file = file.getParentFile();
            if (file == null)
                break;
        }
        return !(file == null || (vfDoesNotExist && ((lastFileIsDir && !vf
                .allowNewDirs()) || (!lastFileIsDir && !vf.allowNewFiles()))));
    }

    private static boolean isValidPath(ProtocolPath path) {
        if (path != null && !path.getFileName().equals(".")
                && !path.getFileName().equals("..")) {
            return true;
        } else {
            return false;
        }
    }

    public static LinkedList<String> toStringList(VirtualFile root) {
        LinkedList<String> stringList = new LinkedList<String>();
        stringList.addAll(toStringList(root.getChildren(), ""));
        return stringList;
    }

    private static LinkedList<String> toStringList(
            LinkedList<VirtualFile> children, String curPath) {
        LinkedList<String> stringList = new LinkedList<String>();
        for (VirtualFile child : children) {
            String currentPath = curPath + "/" + child.getRegExp();
            if (!child.isDir())
                stringList.add(currentPath);
            else
                stringList
                        .addAll(toStringList(child.getChildren(), currentPath));
        }
        return stringList;
    }
}
