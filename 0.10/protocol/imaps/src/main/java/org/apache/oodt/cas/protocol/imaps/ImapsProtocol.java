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
package org.apache.oodt.cas.protocol.imaps;

//JDK imports
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

//Javamail imports
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import org.xml.sax.SAXException;

//APACHE imports
import org.apache.commons.codec.DecoderException;

//OODT imports
import org.apache.oodt.cas.protocol.Protocol;
import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.protocol.auth.Authentication;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;
import org.apache.oodt.cas.protocol.util.ProtocolFileFilter;

//TIKA imports
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.TextContentHandler;

/**
 * IMAP Secure {@link Protocol} implementation
 * 
 * @author bfoster
 * @version $Revision$
 */
public class ImapsProtocol implements Protocol {

  static Store store;

  static Folder currentFolder;
  
  static Folder homeFolder;
  
  static int port = 993;

  static Session session;

  static int openCalls = 0;

  static int connectCalls = 0;
  
  public synchronized void cd(ProtocolFile file) throws ProtocolException {
    try {
      String remotePath = file.getPath();
      if (remotePath.startsWith("/"))
        remotePath = remotePath.substring(1);
      if (remotePath.trim().equals(""))
        homeFolder = currentFolder = store.getDefaultFolder();
      else {
        homeFolder = currentFolder = store.getFolder(remotePath);
      }
    } catch (Exception e) {
      throw new ProtocolException("Failed to change directory to '" 
      		+ file + "' : " + e.getMessage(), e);
    }
  }

  public synchronized void cdRoot() throws ProtocolException {
    try {
      cd(new ProtocolFile("/", true));
    } catch (Exception e) {
      throw new ProtocolException("Failed to cd to root : " + e.getMessage(), e);
    }
  }
  
  public synchronized void cdHome() throws ProtocolException {
    try {
      cd(new ProtocolFile("", true));
    } catch (Exception e) {
      throw new ProtocolException("Failed to cd to home : " + e.getMessage(), e);
    }
  }
  
  public synchronized void connect(String host, Authentication auth)
      throws ProtocolException {
    try {
      if (store == null) {
        store = (session = Session.getInstance(System.getProperties()))
            .getStore("imaps");
        store.connect(host, port, auth.getUser(), auth.getPass());
        currentFolder = store.getDefaultFolder();
      }
      this.incrementConnections();
    } catch (Exception e) {
      throw new ProtocolException("Failed to connected to IMAPS server " + host
          + " with username " + auth.getUser() + " : " + e.getMessage(), e);
    }
  }

  public synchronized void close() throws ProtocolException {
    decrementConnections();
    if (connectCalls <= 0) {
      try {
//        if (!currentFolder.isOpen()) {
//          try {
//            currentFolder.open(Folder.READ_WRITE);
//          } catch (Exception e) {
//            try {
//              currentFolder.open(Folder.READ_ONLY);
//            } catch (Exception e2) {
//            }
//          }
//        }
//        currentFolder.close(true);
        store.close();
      } catch (Exception e) {
        throw new ProtocolException("Failed to close connection : " + e.getMessage(), e);
      } finally {
        store = null;
      }
    }
  }

  private synchronized void incrementConnections() {
    connectCalls++;
  }

  private synchronized void decrementConnections() {
    if (connectCalls > 0)
      connectCalls--;
  }

  public synchronized void get(ProtocolFile fromFile, File toFile)
      throws ProtocolException {
    try {
      openFolder(currentFolder);
      Message[] messages = currentFolder.getMessages();
      for (Message message : messages) {
        if (this.getMessageName(message).equals(fromFile.getName())) {
          writeMessageToLocalFile(message, toFile);
          // message.setFlag(Flags.Flag.DELETED, true);
          break;
        }
      }
    } catch (Exception e) {
      throw new ProtocolException("Failed to download " + fromFile + " to "
          + toFile + " : " + e.getMessage(), e);
    } finally {
      try {
        closeFolder(currentFolder);
      } catch (Exception e) {
      }
    }
  }

  public synchronized void put(File fromFile, ProtocolFile toFile) {
	  //do nothing;
  }
  
  private void writeMessageToLocalFile(Message message, File toLocalFile)
      throws MessagingException, IOException, DecoderException, SAXException,
      TikaException {
    PrintStream ps = new PrintStream(new FileOutputStream(toLocalFile));

    ps.print("From:");
    Address[] senders = message.getFrom();
    for (Address address : senders)
      ps.print(" " + address.toString());

    ps.print("\nTo:");
		Set<Address> recipients = new LinkedHashSet<Address>(Arrays.asList(message
				.getAllRecipients()));
    for (Address address : recipients)
      ps.print(" " + address.toString());

    ps.println("\nSubject: " + message.getSubject());

    ps.println("----- ~ Message ~ -----");
    String content = this.getContentFromHTML(message);
    if (content.equals(""))
      content = this.getContentFromPlainText(message);
    ps.println(content);

    ps.close();
  }

  public synchronized boolean connected() {
    return store.isConnected();
  }

