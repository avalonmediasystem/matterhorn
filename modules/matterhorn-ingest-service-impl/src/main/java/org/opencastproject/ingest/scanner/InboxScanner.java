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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * A Fileinstall artifact installer to watch a directory and schedule files placed in that directory for ingest.
 * Fileinstall takes care of handling files only once they are fully copied into the watch directory.
 * <p>
 * Note that the inbox scanner will not deal with hidden files or directories. Also, since it needs to remove the files
 * after ingest, it will not work on files that it has no write access to.
 */
public class InboxScanner implements ArtifactInstaller, ManagedService {

  /** The logger */
  protected static final Logger logger = LoggerFactory.getLogger(InboxScanner.class);

  /** Expected name of the root inbox directory */
  public static final String INBOX_DIRECTORY_NAME = "inbox";

  /** The configuration key to use for determining the user to run as for ingest */
  public static final String USER_NAME = "user.name";

  /** The configuration key to use for determining the workflow definition to use for ingest */
  public static final String WORKFLOW_DEFINITION = "workflow.definition";

  /** The configuration key to use for determining the properties used when starting the workflow */
  public static final String WORKFLOW_PROPERTIES = "workflow.properties";

  /** The configuration key to use for determining which path to watch */
  public static final String PATH = "inbox.path";

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

//  /** The user to run as during ingest */
//  protected User user = null;

  /** The workflow definition ID to use during ingest */
  protected String workflowDefinition = null;

  /** The path to watch relative to the inbox root directory */
  protected String path = null;

  /** The workflow properties */
  protected Map<String, String> workflowProperties = null;

  /**
   * Constructs an inbox scanner with the services it needs.
   * 
   * @param executorService
   *          the thread pool to use for handling sequential file ingest
   * @param ingestService
   *          the ingest service
   * @param workspace
   *          the workspace instance
   * @param securityService
   *          the security service
   * @param userDirectoryService
   *          the user directory to find the user that this inbox scanner is configured to run as
   */
  public InboxScanner(ExecutorService executorService, IngestService ingestService, Workspace workspace,
          SecurityService securityService) {
//  public InboxScanner(ExecutorService executorService, IngestService ingestService, Workspace workspace,
//          SecurityService securityService, UserDirectoryService userDirectoryService) {
    this.executorService = executorService;
    this.ingestService = ingestService;
    this.workspace = workspace;
    this.securityService = securityService;
//    this.userDirectoryService = userDirectoryService;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.felix.fileinstall.ArtifactInstaller#install(java.io.File)
   */
  public void install(File artifact) throws Exception {
    executorService.execute(getInstallRunnable(artifact));
  }

  /**
   * Method used to wrap an inbox scan inside its own thread.
   * 
   * @param artifact
   *          the artifact to scan
   * @return the runnable
   */
  protected Runnable getInstallRunnable(final File artifact) {
    return new Runnable() {
      public void run() {
        try {
//          securityService.setUser(user);
          boolean mediaPackageIngestSuccess = false;
          if ("zip".equals(FilenameUtils.getExtension(artifact.getName()))) {
            FileInputStream in = null;
            try {
              in = new FileInputStream(artifact);
              ingestService.addZippedMediaPackage(in, workflowDefinition, workflowProperties);
              logger.info("Ingested '{}' as a mediapackage", artifact.getAbsolutePath());
              mediaPackageIngestSuccess = true;
            } catch (Exception e) {
              logger.warn("Unable to ingest mediapackage '{}', {}", artifact.getAbsolutePath(), e);
            } finally {
              IOUtils.closeQuietly(in);
            }
          }

          if (!mediaPackageIngestSuccess) {
            FileInputStream in = null;
            try {
              in = new FileInputStream(artifact);
              workspace.putInCollection("inbox", artifact.getName(), in);
              logger.info("Ingested '{}' as an inbox file", artifact.getAbsolutePath());
            } catch (IOException e) {
              logger.warn("Unable to process inbox file '{}', {}", artifact.getAbsolutePath(), e);
            } finally {
              IOUtils.closeQuietly(in);
            }
          }

          try {
            FileUtils.forceDelete(artifact);
          } catch (IOException e) {
            logger.warn("Unable to delete file {}, {}", artifact.getAbsolutePath(), e);
          }
        } finally {
//          securityService.setUser(null);
        }
      }
    };
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.felix.fileinstall.ArtifactInstaller#uninstall(java.io.File)
   */
  public void uninstall(File artifact) throws Exception {
    // nothing to do
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.felix.fileinstall.ArtifactInstaller#update(java.io.File)
   */
  public void update(File artifact) throws Exception {
    // nothing to do
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.felix.fileinstall.ArtifactListener#canHandle(java.io.File)
   */
  public boolean canHandle(File artifact) {
    if (!artifact.isFile()) {
      logger.debug("Skipping inbox file {}", artifact);
      return false;
    } else if (artifact.isHidden()) {
      logger.debug("Skipping hidden inbox file {}", artifact);
      return false;
    } else if (!artifact.canRead()) {
      logger.debug("Skipping unreadable inbox file {}", artifact);
      return false;
    } else if (!artifact.canWrite()) {
      logger.debug("Skipping read-only inbox file {}", artifact);
      return false;
    }

    // Make sure the file is contained in the inbox (or subdirectories thereof)
    File parentFile = artifact.getParentFile();
    while (parentFile != null && !INBOX_DIRECTORY_NAME.equals(parentFile.getName())) {
      parentFile = parentFile.getParentFile();
    }
    if (parentFile == null) {
      logger.debug("Skipping file {} which is outside of inbox hierarchy", artifact);
      return false;
    }

    if (path == null)
      return INBOX_DIRECTORY_NAME.equals(artifact.getParentFile().getName());
    else
      return path.equals(artifact.getParentFile().getName());
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public void updated(Dictionary properties) throws ConfigurationException {
//    String userNameConfig = (String) properties.get(USER_NAME);
//    if (StringUtils.isNotBlank(userNameConfig)) {
//      user = userDirectoryService.loadUser(userNameConfig);
//    }
    String workflowConfig = (String) properties.get(WORKFLOW_DEFINITION);
    if (StringUtils.isNotBlank(workflowConfig)) {
      workflowDefinition = workflowConfig;
    }
    String path = StringUtils.trimToNull((String) properties.get(PATH));
    if (path != null && !"/".equals(path) && !".".equals(path)) {
      this.path = path;
    } else {
      this.path = null;
    }
    String propertiesText = StringUtils.trimToNull((String) properties.get(WORKFLOW_PROPERTIES));
    if (propertiesText != null) {
      this.workflowProperties = new HashMap<String, String>();
      String[] propertiesList = propertiesText.split(";");
      for (String property : propertiesList) {
        String[] propertyParts = property.trim().split("=");
        if (propertyParts.length != 2) {
          logger.warn("Skipping malformed inbox configuration property '{}'", property);
          continue;
        }
        this.workflowProperties.put(propertyParts[0], propertyParts[1]);
        logger.debug("Defining inbox workflow property '{}'", property);
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "inbox at " + ((path != null) ? path : "/");
  }

}
