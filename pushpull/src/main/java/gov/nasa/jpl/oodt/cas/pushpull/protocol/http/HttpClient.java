//Copyright (c) 2008, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.pushpull.protocol.http;

//OODT imports
import gov.nasa.jpl.oodt.cas.pushpull.exceptions.ProtocolException;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.Protocol;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolFile;
import gov.nasa.jpl.oodt.cas.pushpull.protocol.ProtocolPath;
import gov.nasa.jpl.oodt.cas.metadata.util.MimeTypeUtils;

//JDK imports
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//HTMLParser imports
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.SimpleNodeIterator;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 * <p>
 * Http Protocol communication class
 * </p>.
 */
public class HttpClient extends Protocol {

    static String DIR = "dir";

    static String FILE = "file";

    static String IGNORE = "ignore";

    static HashMap<String, LinkedList<ProtocolFile>> linkChildren = new HashMap<String, LinkedList<ProtocolFile>>();

    static boolean takeAllFiles = true;

    HttpPath parentPath;

    boolean abort;

    HttpPath currentPath;

    boolean isConnected;

    MimeTypeUtils mimeTypes;

    public HttpClient() throws InstantiationException {
        super("http");
        try {
            mimeTypes = new MimeTypeUtils();
        } catch (Exception e) {
            e.printStackTrace();
            throw new InstantiationException(
                    "Failed to load tika configuration file : "
                            + e.getMessage());
        }
        isConnected = false;
    }

    protected void chDir(ProtocolPath path) throws ProtocolException {
        if (!(path instanceof HttpPath))
            throw new ProtocolException(
                    "HttpClient must receive a HttpPath - failed to cd");

        HttpPath httpPath = (HttpPath) path;
        try {
        	if (!this.isDirectory(httpPath.getLink().toString(), path.getPathString()))
                throw new ProtocolException(path
                        + " is not a directory (mime type must be text/html)");
            this.currentPath = httpPath;
        } catch (Exception e) {
            throw new ProtocolException("Failed to cd to " + path + " : "
                    + e.getMessage());
        }
    }

    public void cdToRoot() {
        this.currentPath = this.parentPath;
    }

    public void connect(String host, String username, String password)
            throws ProtocolException {
        try {
            URL newURL = new URL("http://" + host + "/");
            newURL.openStream().close();
            currentPath = parentPath = new HttpPath("/", true, newURL, null);
            isConnected = true;
        } catch (Exception e) {
            throw new ProtocolException("Failed to connect to http://" + host
                    + " : " + e.getMessage());
        }
    }

    public void disconnectFromServer() throws ProtocolException {
        currentPath = parentPath = null;
    }