  public List<ProtocolFile> ls() throws ProtocolException {
    // if (changedDir) {
    // System.out.println("Refreshed LS");
    // currentFilesForCurrentFolder = new LinkedList<ProtocolFile>();
    LinkedList<ProtocolFile> currentFilesForCurrentFolder = new LinkedList<ProtocolFile>();
    try {
      openFolder(currentFolder);
      if (!currentFolder.getFullName().equals(
          store.getDefaultFolder().getFullName())) {
        Message[] messages = currentFolder.getMessages();
        for (Message message : messages) {
          currentFilesForCurrentFolder.add(new ProtocolFile(this.pwd().getPath()
              + "/" + this.getMessageName(message), false));
        }
      }
      // changedDir = false;
    } catch (Exception e) {
      if (!currentFolder.getFullName().equals(""))
        throw new ProtocolException("Failed to ls : " + e.getMessage(), e);
    } finally {
      try {
        closeFolder(currentFolder);
      } catch (Exception e) {
      }
    }
    // }
    return currentFilesForCurrentFolder;
  }

	public List<ProtocolFile> ls(ProtocolFileFilter filter)
			throws ProtocolException {
    LinkedList<ProtocolFile> currentFilesForCurrentFolder = new LinkedList<ProtocolFile>();
    try {
      openFolder(currentFolder);
      if (!currentFolder.getFullName().equals(
          store.getDefaultFolder().getFullName())) {
        Message[] messages = currentFolder.getMessages();
        for (Message message : messages) {
        	ProtocolFile pFile = new ProtocolFile(this.pwd().getPath()
              + "/" + this.getMessageName(message), false);
        	if (filter.accept(pFile)) {
        		currentFilesForCurrentFolder.add(pFile);
        	}
        }
      }
    } catch (Exception e) {
      if (!currentFolder.getFullName().equals(""))
        throw new ProtocolException("Failed to ls : " + e.getMessage(), e);
    } finally {
      try {
        closeFolder(currentFolder);
      } catch (Exception e) {
      }
    }
    return currentFilesForCurrentFolder;
	}

  public synchronized ProtocolFile pwd()
      throws ProtocolException {
    try {
      String pwd = currentFolder.getFullName();
      if (!pwd.equals("") && !pwd.startsWith("/"))
        pwd = "/" + pwd;
      return new ProtocolFile(pwd, true);
    } catch (Exception e) {
      throw new ProtocolException("Failed to pwd : " + e.getMessage(), e);
    }
  }

  private String getMessageName(Message msg) throws MessagingException {
    Enumeration headers = msg.getAllHeaders();
    while (headers.hasMoreElements()) {
      Header header = (Header) headers.nextElement();
      if (header.getName().toLowerCase().equals("message-id")) {
        String stringHeader = header.getValue();
        // System.out.println(stringHeader);
        stringHeader = stringHeader.replace("<", "");
        return stringHeader.substring(0, stringHeader.indexOf("@"));
      }
    }
    return null;
  }

  private String getContentFromPlainText(Part p) throws MessagingException,
      IOException, DecoderException {
    StringBuffer content = new StringBuffer("");
    if (p.isMimeType("text/plain")) {
      content.append((String) p.getContent());
    } else if (p.isMimeType("multipart/*")) {
      Multipart mp = (Multipart) p.getContent();
      int count = mp.getCount();
      for (int i = 0; i < count; i++)
        content.append(getContentFromPlainText(mp.getBodyPart(i)));
    } else {
      Object obj = p.getContent();
      if (obj instanceof Part)
        content.append(getContentFromPlainText((Part) p.getContent()));
    }
    return content.toString().replaceAll(" \\r\\n", "").replaceAll(" \\n", "");
  }

  private String getContentFromHTML(Part p) throws MessagingException,
      IOException, DecoderException, SAXException, TikaException {
    StringBuffer content = new StringBuffer("");
    if (p.isMimeType("multipart/*")) {
      Multipart mp = (Multipart) p.getContent();
      int count = mp.getCount();
      for (int i = 0; i < count; i++)
        content.append(getContentFromHTML(mp.getBodyPart(i)));
    } else if (p.isMimeType("text/html")) {
      HtmlParser parser = new HtmlParser();
      Metadata met = new Metadata();
      TextContentHandler handler = new TextContentHandler(
          new BodyContentHandler());
      parser.parse(new ByteArrayInputStream(((String) p.getContent())
          .getBytes()), handler, met);
      content.append(handler.toString());
    } else {
      Object obj = p.getContent();
      if (obj instanceof Part)
        content.append(getContentFromHTML((Part) p.getContent()));
    }
    return content.toString();
  }

  private synchronized void openFolder(Folder folder) throws ProtocolException {
    if (!folder.isOpen()) {
      try {
        folder.open(Folder.READ_WRITE);
      } catch (Exception e) {
        try {
          folder.open(Folder.READ_ONLY);
        } catch (Exception e2) {
          throw new ProtocolException("Failed to open folder : "
              + e.getMessage() + " : " + e2.getMessage());
        }
      }
    }
    openCalls++;
  }

  private synchronized void closeFolder(Folder folder) {
    if (openCalls > 0)
      openCalls--;

    if (openCalls <= 0) {
      try {
        folder.close(true);
      } catch (Exception e) {
      }
    }
  }

  public synchronized void delete(ProtocolFile file) throws ProtocolException {
    try {
      openFolder(currentFolder);
      Message[] messages = currentFolder.getMessages();
      for (Message message : messages) {
        if (this.getMessageName(message).equals(file.getName())) {
          message.setFlag(Flags.Flag.DELETED, true);
          break;
        }
      }
    } catch (Exception e) {
    	throw new ProtocolException("Failed to delete file '" + file + "' : " + e.getMessage(), e);
    } finally {
      closeFolder(currentFolder);
    }
  }

}
