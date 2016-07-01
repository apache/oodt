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
import com.google.common.collect.Lists;

import org.apache.commons.lang.Validate;
import org.apache.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.util.PathUtils;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//Spring imports
//Apache imports
//OODT imports
//Google imports

/**
 * This action sends an email notification. It performs metadata and system
 * property replacement on the subject, recipients, and message. This allows the
 * notifications to be dynamically generated.
 * 
 * @author pramirez (Paul Ramirez)
 * @author mattmann (Chris Mattmann)
 * 
 */
public class EmailNotification extends CrawlerAction {

   private String mailHost;
   private String subject;
   private String message;
   private List<String> recipients;
   private String sender;
   private boolean ignoreInvalidAddresses;

   public EmailNotification() {
      this.ignoreInvalidAddresses = true;
   }

   @Override
   public boolean performAction(File product, Metadata metadata)
         throws CrawlerActionException {
      try {
         Properties props = new Properties();
         props.put("mail.host", this.mailHost);
         props.put("mail.transport.protocol", "smtp");
         props.put("mail.from", this.sender);

         Session session = Session.getDefaultInstance(props);
         Message msg = new MimeMessage(session);
         msg.setSubject(PathUtils.doDynamicReplacement(subject, metadata));
         msg.setText(new String(PathUtils.doDynamicReplacement(message,
               metadata).getBytes()));
         for (String recipient : recipients) {
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
         return true;
      } catch (Exception e) {
         LOG.severe(e.getMessage());
         return false;
      }
   }

   @Override
   public void validate() throws CrawlerActionException {
      super.validate();
      try {
         Validate.notNull(mailHost, "Must specify mailHost");
         Validate.notNull(subject, "Must specify subject");
         Validate.notNull(message, "Must specify message");
         Validate.notNull(recipients, "Must specify recipients");
         Validate.notNull(sender, "Must specify sender");
      } catch (Exception e) {
         throw new CrawlerActionException(e);
      }
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

   public void setRecipients(String recipients) {
      if (recipients != null) {
         this.recipients = Lists.newArrayList(recipients.split(","));
      }
   }

   public void setRecipients(List<String> recipients) {
      this.recipients = recipients;
   }

   @Required
   public void setSender(String sender) {
      this.sender = sender;
   }

   public void setIgnoreInvalidAddresses(Boolean ignoreInvalidAddresses) {
      this.ignoreInvalidAddresses = ignoreInvalidAddresses;
   }

   public static void main(String[] args) throws CrawlerActionException {
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
