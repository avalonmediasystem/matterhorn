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

import static java.lang.Boolean.TRUE;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

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

  /** The event property containing the JMS payload */
  public static final String PAYLOAD = "payload";

  /** The event property specifying that the event came from this bridge */
  public static final String BRIDGED = "bridged";

  /** This server's base URL */
  protected String serverUrl = null;

  /** The JMS connection factory */
  protected ConnectionFactory jmsConnectionFactory = null;

  /** The JMS connection obtained from the factory */
  protected Connection jmsConnection = null;

  /** The event admin service */
  protected EventAdmin eventAdmin = null;

  /**
   * Activates the component
   * 
   * @param cc
   *          the OSGI component context
   * @throws JMSException
   *           if there is a problem creating or starting the JMS connection
   */
  protected void activate(ComponentContext cc) {
    serverUrl = cc.getBundleContext().getProperty(SERVER_ID);
    try {
      jmsConnection = jmsConnectionFactory.createConnection();
      jmsConnection.start();

      // Register a JMS consumer
      Session session = jmsConnection.createSession(true, Session.SESSION_TRANSACTED);
      Destination destination = session.createTopic(TOPICS);
      MessageConsumer consumer = session.createConsumer(destination);
      consumer.setMessageListener(this);
    } catch (JMSException e) {
      throw new ComponentException("Unable to create a JMS connection", e);
    }
  }

  /**
   * Deactivates the component
   * 
   * @throws JMSException
   *           if there is a problem closing the JMS connection
   */
  protected void deactivate() {
    if (jmsConnection != null) {
      try {
        jmsConnection.close();
      } catch (JMSException e) {
        throw new ComponentException("Unable to close JMS connection", e);
      }
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
   * Sets teh EventAdmin service.
   * 
   * @param eventAdmin
   *          the eventAdmin to set
   */
  public void setEventAdmin(EventAdmin eventAdmin) {
    this.eventAdmin = eventAdmin;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
   */
  @Override
  public void handleEvent(Event event) {
    if (Boolean.TRUE.toString().equals(event.getProperty(BRIDGED))) {
      logger.debug("Skipping OSGI Event {}.  It has already been bridged", event);
      return;
    }
    logger.debug("Bridging OSGI Event {}", event);
    try {
      Session session = jmsConnection.createSession(true, Session.SESSION_TRANSACTED);
      String topicPath = event.getTopic();
      Topic topic = session.createTopic(topicPath);
      MessageProducer producer = session.createProducer(topic);
      TextMessage msg = session.createTextMessage();
      msg.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
      msg.setJMSType(topicPath);
      for (String name : event.getPropertyNames()) {
        Object obj = event.getProperty(name);
        if (obj instanceof String) {
          msg.setText((String) obj);
        } else {
          logger.warn("Only String event properties may be included in events for topic {}", event.getTopic());
        }
      }
      producer.send(msg);
    } catch (JMSException e) {
      logger.warn("Can not bridge {} to JMS", event);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
   */
  @Override
  public void onMessage(Message message) {
    logger.debug("Handling JMS Message {}", message);
    try {
      Dictionary<String, String> props = new Hashtable<String, String>();
      String topic = message.getJMSDestination().toString();
      if (message instanceof TextMessage) {
        props.put(BRIDGED, TRUE.toString());
        props.put(PAYLOAD, ((TextMessage) message).getText());
        eventAdmin.postEvent(new Event(topic, props));
      } else {
        logger.warn("Received a {} message.  Matterhorn deals with only TextMessages", message.getClass().getName());
      }
    } catch (JMSException e) {
      logger.warn("Unable to handle {}: {}", message, e.getMessage());
    }
  }

}
