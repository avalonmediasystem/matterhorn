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
package org.opencastproject.capture.pipeline;

import java.io.File;
import java.util.Properties;
import org.gstreamer.Gst;
import org.gstreamer.Pipeline;
import org.opencastproject.capture.CaptureParameters;
import org.opencastproject.capture.pipeline.bins.CaptureDevice;
import org.opencastproject.capture.pipeline.bins.CaptureDeviceBin;
import org.opencastproject.capture.pipeline.bins.producers.ProducerFactory;
import org.opencastproject.capture.pipeline.bins.producers.ProducerFactory.ProducerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author opencast
 */
public final class MonitoringGStreamerPipeline {

  static final Logger logger = LoggerFactory.getLogger(MonitoringGStreamerPipeline.class);
  
  protected MonitoringGStreamerPipeline() { }
  
  public static Pipeline create(Properties properties) throws CannotFindSourceFileOrDeviceException, UnrecognizedDeviceException {
    
    Gst.init();
    Pipeline monitoringPipeline = new Pipeline("Confidence-Monitoring");

    // get friendly names
    String[] friendlyNames;
    try {
      friendlyNames = GStreamerPipeline.getDeviceNames(properties);
    } catch (InvalidCaptureDevicesSpecifiedException e) {
      logger.error(e.getStackTrace().toString());
      return null;
    }
    
    // create CaptureDevice
    for (String deviceName : friendlyNames) {
      CaptureDevice captureDevice = createCaptureDevice(deviceName, properties);
      if (!addCaptureDeviceBinsToPipeline(captureDevice, properties, monitoringPipeline)) {
        logger.error("Failed to create pipeline for {}.", deviceName);
      }
    }
    
    monitoringPipeline.debugToDotFile(Pipeline.DEBUG_GRAPH_SHOW_ALL, monitoringPipeline.getName());
    return monitoringPipeline;
  }
  
  protected static CaptureDevice createCaptureDevice(String friendlyName, Properties properties) 
          throws CannotFindSourceFileOrDeviceException, UnrecognizedDeviceException {
    
    // Get properties from the configuration
    String srcProperty = CaptureParameters.CAPTURE_DEVICE_PREFIX + friendlyName + CaptureParameters.CAPTURE_DEVICE_SOURCE;
    String outputProperty = CaptureParameters.CAPTURE_DEVICE_PREFIX + friendlyName + CaptureParameters.CAPTURE_DEVICE_DEST;
    String typeProperty = CaptureParameters.CAPTURE_DEVICE_PREFIX + friendlyName + CaptureParameters.CAPTURE_DEVICE_TYPE;
    
    String srcLoc = properties.getProperty(srcProperty);
    String type = properties.getProperty(typeProperty);
    String outputLoc = properties.getProperty(outputProperty);
    
    ProducerType devName;
    if (type != null) {
      devName = ProducerType.valueOf(type);
      logger.debug("Device {} has type {}.", friendlyName, type);
      /** For certain devices we need to check to make sure that the src is specified, others are exempt. **/
      if (ProducerFactory.getInstance().requiresSrc(devName) && srcLoc == null) {  
        GStreamerPipeline.checkSrcLocationExists(friendlyName, srcLoc);
      }
    } else {
      GStreamerPipeline.checkSrcLocationExists(friendlyName, srcLoc);
      if (new File(srcLoc).isFile()) {
        // Non-V4L file. If it exists, assume it is ingestable
        // TODO: Fix security risk. Any file on CaptureAgent filesytem could be ingested
        devName = ProducerType.FILE;
        logger.debug("Device {} is a File device.", friendlyName);
      } else {
        devName = GStreamerPipeline.determineSourceFromJ4VLInfo(srcLoc);
      }
    }
    return new CaptureDevice(srcLoc, devName, friendlyName, outputLoc);
  }
  
  protected static boolean addCaptureDeviceBinsToPipeline(CaptureDevice captureDevice, Properties properties, Pipeline pipeline) {
    CaptureDeviceBin captureDeviceBin = null;
    try {
      captureDeviceBin = new CaptureDeviceBin(captureDevice, properties, true);
      return pipeline.add(captureDeviceBin.getBin());
    } catch (Exception e) {
      logger.error("Can not create CaptureDeviceBin: ", e);
    }
    return false;
  }
}
