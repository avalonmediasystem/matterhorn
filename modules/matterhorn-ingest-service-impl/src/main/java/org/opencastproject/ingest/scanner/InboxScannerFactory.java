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
package org.opencastproject.ingest.scanner;

import org.opencastproject.ingest.api.IngestService;
import org.opencastproject.security.api.SecurityService;
import org.opencastproject.workspace.api.Workspace;

import org.apache.felix.fileinstall.ArtifactInstaller;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A factory that will register artifact installers used to ingest files from a directory based on configurations of
 * type <code>org.opencastproject.ingest.scanner.InboxScanner</code>.
 */
public class InboxScannerFactory implements ManagedServiceFactory {

  /** The logger */
  protected static final Logger logger = LoggerFactory.getLogger(InboxScannerFactory.class);

  /** The workspace */
  protected Workspace workspace = null;

  /** The ingest service */
  protected IngestService ingestService = null;

  /** The local thread pool */
  protected ExecutorService executorService = null;

//  /** The user directory service */
//  protected UserDirectoryService userDirectoryService = null;

  /** The security service */
  protected SecurityService securityService = null;

  /** The OSGI bundle context, used for registering and unregistering managed inbox scanners */
  protected BundleContext bundleContext = null;

  /** The map of service PIDs to service registrations */
  protected Map<String, ServiceRegistration> scanners = null;

  /**
   * Sets the ingest service
   * 
   * @param ingestService
   *          the ingest service
   */
  public void setIngestService(IngestService ingestService) {
    this.ingestService = ingestService;
  }

  /**
   * Sets the workspace
   * 
   * @param workspace
   *          an instance of the workspace
   */
  public void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

  /**
   * Callback for setting the security service.
   * 
   * @param securityService
   *          the securityService to set
   */
  public void setSecurityService(SecurityService securityService) {
    this.securityService = securityService;
  }

//  /**
//   * Sets the user directory
//   * 
//   * @param userDirectoryService
//   *          the userDirectoryService to set
//   */
//  public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
//    this.userDirectoryService = userDirectoryService;
//  }

  protected void activate(ComponentContext cc) {
    int maxThreads = 1;
    if (cc != null && cc.getBundleContext().getProperty("org.opencastproject.inbox.threads") != null) {
      try {
        maxThreads = Integer.parseInt(cc.getBundleContext().getProperty("org.opencastproject.inbox.threads"));
      } catch (NumberFormatException e) {
        logger.warn("Illegal value set for org.opencastproject.inbox.threads. Using default value of 1 inbox ingest at a time.");
      }
    }
    this.executorService = Executors.newFixedThreadPool(maxThreads);
    this.bundleContext = cc.getBundleContext();
    this.scanners = new HashMap<String, ServiceRegistration>();

  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedServiceFactory#getName()
   */
  @Override
  public String getName() {
    return "org.opencastproject.ingest.scanner.InboxScanner";
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedServiceFactory#updated(java.lang.String, java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void updated(String pid, Dictionary properties) throws ConfigurationException {
    ServiceRegistration scannerServiceRegistration = scanners.get(pid);
    InboxScanner scanner = null;
    if (scannerServiceRegistration == null) {
//      scanner = new InboxScanner(executorService, ingestService, workspace, securityService, userDirectoryService);
      scanner = new InboxScanner(executorService, ingestService, workspace, securityService);
      ServiceRegistration reg = bundleContext.registerService(ArtifactInstaller.class.getName(), scanner, properties);
      scanners.put(pid, reg);
      logger.info("Registering new inbox '{}'", pid);
    } else {
      scanner = (InboxScanner) bundleContext.getService(scannerServiceRegistration.getReference());
      logger.info("Updating inbox '{}'", pid);
    }
    scanner.updated(properties);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedServiceFactory#deleted(java.lang.String)
   */
  @Override
  public void deleted(String pid) {
    ServiceRegistration reg = scanners.get(pid);
    if (reg != null) {
      reg.unregister();
      scanners.remove(pid);
      logger.info("Removing inbox '{}'", pid);
    }
  }

}
