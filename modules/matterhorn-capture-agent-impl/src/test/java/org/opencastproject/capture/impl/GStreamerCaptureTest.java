package org.opencastproject.capture.impl;

import org.opencastproject.capture.api.CaptureParameters;
import org.opencastproject.capture.pipeline.GStreamerPipeline;

import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

public class GStreamerCaptureTest {
  private GStreamerCaptureFramework gstreamerCapture;
  private RecordingImpl mockRecording;
  private CaptureFailureHandler captureFailureHandler;
  private ConfigurationManager configurationManager;
  private XProperties properties;
  private long timeout = GStreamerPipeline.DEFAULT_PIPELINE_SHUTDOWN_TIMEOUT;
  
  @Before
  public void setUp() {
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
    gstreamerCapture.start(mockRecording, captureFailureHandler);
  }
  
  @Test
  public void testStopWithoutStart() {
    gstreamerCapture.stop(timeout);
  }
}
