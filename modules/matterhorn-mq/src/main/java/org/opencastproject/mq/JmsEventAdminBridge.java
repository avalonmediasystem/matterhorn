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
package org.opencastproject.mq;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

/**
 * Listens for {@value #TOPICS} {@link Event}s, and places them on the JMS queue of the same topic. Also listens for
 * {@link Message}s on {@value #TOPICS} topics and published them to the {@link org.osgi.service.event.EventAdmin}
 * service.
 */
public class JmsEventAdminBridge implements EventHandler, MessageListener {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(JmsEventAdminBridge.class);

  /** The topics that this service will bridge between JMS and the OSGI Event Admin */
  public static final String TOPICS = "org/opencastproject/*";

  /** The key used to specify the origin of the event */
  public static final String SERVER_ID = "org.opencastproject.server.url";

  /** This server's base URL */
  protected String serverUrl = null;

  /** The JMS connection factory */
  protected ConnectionFactory jmsConnectionFactory = null;

  /** The JMS connection obtained from the factory */
  protected Connection jmsConnection = null;

  /**
   * Activates the component
   * 
   * @param cc
   *          the OSGI component context
   * @throws JMSException
   *           if there is a problem creating or starting the JMS connection
   */
  protected void activate(ComponentContext cc) throws JMSException {
    serverUrl = cc.getBundleContext().getProperty(SERVER_ID);
    jmsConnection = jmsConnectionFactory.createConnection();
    jmsConnection.start();

    // Register a JMS consumer
    Session session = jmsConnection.createSession(true, Session.SESSION_TRANSACTED);
    Destination destination = session.createTopic(TOPICS);
    MessageConsumer consumer = session.createConsumer(destination);
    consumer.setMessageListener(this);
  }

  /**
   * Deactivates the component
   * 
   * @throws JMSException
   *           if there is a problem closing the JMS connection
   */
  protected void deactivate() throws JMSException {
    if (jmsConnection != null) {
      jmsConnection.close();
    }
  }

  /**
   * Sets the JMS connection factory
   * 
   * @param connectionFactory
   *          the connection factory to set
   */
  public void setConnectionFactory(ConnectionFactory connectionFactory) {
    this.jmsConnectionFactory = connectionFactory;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
   */
  @Override
  public void handleEvent(Event event) {
    logger.info("Handling OSGI Event {}", event);
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
   */
  @Override
  public void onMessage(Message message) {
    logger.info("Handling JMS Message {}", message);
  }

}
