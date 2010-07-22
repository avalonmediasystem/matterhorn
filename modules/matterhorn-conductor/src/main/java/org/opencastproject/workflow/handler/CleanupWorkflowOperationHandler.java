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

import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElement;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.workflow.api.AbstractWorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowBuilder;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.api.WorkflowOperationResult.Action;
import org.opencastproject.workspace.api.Workspace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

/**
 * Removes all files in the working file repository for mediapackage elements that don't match one of the
 * "preserve-flavors" configuration value.
 */
public class CleanupWorkflowOperationHandler extends AbstractWorkflowOperationHandler {

  /** The logger */
  private static final Logger logger = LoggerFactory.getLogger(CleanupWorkflowOperationHandler.class);

  /** The element flavors to maintain in the original mediapackage.  All others will be removed */
  public static final String PRESERVE_FLAVOR_PROPERTY = "preserve-flavors";

  /** The configuration properties */
  protected SortedMap<String, String> configurationOptions = null;

  /**
   * The workspace to use in retrieving and storing files.
   */
  protected Workspace workspace;

  /** The thread pool */
  protected ExecutorService executorService;
  
  /** The default no-arg constructor builds the configuration options set */
  public CleanupWorkflowOperationHandler() {
    configurationOptions = new TreeMap<String, String>();
    configurationOptions.put(PRESERVE_FLAVOR_PROPERTY,
            "The configuration key that specifies the flavors to preserve.  If not specified, this operation will not" +
            "remove any files.");
  }
  
  /**
   * Sets the workspace to use.
   * 
   * @param workspace
   *          the workspace
   */
  public void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.AbstractWorkflowOperationHandler#start(org.opencastproject.workflow.api.WorkflowInstance)
   */
  @Override
  public WorkflowOperationResult start(WorkflowInstance workflowInstance) throws WorkflowOperationException {
    MediaPackage mediaPackage = workflowInstance.getMediaPackage();
    WorkflowOperationInstance currentOperation = workflowInstance.getCurrentOperation();

    String flavors = currentOperation.getConfiguration(PRESERVE_FLAVOR_PROPERTY);
    final List<MediaPackageElementFlavor> flavorsToPreserve = new ArrayList<MediaPackageElementFlavor>();

    // If the configuration does not specify flavors, keep them all
    if (flavors == null) {
      flavorsToPreserve.add(MediaPackageElementFlavor.parseFlavor("*/*"));
    } else {
      for(String flavor : asList(flavors)) {
        flavorsToPreserve.add(MediaPackageElementFlavor.parseFlavor(flavor));
      }
    }
    
    for (MediaPackageElement element : mediaPackage.getElements()) {
      // remove the element if it doesn't match the flavors to preserve
      boolean remove = true;
      if(element.getFlavor() == null) {
        // elements without flavors are kept, since this is a configuration error and the files should be kept to ease fixing the problem
        remove = false;
      } else {
        for(MediaPackageElementFlavor flavor : flavorsToPreserve) {
          if(flavor.matches(element.getFlavor())) {
            remove = false;
            break;
          }
        }
      }
      if(remove) {
        try {
          workspace.delete(mediaPackage.getIdentifier().toString(), element.getIdentifier());
        } catch(Exception e) {
          logger.info("Unable to delete the file for {}", element);
        }
        mediaPackage.remove(element);
      }
    }
    return WorkflowBuilder.getInstance().buildWorkflowOperationResult(mediaPackage, Action.CONTINUE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowOperationHandler#getConfigurationOptions()
   */
  @Override
  public SortedMap<String, String> getConfigurationOptions() {
    return configurationOptions;
  }
}