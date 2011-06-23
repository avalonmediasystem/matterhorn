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

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.network.NetworkConnector;
import org.apache.commons.lang.StringUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import javax.jms.ConnectionFactory;

/**
 * Starts and stops an
 */
public class Activator implements BundleActivator {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(Activator.class);

  /** The ActiveMQ Broker */
  protected BrokerService broker = null;

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
   */
  @Override
  public void start(BundleContext context) throws Exception {
    String dataDir = new File(context.getProperty("org.opencastproject.storage.dir"), "mq").getAbsolutePath();
    broker = new BrokerService();
    broker.setDataDirectory(dataDir);
    broker.setBrokerName("mh");

    // Set the connection URL
    String url = "tcp://localhost:" + StringUtils.trimToEmpty(context.getProperty("org.opencastproject.mq.port"));
    logger.info("ActiveMQ accepting connections at {}", url);
    broker.addConnector(url);

    // Set up JMS federation, if necessary
    String federationUrl = StringUtils.trimToNull(context.getProperty("org.opencastproject.mq.discovery"));
    if (federationUrl != null) {
      logger.info("ActiveMQ connected to {}", federationUrl);
      NetworkConnector connector = broker.addNetworkConnector(federationUrl);
      connector.setDuplex(true);
    }

    // Start the broker
    broker.start();

    // Register the connection factory
    context.registerService(ConnectionFactory.class.getName(), new ActiveMqPooledConnectionFactory(url), null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
   */
  @Override
  public void stop(BundleContext context) throws Exception {
    try {
      if (broker != null && broker.isStarted()) {
        broker.stop();
      }
      broker = null;
    } catch (Exception e) {
      logger.info("Error Shutting down local message broker {} ", e.getMessage());
    }
  }
}
