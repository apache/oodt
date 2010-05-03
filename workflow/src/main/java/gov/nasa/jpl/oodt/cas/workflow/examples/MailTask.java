//Copyright (c) 2009, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.oodt.cas.workflow.examples;

//JDK imports
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//OODT imports
import gov.nasa.jpl.oodt.cas.metadata.Metadata;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskInstance;
import gov.nasa.jpl.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;

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
   * gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskInstance#run(gov.nasa
   * .jpl.oodt.cas.metadata.Metadata,
   * gov.nasa.jpl.oodt.cas.workflow.structs.WorkflowTaskConfiguration)
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
    StringBuffer buf = new StringBuffer();
    for (Object key : met.getHashtable().keySet()) {
      String keyStr = (String) key;
      StringBuffer val = new StringBuffer();
      for (Object value : met.getAllMetadata(keyStr)) {
        String valStr = (String) value;
        val.append(valStr);
        val.append(",");
      }
      val.deleteCharAt(val.length() - 1);

      buf.append("[" + keyStr + "=>" + val + "]\n");
    }

    return buf.toString();
  }

}
