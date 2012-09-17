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

import java.util.List;

import org.opencastproject.job.api.JobContext;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.smil.api.SmilService;
import org.opencastproject.smil.entity.MediaElement;
import org.opencastproject.smil.entity.ParallelElement;
import org.opencastproject.videoeditor.api.MediaSegment;
import org.opencastproject.videoeditor.api.SilenceDetectionService;
import org.opencastproject.workflow.api.AbstractWorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.api.WorkflowService;
import org.opencastproject.workflow.api.WorkflowOperationResult.Action;
import org.opencastproject.workspace.api.Workspace;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * workflowoperationhandler for silencedetection executes the silencedetection and adds a SMIL
 * document to the mediapackage containing the cutting points
 */
public class SilenceDetectionWorkflowOperationHandler extends AbstractWorkflowOperationHandler {

  private static final Logger logger = LoggerFactory
      .getLogger(SilenceDetectionWorkflowOperationHandler.class);

  /**
   * the silence detection service
   */
  private SilenceDetectionService detetionService;
  /**
   * the smil service
   */
  private SmilService smilService;
  /**
   * the workflowService for retrieving the workflow
   */
  private WorkflowService workflowService;
  /**
   * the workspace
   */
  private Workspace workspace;

  private static final String SOURCE_FLAVOR = "source-flavor";

  @Override
  public WorkflowOperationResult start(WorkflowInstance workflowInstance, JobContext context)
      throws WorkflowOperationException {

    String flavor = workflowInstance.getCurrentOperation().getConfiguration(SOURCE_FLAVOR);
    MediaPackage mp = workflowInstance.getMediaPackage();
    Track[] tracks = mp.getTracks(MediaPackageElementFlavor.parseFlavor(flavor));
    logger.debug("number of source-tracks ", tracks.length);

    if (tracks.length > 0) {
      try {
        Track t = tracks[0];
        logger.info("Executing silence detection on track: {}", t.getURI());
        List<MediaSegment> segments = detetionService.detect(workspace.get(t.getURI())
            .getAbsolutePath());
        logger.debug("creating new SMIL");
        smilService.createNewSmil(workflowInstance.getId());
        mp = workflowService.getWorkflowById(workflowInstance.getId()).getMediaPackage();

        for (MediaSegment ms : segments) {
          // checking whether first item starts at 0
          if (segments.indexOf(ms) == 0 && ms.getSegmentStart() != 0) {
            logger.debug("adding new first item, because old first item doesn't start at 0");
            ParallelElement p = new ParallelElement();
            p.addElement(new MediaElement("", "0", String.valueOf(ms.getSegmentStart() / 1000.0)));
            smilService.addParallelElement(workflowInstance.getId(), p);
          }

          logger.debug("adding segment with  ({},{})", ms.getSegmentStart() / 1000.0,
              ms.getSegmentStop() / 1000.0);
          ParallelElement p = new ParallelElement();
          p.addElement(new MediaElement("", String.valueOf(ms.getSegmentStart() / 1000.0), String
              .valueOf(ms.getSegmentStop() / 1000.0)));
          smilService.addParallelElement(workflowInstance.getId(), p);

          // checking whether last item end at media duration
          if (segments.indexOf(ms) == segments.size() - 1
              && ms.getSegmentStop() != mp.getDuration()) {
            logger
                .debug("adding new last item, because old last item doesn't end with duration of media");
            p = new ParallelElement();
            p.addElement(new MediaElement("", String.valueOf(ms.getSegmentStop() / 1000.0), String
                .valueOf(mp.getDuration() / 1000.0)));
            smilService.addParallelElement(workflowInstance.getId(), p);
          }
        }
      } catch (Exception e) {
        logger.error(e.getMessage(), e);
        throw new WorkflowOperationException(e);
      }
    }

    return createResult(mp, Action.CONTINUE);
  }

  public void activate(ComponentContext cc) {
    super.activate(cc);
    logger.debug("Activating SilenceDetectionService");
  }

  public void setDetectionService(SilenceDetectionService detectionService) {
    this.detetionService = detectionService;
  }

  public void setSmilService(SmilService smilService) {
    this.smilService = smilService;
  }

  public void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

  public void setWorkflowService(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

}
