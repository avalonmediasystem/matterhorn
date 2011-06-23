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
package org.opencastproject.mq.internal;

import org.apache.activemq.pool.PooledConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * The active MQ implementation of a JMS connection factory.
 */
public class ActiveMqPooledConnectionFactory implements ConnectionFactory {

  /** The connection factory to use as a delegate */
  private PooledConnectionFactory connectionFactory = null;

  public ActiveMqPooledConnectionFactory(String url) {
    connectionFactory = new PooledConnectionFactory(url);
  }

  /**
   * {@inheritDoc}
   *
   * @see javax.jms.ConnectionFactory#createConnection()
   */
  @Override
  public Connection createConnection() throws JMSException {
    return connectionFactory.createConnection();
  }
  
  /**
   * {@inheritDoc}
   *
   * @see javax.jms.ConnectionFactory#createConnection(java.lang.String, java.lang.String)
   */
  @Override
  public Connection createConnection(String userName, String password) throws JMSException {
    return connectionFactory.createConnection(userName, password);
  }
}
