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


package org.apache.oodt.cas.workflow.examples;

//JDK imports
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//OODT imports

/**
 * 
 * A CAS {@link WorkflowTaskInstance} responsible for sending an email
 * to a recipient notifying them of ingest.
 * 
 * @author mattmann
 * @version $Revision$
 * 
 */
public class MailTask implements WorkflowTaskInstance {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance#run(gov.nasa
   * .jpl.oodt.cas.metadata.Metadata,
   * org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
   */
  public void run(Metadata metadata, WorkflowTaskConfiguration config)
      throws WorkflowTaskInstanceException {
    Properties mailProps = new Properties();
    mailProps.setProperty("mail.host", "smtp.jpl.nasa.gov");
    mailProps.setProperty("mail.user", "mattmann");
    
    Session session = Session.getInstance(mailProps);

    String msgTxt = "Hello "
        + config.getProperty("user.name")
        + ":\n\n"
        + "You have successfully ingested the file with the following metadata: \n\n"
        + getMsgStringFromMet(metadata) + "\n\n" + "Thanks!\n\n" + "CAS";

    Message msg = new MimeMessage(session);
    try {
      msg.setSubject(config.getProperty("msg.subject"));
      msg.setSentDate(new Date());
      msg.setFrom(InternetAddress.parse(config.getProperty("mail.from"))[0]);
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(config
          .getProperty("mail.to"), false));
      msg.setText(msgTxt);
      Transport.send(msg);

    } catch (MessagingException e) {
      throw new WorkflowTaskInstanceException(e.getMessage());
    }

  }

  private String getMsgStringFromMet(Metadata met) {
    StringBuilder buf = new StringBuilder();
    for (Object key : met.getMap().keySet()) {
      String keyStr = (String) key;
      StringBuilder val = new StringBuilder();
      for (Object value : met.getAllMetadata(keyStr)) {
        String valStr = (String) value;
        val.append(valStr);
        val.append(",");
      }
      val.deleteCharAt(val.length() - 1);

      buf.append("[").append(keyStr).append("=>").append(val).append("]\n");
    }

    return buf.toString();
  }

}
