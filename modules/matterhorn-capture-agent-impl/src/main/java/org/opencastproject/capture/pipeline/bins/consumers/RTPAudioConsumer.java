package org.opencastproject.capture.pipeline.bins.consumers;

import org.opencastproject.capture.pipeline.bins.CaptureDevice;
import org.opencastproject.capture.pipeline.bins.CaptureDeviceNullPointerException;
import org.opencastproject.capture.pipeline.bins.GStreamerElementFactory;
import org.opencastproject.capture.pipeline.bins.GStreamerElements;
import org.opencastproject.capture.pipeline.bins.GStreamerProperties;
import org.opencastproject.capture.pipeline.bins.UnableToCreateElementException;
import org.opencastproject.capture.pipeline.bins.UnableToCreateGhostPadsForBinException;
import org.opencastproject.capture.pipeline.bins.UnableToLinkGStreamerElementsException;
import org.opencastproject.capture.pipeline.bins.UnableToSetElementPropertyBecauseElementWasNullException;

import org.gstreamer.Element;
import org.gstreamer.Pad;
import org.gstreamer.PadLinkReturn;

import java.util.Properties;

public class RTPAudioConsumer extends ConsumerBin {
  // Used to encode the audio stream for RTP transmitting.
  Element encoder;
  // Used to package the encoded audio into RTP packets
  Element rtpPayloader;
  // Bin used to control the RTP stream through RTCP and transmit the audio.
  Element gstRTPBin; 
  // Used to receive RTCP control signals
  Element rtcpUDPSrc;
  //Used to send RTCP control signals
  Element rtcpUDPSink;
  // Used to transmit the RTP packets
  Element rtpUDPSink;

  String hostName = "128.233.104.69";
  String rtpSendPort = "5002";
  String rtcpSendPort = "5003";
  String rtcpReceivePort = "5007";
  
  /**
   * Creates a consumer that takes in an audio source such as an alsa or pulse source and streams it using RTP. This is
   * based upon the example gstreamer pipeline given at
   * http://cgit.freedesktop.org/gstreamer/gst-plugins-good/tree/tests/examples/rtp/server-alsasrc-PCMA.c
   * 
   * @param captureDevice
   *          The details for the capture device including its location
   * @param properties
   *          The confidence monitoring properties
   * @throws UnableToLinkGStreamerElementsException
   *           Thrown if we cannot link the gstreamer elements together.
   * @throws UnableToCreateGhostPadsForBinException
   *           Thrown if we cannot create the ghost pads used to link this consumer to a producer
   * @throws UnableToSetElementPropertyBecauseElementWasNullException
   *           Thrown if we cannot set required properties on an gstreamer element.
   * @throws CaptureDeviceNullPointerException
   *           Thrown if there are no properties for a capture device
   * @throws UnableToCreateElementException
   *           Thrown if we cannot create a gstreamer element.
   */
  public RTPAudioConsumer(CaptureDevice captureDevice, Properties properties)
          throws UnableToLinkGStreamerElementsException, UnableToCreateGhostPadsForBinException,
          UnableToSetElementPropertyBecauseElementWasNullException, CaptureDeviceNullPointerException,
          UnableToCreateElementException {
    super(captureDevice, properties);
  }

  /* Creates all of the gstreamer elements required for audio RTP streaming.
   * @see org.opencastproject.capture.pipeline.bins.consumers.ConsumerBin#createElements()
   */
  @Override
  public void createElements() throws UnableToCreateElementException {
    createQueue();
    createEncoder();
    createRTPPayLoader();
    createRTPBin();
    createRTCPUDPSrc();
    createRTCPUDPSink();
    createRTPUDPSink();
  }

  
  /**
   * Create the queue used to connect the tee to this consumer.
   * 
   * @throws UnableToCreateElementException
   *           Thrown if the queue cannot be created.
   */
  private void createQueue() throws UnableToCreateElementException {
    queue = GStreamerElementFactory.getInstance().createElement(captureDevice.getFriendlyName(),
            GStreamerElements.QUEUE, captureDevice.getFriendlyName());
  }

  /**
   * Create the encoder to encode the audio stream before transmitting it over RTP
   * 
   * @throws UnableToCreateElementException
   *           Thrown if the encoder cannot be created
   */
  private void createEncoder() throws UnableToCreateElementException {
    // For the encoder we will use alawenc, A law 8 bit encoding usually used for voice communication but is really
    // light weight on processor and bandwidth.
    encoder = GStreamerElementFactory.getInstance().createElement(captureDevice.getFriendlyName(),
            GStreamerElements.ALAWENC, captureDevice.getFriendlyName());
  }

  /**
   * Create the rtp pay loader related to the encoder that changes the stream into RTP packets.
   * 
   * @throws UnableToCreateElementException
   *           Thrown if the rtp payloader cannot be created
   **/
  private void createRTPPayLoader() throws UnableToCreateElementException {
    rtpPayloader = GStreamerElementFactory.getInstance().createElement(captureDevice.getFriendlyName(),
            GStreamerElements.RTPPCMAPAY, captureDevice.getFriendlyName());
  }
  
