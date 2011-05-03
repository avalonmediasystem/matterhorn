package org.opencastproject.capture.impl;

import org.opencastproject.capture.api.CaptureParameters;
import org.opencastproject.capture.pipeline.GStreamerPipeline;

import org.easymock.classextension.EasyMock;
import org.gstreamer.Gst;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GStreamerCaptureTest {
  private GStreamerCaptureFramework gstreamerCapture;
  private RecordingImpl mockRecording;
  private CaptureFailureHandler captureFailureHandler;
  private ConfigurationManager configurationManager;
  private XProperties properties;
  private long timeout = GStreamerPipeline.DEFAULT_PIPELINE_SHUTDOWN_TIMEOUT;
  /** True to run the tests */
  private static boolean gstreamerInstalled = true;

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(GStreamerCaptureTest.class);
  
  @Before
  public void setUp() {
    try {
      Gst.init();
    } catch (Throwable t) {
      logger.warn("Skipping agent tests due to unsatisifed gstreamer installation");
      gstreamerInstalled = false;
    }
    gstreamerCapture = new GStreamerCaptureFramework();
    mockRecording = EasyMock.createNiceMock(RecordingImpl.class);
    properties = new XProperties();
    properties.put(CaptureParameters.CAPTURE_DEVICE_NAMES, "vga, cam, mic");
    EasyMock.expect(mockRecording.getProperties()).andReturn(properties);
    EasyMock.replay(mockRecording);
    captureFailureHandler = EasyMock.createNiceMock(CaptureFailureHandler.class);
    configurationManager = EasyMock.createNiceMock(ConfigurationManager.class);
  }
  
  @Test
  public void testStartWithoutConfigurationManager() {
    if (!gstreamerInstalled)
      return;
    gstreamerCapture.start(mockRecording, captureFailureHandler);
  }
  
  @Test
  public void testStopWithoutStart() {
    if (!gstreamerInstalled)
      return;
    gstreamerCapture.stop(timeout);
  }
}
