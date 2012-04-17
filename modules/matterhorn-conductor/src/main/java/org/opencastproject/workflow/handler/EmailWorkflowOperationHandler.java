/**
 *  Copyright 2009, 2010 The Regents of the University of California
 *  Licensed under the Educational Community License, Version 2.0
 *  (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at
 *
 *  http://www.osedu.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS"
 *  BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 */
package org.opencastproject.workflow.handler;

import org.opencastproject.job.api.JobContext;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.workflow.api.AbstractWorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.api.WorkflowOperationResult.Action;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * Please describe what this handler does.
 */
public class EmailWorkflowOperationHandler extends AbstractWorkflowOperationHandler {

  private static final Logger logger = LoggerFactory.getLogger(EmailWorkflowOperationHandler.class);

  // Configuration properties defined in the JavaMail API
  // TODO: Complete list
  private static final String MAIL_STORE_PROPERTY = "mail.store.protocol";
  private static final String MAIL_TRANSPORT_PROPERTY = "mail.transport.protocol";
  private static final String MAIL_DEBUG_PROPERTY = "mail.debug";
  private static final String MAIL_HOST_PROPERTY = "mail.host";
  private static final String MAIL_USER_PROPERTY = "mail.user";
  private static final String MAIL_FROM_PROPERTY = "mail.from";
  private static final String MAIL_AUTH_PROPERTY = "mail.auth";

  // Configuration properties used in the workflow definition
  private static final String TO_PROPERTY = "to";
  private static final String SUBJECT_PROPERTY = "subject";
  private static final String FROM_PROPERTY = "from";
  private static final String HOST_PROPERTY = "host";
  private static final String USER_PROPERTY = "user";
  private static final String PASSWORD_PROPERTY = "password";

  /* (non-Javadoc)
   * @see org.opencastproject.workflow.api.AbstractWorkflowOperationHandler#activate(org.osgi.service.component.ComponentContext)
   */
  @Override
  protected void activate(ComponentContext cc) {
    super.activate(cc);

    /* Custom values*/
    addConfigurationOption(TO_PROPERTY, "The mail address to send to");
    addConfigurationOption(SUBJECT_PROPERTY, "The subject line");

    /* From Appendix A of the Java Mail spec http://java.sun.com/products/javamail/JavaMail-1.2.pdf */
    addConfigurationOption(FROM_PROPERTY,
            "Specifies the return address of the current user."
                    + "Used by the InternetAddress.getLocalAddress method to specify the current user’s email address.");
    addConfigurationOption(HOST_PROPERTY,
            "Specifies the default Mail server."
                    + "The Store and Transport object’s connect methods use this property, if the protocol- specific host property is absent,"
                    + "to locate the target host.");    

  }


  /**
   * {@inheritDoc}
   * @see org.opencastproject.workflow.api.AbstractWorkflowOperationHandler#start(org.opencastproject.workflow.api.WorkflowInstance)
   */
  @Override
  public WorkflowOperationResult start(WorkflowInstance workflowInstance, JobContext context) throws WorkflowOperationException {
      
    // The current workflow operation instance
    WorkflowOperationInstance operation = workflowInstance.getCurrentOperation();
    // MediaPackage from previous workflow operations
    MediaPackage srcPackage = workflowInstance.getMediaPackage();
    // Lookup the name of the to, from, and subject
    String to = operation.getConfiguration(TO_PROPERTY);
    String from = operation.getConfiguration(FROM_PROPERTY);
    String subject = operation.getConfiguration(SUBJECT_PROPERTY);
    String host = operation.getConfiguration(HOST_PROPERTY);
    final String username = operation.getConfiguration(USER_PROPERTY);
    final String password = operation.getConfiguration(PASSWORD_PROPERTY);


    // Authenticator 
    Authenticator auth = new Authenticator() {
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
      }
    };

    // Set the body of the message to be the ID of the media package
    String body = srcPackage.getTitle() + "(" + srcPackage.getIdentifier().toString() + ")";

    Properties mailProps = new Properties();

    mailProps.put(MAIL_TRANSPORT_PROPERTY, "smtp");
    mailProps.put(MAIL_AUTH_PROPERTY, "true");
    mailProps.put("mail.smtp.starttls.enable", "true");
    mailProps.put("mail.smtp.host", "smtp.gmail.com");
    mailProps.put("mail.smtp.port", "587");

    mailProps.put(MAIL_FROM_PROPERTY, from);
    //mailProps.put(MAIL_HOST_PROPERTY, host);
    mailProps.put(MAIL_DEBUG_PROPERTY, "true");

    Enumeration names = mailProps.propertyNames();
    
    while (names.hasMoreElements()) { 
      String name = (String)names.nextElement();
      logger.info("{}: {}", name, mailProps.getProperty(name));
    }

    Session session = Session.getDefaultInstance(mailProps, auth);
    MimeMessage message = new MimeMessage(session);
    try {
      message.addRecipient(RecipientType.TO, new InternetAddress(to));
      message.setSubject(subject);
      message.setText(body);
      message.saveChanges();
      Transport.send(message);
    } catch (MessagingException e) {
      throw new WorkflowOperationException(e);
    } 

    // Return the source mediapackage and tell processing to continue
    return createResult(srcPackage, Action.CONTINUE);
  }
}