    public void getFile(ProtocolFile file, File toLocalFile)
            throws ProtocolException {

        OutputStream out = null;
        InputStream in = null;
        try {
            this.abort = false;
            out = new BufferedOutputStream(new FileOutputStream(toLocalFile));
            in = ((HttpPath) file.getProtocolPath()).getLink().openStream();

            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            while ((numRead = in.read(buffer)) != -1 && !this.abort) {
                out.write(buffer, 0, numRead);
                numWritten += numRead;
            }
            in.close();
            out.close();
        } catch (Exception e) {
            throw new ProtocolException("Failed to get file " + file + " : "
                    + e.getMessage());
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Exception e) {
                    // log failure
                }
            if (out != null)
                try {
                    out.close();
                } catch (Exception e) {
                    // log failure
                }
        }
    }

    public void abortCurFileTransfer() {
        this.abort = true;
    }

    public List<ProtocolFile> listFiles() throws ProtocolException {
        return parseLink(currentPath);
    }

    public ProtocolFile getCurrentWorkingDir() throws ProtocolException {
        try {
            return new ProtocolFile(this.getRemoteSite(), currentPath);
        } catch (Exception e) {
            throw new ProtocolException(
                    "Failed to get current working directory : "
                            + e.getMessage());
        }
    }

    public boolean isConnected() throws ProtocolException {
        return this.isConnected;
    }

    public LinkedList<ProtocolFile> parseLink(HttpPath path)
            throws ProtocolException {
        LinkedList<ProtocolFile> children = linkChildren.get(path.getLink()
                .toString());
        if (path.isDirectory() && children == null) {
            try {

                // Open link
                HttpURLConnection con = (HttpURLConnection) path.getLink()
                        .openConnection();
                con.connect();
                con.getResponseMessage();

                // if redirection took place, then change the ProtocolFile's URL
                if (!path.getLink().toString().equals(con.getURL().toString()))
                    path = new HttpPath(path.getPathString(), path
                            .isDirectory(), con.getURL(), path);

                // create URL source reader
                Scanner scanner = new Scanner(con.getInputStream());

                // Read in link
                StringBuffer sb = new StringBuffer("");
                while (scanner.hasNext())
                    sb.append(scanner.nextLine());

                Parser parser = Parser.createParser(sb.toString(), null);
                org.htmlparser.util.NodeList nl = parser
                        .parse(new NodeFilter() {
                            public boolean accept(org.htmlparser.Node arg0) {
                                if (arg0 instanceof TagNode) {
                                    return ((TagNode) arg0).getTagName()
                                            .toLowerCase().equals("a");
                                }
                                return false;
                            }
                        });

                children = new LinkedList<ProtocolFile>();
                SimpleNodeIterator sni = nl.elements();
                while (sni.hasMoreNodes()) {
                    try {
                        TagNode tn = (TagNode) sni.nextNode();
                        String href = tn.getAttribute("href");
                        if (href == null)
                            if ((href = HttpClient.findLinkInATag(tn.toHtml())) == null)
                                continue;
                        String link = HttpClient.createLinkFromHref(path, href);
                        String linkName = null;
                        SimpleNodeIterator aTagChildNodes = tn.getChildren()
                                .elements();
                        while (aTagChildNodes.hasMoreNodes()) {
                            org.htmlparser.Node node = aTagChildNodes
                                    .nextNode();
                            if (node instanceof TextNode) {
                                linkName = ((TextNode) node).getText().trim();
                                if (linkName.endsWith("/"))
                                    linkName = linkName.substring(0, linkName
                                            .length() - 1);
                                break;
                            }
                        }
                        if (linkName == null || linkName.equals(""))
                            continue;
                        String curPath = this.pwd().getProtocolPath()
                                .getPathString();
                        String linkPath = curPath
                                + (curPath.endsWith("/") ? "" : "/") + linkName;
                        children.add(new ProtocolFile(this.getRemoteSite(),
                                new HttpPath(linkPath, isDirectory(link,
                                        linkPath), new URL(link), path)));
                    } catch (Exception e) {
                    }
                }
                linkChildren.put(path.getLink().toString(), children);

            } catch (Exception e) {
                e.printStackTrace();
                throw new ProtocolException("Failed to get children links for "
                        + path + " : " + e.getMessage());
            }
        }
        return children;
    }

    public static String findLinkInATag(String aTag) {
        // find 'href' attribute
        String find = aTag.substring(aTag.indexOf("href") + 4);
        // USE STRICT FINDING FIRST
        // (['\"])\s*?[(http)(./)(..)/#].+?\\1
        // finds link between ' or ", which starts with one of
        // the following: http, ./, .., /, #
        // these starting possibilities can then be followed any
        // number of characters until the corresponding
        // ' or " is reached.
        String patternRegExp = "(['\"])\\s*?[\\(http\\)\\(\\./\\)\\(\\.\\.\\)/#].+?\\1";
        Pattern linkPattern = Pattern.compile(patternRegExp);
        Matcher linkMatch = linkPattern.matcher(find);
        if (linkMatch.find())
            find = find.substring(linkMatch.start() + 1, linkMatch.end() - 1);
        else {
            // RELAX FINDING SOME
            patternRegExp = "(['\"])\\s*?[^./].+?\\1";
            linkPattern = Pattern.compile(patternRegExp);
            linkMatch = linkPattern.matcher(find);
            if (linkMatch.find())
                find = find.substring(linkMatch.start() + 1,
                        linkMatch.end() - 1);
            else {
                // EXTREMELY RELAX FINDING
                patternRegExp = "[^\"='/>\\s]+?[^\\s>\"']*?";
                linkPattern = Pattern.compile(patternRegExp);
                linkMatch = linkPattern.matcher(find);
                if (linkMatch.find())
                    find = find.substring(linkMatch.start(), linkMatch.end());
                else {
                    return null;
                }
            }
        }
        return find;
    }

    public boolean isDirectory(String link, String virtualPath)
            throws ProtocolException, IOException {
        // connect URL and get content type
        try {
    		String mime = this.mimeTypes.autoResolveContentType(link, 
    				MimeTypeUtils.readMagicHeader(new URL(link).openStream()));
            return (mime.equals("text/html") && !virtualPath.endsWith(".html"));
        } catch (Exception e) {
            throw new IOException("URL does not exist " + link);
        }
    }

    public static String createLinkFromHref(HttpPath parent, String href) {
        if (!href.startsWith("http")) {
            String link = parent.getLink().toExternalForm();
            if (href.startsWith("..")) {
                int index = link.substring(0, link.lastIndexOf("/"))
                        .lastIndexOf("/");
                href = (index < 7) ? link + href.substring(2) : link.substring(
                        0, link.substring(0, link.lastIndexOf("/"))
                                .lastIndexOf("/"))
                        + href.substring(2);
            } else if (href.startsWith("./")) {
                int index = link.lastIndexOf("/");
                href = (index < 7) ? link + href.substring(1) : link.substring(
                        0, index)
                        + href.substring(1);
            } else if (href.startsWith("/")) {
                URL url = parent.getLink();
                href = url.getProtocol() + "://" + url.getHost() + href;
            } else {
                // find the last / in current link
                int index = link.lastIndexOf("/");
                // (index < 7) checks if in the current link, "/" only exists
                // in the protocol section of link (i.e. http://jpl.nasa.gov)
                href = (index < 7) ? link + "/" + href : link.substring(0,
                        index)
                        + "/" + href;
            }
        }

        // remove "/" at end of link
        if (href.endsWith("/"))
            href = href.substring(0, href.length() - 1);
        href = href.trim();

        return href;
    }

    public ProtocolFile getProtocolFileFor(String path, boolean isDir)
            throws ProtocolException {
        try {
            StringTokenizer st = new StringTokenizer(path, "/ ");
            HttpPath curPath = this.parentPath;
            // System.out.println(parentPath);
            if (st.hasMoreTokens()) {
                do {
                    String token = st.nextToken();
                    LinkedList<ProtocolFile> children = this.parseLink(curPath);
                    for (ProtocolFile pFile : children) {
                        if (pFile.getName().equals(token)) {
                            // System.out.println("token " + token + " " +
                            // pFile);
                            curPath = (HttpPath) pFile.getProtocolPath();
                            continue;
                        }
                    }
                } while (st.hasMoreTokens());
                if (curPath.equals(this.parentPath))
                    return new ProtocolFile(this.getRemoteSite(), new HttpPath(
                            path, isDir, new URL("http://"
                                    + this.getRemoteSite().getURL().getHost()
                                    + path), curPath));
            }
            return new ProtocolFile(this.getRemoteSite(), curPath);
        } catch (Exception e) {
            throw new ProtocolException("Failed to get ProtocolPath for "
                    + path);
        }
    }

    @Override
    public boolean deleteFile(ProtocolFile file) {
        return false;
    }
    
    public static void main(String[] args) throws Exception {
    	String urlString = null, downloadToDir = null;
    	for (int i = 0; i < args.length; i++) {
    		if (args[i].equals("--url"))
    			urlString = args[++i];
    		else if (args[i].equals("--downloadToDir"))
    			downloadToDir = args[++i];
    	}
    	
    	if (urlString == null)
    		throw new Exception("Must specify a url to download: --url <url>");
    	
    	URL url = new URL(urlString);
    	ProtocolFile urlFile = new ProtocolFile(null, new HttpPath(url.getPath(), false, url, null));
    	File toFile = new File(downloadToDir, urlFile.getName());
    	toFile = toFile.getAbsoluteFile();
    	toFile.createNewFile();
    	new HttpClient().getFile(urlFile, toFile);
    }

}