  /**
   * Create the RTP Bin used to send the RTP packets, send and receive RTCP commands (stop, play, pause)
   * 
   * @throws UnableToCreateElementException
   */
  private void createRTPBin() throws UnableToCreateElementException {
    gstRTPBin = GStreamerElementFactory.getInstance().createElement(captureDevice.getFriendlyName(),
            GStreamerElements.GSTRTPBIN, captureDevice.getFriendlyName());
  }
  
  /**
   * Create a UDP source that RTCP through the gstrtpbin communicates plays, stops, pauses, etc.
   * 
   * @throws UnableToCreateElementException
   */
  private void createRTCPUDPSrc() throws UnableToCreateElementException {
    rtcpUDPSrc = GStreamerElementFactory.getInstance().createElement(captureDevice.getFriendlyName(),
            GStreamerElements.UDPSRC, captureDevice.getFriendlyName());
  }
  
  /**
   * Create a UDP sink that transmits the RTP packets and the results from RTCP commands.
   * 
   * @throws UnableToCreateElementException
   */
  private void createRTCPUDPSink() throws UnableToCreateElementException {
    rtcpUDPSink = GStreamerElementFactory.getInstance().createElement(captureDevice.getFriendlyName(),
            GStreamerElements.UDPSINK, captureDevice.getFriendlyName());
  }
  
  
  /**
   * Create a UDP sink that transmits the RTP packets and the results from RTCP commands.
   * 
   * @throws UnableToCreateElementException
   */
  private void createRTPUDPSink() throws UnableToCreateElementException {
    rtpUDPSink = GStreamerElementFactory.getInstance().createElement(captureDevice.getFriendlyName(),
            GStreamerElements.UDPSINK, captureDevice.getFriendlyName());
  }
  
  @Override
  public void setElementProperties() {
    setRTCPUDPSrcProperties();
    setRTCPUDPSinkProperties();
    setRTPUDPSinkProperties();
  }

  private void setRTCPUDPSrcProperties() {
    rtcpUDPSrc.set("port", rtcpReceivePort);
  }

  private void setRTCPUDPSinkProperties() {
    rtcpUDPSink.set("port", rtcpSendPort);
    rtcpUDPSink.set("host", hostName);
    rtcpUDPSink.set("async", "false");
    rtcpUDPSink.set("sync", "false");
  }

  private void setRTPUDPSinkProperties() {
    rtpUDPSink.set("port", rtcpSendPort);
    rtpUDPSink.set("host", hostName);
  }
  
  @Override
  public void linkElements() throws UnableToLinkGStreamerElementsException {
    Pad sinkPad, srcPad;
    PadLinkReturn padLinkReturn;
    encoder.link(rtpPayloader);
    
    // Link the Payloader to the Rtp Bin
    srcPad = rtpPayloader.getStaticPad(GStreamerProperties.SRC);
    sinkPad = gstRTPBin.getRequestPad("send_rtp_sink_0");
    padLinkReturn = srcPad.link(sinkPad);
    if(!PadLinkReturn.OK.equals(padLinkReturn))
      throw new UnableToLinkGStreamerElementsException(captureDevice, rtpPayloader, gstRTPBin);
    
    // Link the RTP Bin to the rtcpUDPSink
    srcPad = gstRTPBin.getRequestPad("send_rtcp_src_0");
    sinkPad = rtcpUDPSink.getStaticPad(GStreamerProperties.SINK);
    padLinkReturn = srcPad.link(sinkPad);
    if(!PadLinkReturn.OK.equals(padLinkReturn))
      throw new UnableToLinkGStreamerElementsException(captureDevice, gstRTPBin, rtcpUDPSink);
    
    // Link RTCP receive to the UDP src
    srcPad = rtcpUDPSink.getStaticPad("src");
    sinkPad = gstRTPBin.getRequestPad("recv_rtcp_sink_0");;
    padLinkReturn = srcPad.link(sinkPad);
    if(!PadLinkReturn.OK.equals(padLinkReturn))
      throw new UnableToLinkGStreamerElementsException(captureDevice, gstRTPBin, rtcpUDPSink);
    
    // Link RTP to send through rtpUDPSink
    srcPad = gstRTPBin.getStaticPad("send_rtp_src_0");
    sinkPad = rtpUDPSink.getStaticPad(GStreamerProperties.SINK);
    padLinkReturn = srcPad.link(sinkPad);
    if(!PadLinkReturn.OK.equals(padLinkReturn))
      throw new UnableToLinkGStreamerElementsException(captureDevice, gstRTPBin, rtpUDPSink);
  }
  
  @Override
  public Element getSrc() {
    return queue;
  }
}
