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

package org.apache.oodt.cas.crawl.action;

//JDK imports
import java.io.File;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

//OODT imports
import org.apache.oodt.cas.crawl.action.CrawlerAction;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;

/**
 * This action sends an email notification. It performs metadata and system
 * property replacement on the subject, recipients, and message. This allows the
 * notifications to be dynamically generated.
 * 
 * @author pramirez
 * @author mattmann
 * 
 */
public class EmailNotification extends CrawlerAction {

  private String mailHost;
  private String subject;
  private String message;
  private String recipients;
  private String sender;
  private boolean ignoreInvalidAddresses;

  public EmailNotification() {
    this.ignoreInvalidAddresses = true;
  }

  @Override
  public boolean performAction(File product, Metadata metadata)
      throws CrawlerActionException {
    Properties props = new Properties();
    props.put("mail.host", this.mailHost);
    props.put("mail.transport.protocol", "smtp");
    props.put("mail.from", this.sender);

    Session session = Session.getDefaultInstance(props);
    Message msg = new MimeMessage(session);
    try {
      msg.setSubject(PathUtils.doDynamicReplacement(subject, metadata));
      msg.setText(new String(PathUtils.doDynamicReplacement(message, metadata)
          .getBytes()));
      String[] recips = recipients.split(",");
      for (String recipient : recips) {
        try {
          msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
              PathUtils.replaceEnvVariables(recipient.trim(), metadata),
              ignoreInvalidAddresses));
          LOG.fine("Recipient: "
              + PathUtils.replaceEnvVariables(recipient.trim(), metadata));
        } catch (AddressException ae) {
          LOG.fine("Recipient: "
              + PathUtils.replaceEnvVariables(recipient.trim(), metadata));
          LOG.warning(ae.getMessage());
        }
      }
      LOG.fine("Subject: " + msg.getSubject());
      LOG.fine("Message: "
          + new String(PathUtils.doDynamicReplacement(message, metadata)
              .getBytes()));
      Transport.send(msg);
    } catch (Exception e) {
      LOG.severe(e.getMessage());
      return false;
    }

    return true;
  }

  @Required
  public void setMailHost(String smtpHost) {
    this.mailHost = smtpHost;
  }

  @Required
  public void setSubject(String subject) {
    this.subject = subject;
  }

  @Required
  public void setMessage(String message) {
    this.message = message;
  }

  @Required
  public void setRecipients(String recipients) {
    this.recipients = recipients;
  }

  @Required
  public void setSender(String sender) {
    this.sender = sender;
  }

  public void setIgnoreInvalidAddresses(Boolean ignoreInvalidAddresses) {
    this.ignoreInvalidAddresses = ignoreInvalidAddresses;
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 5) {
      System.out.println("Usage: java " + EmailNotification.class.getName()
          + " <mailhost> <sender> <recipients> <subject> <message>");
      System.exit(-1);
    }
    EmailNotification notification = new EmailNotification();
    notification.setMailHost(args[0]);
    notification.setSender(args[1]);
    notification.setRecipients(args[2]);
    notification.setSubject(args[3]);
    notification.setMessage(args[4]);
    notification.performAction(new File(""), new Metadata());
  }
}
