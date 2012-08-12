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

import java.net.URI;
import java.util.Map;

import org.opencastproject.composer.api.ComposerService;
import org.opencastproject.job.api.Job;
import org.opencastproject.job.api.JobContext;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.mediapackage.MediaPackageElementParser;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.smil.api.SmilException;
import org.opencastproject.smil.api.SmilService;
import org.opencastproject.smil.entity.Smil;
import org.opencastproject.util.NotFoundException;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.api.WorkflowOperationResult.Action;
import org.opencastproject.workspace.api.Workspace;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoEditorWorkflowOperationHandler extends ResumableWorkflowOperationHandlerBase {

  private static final Logger logger = LoggerFactory
      .getLogger(VideoEditorWorkflowOperationHandler.class);

  /** Path to the hold ui resources */
  private static final String HOLD_UI_PATH = "/ui/operation/editor/index.html";

  /** Name of the configuration option that provides the source flavors we are looking for */
  private static final String SOURCE_FLAVOR_PROPERTY = "source-flavor";

  /** Name of the configuration option that provides the target flavors we will produce */
  private static final String TARGET_FLAVOR_SUBTYPE_PROPERTY = "target-flavor-subtype";

  /** Name of the configuration option that provides the encoding profile */
  private static final String ENCODING_PROFILE_PROPERTY = "encoding-profile";

  /** The composer service */
  private ComposerService composerService;

  /** The workspace */
  private Workspace workspace;

  private SmilService smilService;

  public void activate(ComponentContext cc) {
    super.activate(cc);
    setHoldActionTitle("Review / VideoEdit");
    registerHoldStateUserInterface(HOLD_UI_PATH);
    logger.info("Registering videoEditor hold state ui from classpath {}", HOLD_UI_PATH);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowOperationHandler#start(org.opencastproject.workflow.api.WorkflowInstance,
   *      JobContext)
   */
  @Override
  public WorkflowOperationResult start(WorkflowInstance workflowInstance, JobContext context)
      throws WorkflowOperationException {
    logger.info("Holding for video edit...");

    return createResult(Action.PAUSE);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.AbstractWorkflowOperationHandler#skip(org.opencastproject.workflow.api.WorkflowInstance,
   *      JobContext)
   */
  @Override
  public WorkflowOperationResult skip(WorkflowInstance workflowInstance, JobContext context)
      throws WorkflowOperationException {
    // If we do not hold for trim, we still need to put tracks in the mediapackage with the right
    // flavor
    MediaPackage mediaPackage = workflowInstance.getMediaPackage();
    WorkflowOperationInstance currentOperation = workflowInstance.getCurrentOperation();
    String configuredSourceFlavor = currentOperation.getConfiguration(SOURCE_FLAVOR_PROPERTY);
    String configuredTargetFlavorSubtype = currentOperation
        .getConfiguration(TARGET_FLAVOR_SUBTYPE_PROPERTY);
    MediaPackageElementFlavor matchingFlavor = MediaPackageElementFlavor
        .parseFlavor(configuredSourceFlavor);
    for (Track t : workflowInstance.getMediaPackage().getTracks()) {
      MediaPackageElementFlavor trackFlavor = t.getFlavor();
      if (trackFlavor != null && trackFlavor.matches(matchingFlavor)) {
        Track clonedTrack = (Track) t.clone();
        clonedTrack.setIdentifier(null);
        clonedTrack.setURI(t.getURI()); // use the same URI as the original
        clonedTrack.setFlavor(new MediaPackageElementFlavor(trackFlavor.getType(),
            configuredTargetFlavorSubtype));
        mediaPackage.addDerived(clonedTrack, t);
      }
    }
    return createResult(mediaPackage, Action.SKIP);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.ResumableWorkflowOperationHandler#resume(org.opencastproject.workflow.api.WorkflowInstance,
   *      JobContext, java.util.Map)
   */
  @Override
  public WorkflowOperationResult resume(WorkflowInstance workflowInstance, JobContext context,
                                        Map<String, String> properties)
      throws WorkflowOperationException {

    logger.info("VideoEditing workflow {} using ", workflowInstance.getId());

    try {
      Smil smil = smilService.getSmil(workflowInstance.getId());
    } catch (SmilException e) {
      throw new WorkflowOperationException("SMIL Exception", e);
    } catch (NotFoundException e) {
      throw new WorkflowOperationException("SMIL NOT FOUND", e);
    }

    // Get the source flavor to match
    WorkflowOperationInstance currentOperation = workflowInstance.getCurrentOperation();
    String configuredSourceFlavor = currentOperation.getConfiguration(SOURCE_FLAVOR_PROPERTY);
    String configuredTargetFlavorSubtype = currentOperation
        .getConfiguration(TARGET_FLAVOR_SUBTYPE_PROPERTY);
    MediaPackageElementFlavor matchingFlavor = MediaPackageElementFlavor
        .parseFlavor(configuredSourceFlavor);

    for (Track t : workflowInstance.getMediaPackage().getTracks()) {
      MediaPackageElementFlavor trackFlavor = t.getFlavor();
      if (trackFlavor != null && matchingFlavor.matches(trackFlavor)) {
        String profileId = currentOperation.getConfiguration(ENCODING_PROFILE_PROPERTY);

        logger.info("Trimming {} to ", new String[] { t.toString() });

        Track trimmedTrack = null;
        try {
          // Trim the track
          Job job  = null;// = composerService.trim(t, profileId, trimStart, trimDuration);
          if (!waitForStatus(job).isSuccess()) {
            throw new WorkflowOperationException("Trimming of " + t + " failed");
          }

          trimmedTrack = (Track) MediaPackageElementParser.getFromXml(job.getPayload());
          if (trimmedTrack == null) {
            throw new WorkflowOperationException("Trimming failed to produce a track");
          }

          // Put the new track in the mediapackage area of the workspace
          String fileName = getFileNameFromElements(t, trimmedTrack);
          URI uri = workspace.moveTo(trimmedTrack.getURI(), workflowInstance.getMediaPackage()
              .getIdentifier().compact(), trimmedTrack.getIdentifier(), fileName);
          trimmedTrack.setURI(uri);

          // Fix the flavor
          MediaPackageElementFlavor trimmedFlavor = new MediaPackageElementFlavor(t.getFlavor()
              .getType(), configuredTargetFlavorSubtype);
          trimmedTrack.setFlavor(trimmedFlavor);

          // Add the trimmed track to the mediapackage
          workflowInstance.getMediaPackage().addDerived(trimmedTrack, t);
        } catch (Exception e) {
          logger.warn("Unable to trim: {}", e);
          throw new WorkflowOperationException(e);
        }
      }
    }

    return super.resume(workflowInstance, context, properties);
  }

  public void setSmilService(SmilService smilService) {
    this.smilService = smilService;
  }

  /**
   * Sets the composer service.
   * 
   * @param composerService the composer service
   */
  public void setComposerService(ComposerService composerService) {
    this.composerService = composerService;
  }

  /**
   * Sets the workspace
   * 
   * @param workspace the workspace
   */
  public void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

}
