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
package org.opencastproject.videoeditor.impl;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.gstreamer.Gst;
import org.opencastproject.videoeditor.api.MediaSegment;
import org.opencastproject.videoeditor.api.ProcessFailedException;
import org.opencastproject.videoeditor.api.SilenceDetectionService;
import org.opencastproject.videoeditor.gstreamer.silencedetector.GstreamerSilenceDetector;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wsmirnow
 */
public class SilenceDetectionServiceImpl implements SilenceDetectionService, ManagedService {

  /**
   * The logging instance
   */
  private static final Logger logger = LoggerFactory.getLogger(SilenceDetectionServiceImpl.class);
  private Properties properties = new Properties();
  
  private Map<String, GstreamerSilenceDetector> runningInstances = new Hashtable<String, GstreamerSilenceDetector>();
  
  @Override
  public List<MediaSegment> detect(String filePath) throws ProcessFailedException {
    try {
      GstreamerSilenceDetector silenceDetector = new GstreamerSilenceDetector(properties, filePath);
      runningInstances.put(filePath, silenceDetector);
      silenceDetector.detect();
      return silenceDetector.getMediaSegments();
//    } catch (PipelineBuildException ex) {
    } catch (Exception ex) {
      logger.error(ex.getMessage());
      throw new ProcessFailedException("An error happens by detecting silence segments!");
    } finally {
      if (runningInstances.containsKey(filePath))
        runningInstances.remove(filePath);
    }
  }
  
  /**
   * Interrupt running silence detection for given file.
   */
  @Override
  public void interruptDetection(String filePath) {
    GstreamerSilenceDetector silenceDetector = runningInstances.remove(filePath);
    silenceDetector.interruptDetection();
  }
  
  /**
   * Interrupt all running silence detections.
   */
  @Override
  public void interruptAllDetections() {
    for (String filePath : runningInstances.keySet()) {
      interruptDetection(filePath);
    }
  }

  @Override
  public void updated(Dictionary dctnr) throws ConfigurationException {
    this.properties.clear();
    Enumeration keys = properties.keys();
    while (keys.hasMoreElements()) {
      Object key = keys.nextElement();
      this.properties.put(key, properties.get(key));
    }
    logger.debug("Properties updated!");
  }
  
  protected void activate(ComponentContext context) {
    Gst.setUseDefaultContext(true);
    Gst.init();
  }

  protected void deactivate(ComponentContext context) {
    interruptAllDetections();
  }
}
