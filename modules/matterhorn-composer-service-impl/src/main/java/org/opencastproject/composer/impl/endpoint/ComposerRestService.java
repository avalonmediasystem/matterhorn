/**
 *  Copyright 2009 The Regents of the University of California
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
package org.opencastproject.composer.impl.endpoint;

import org.opencastproject.composer.api.ComposerService;
import org.opencastproject.composer.api.EncodingProfile;
import org.opencastproject.composer.impl.EncodingProfileImpl;
import org.opencastproject.composer.impl.endpoint.Receipt.STATUS;
import org.opencastproject.media.mediapackage.Attachment;
import org.opencastproject.media.mediapackage.MediaPackageImpl;
import org.opencastproject.util.DocUtil;
import org.opencastproject.util.UrlSupport;
import org.opencastproject.util.doc.DocRestData;
import org.opencastproject.util.doc.Format;
import org.opencastproject.util.doc.Param;
import org.opencastproject.util.doc.RestEndpoint;
import org.opencastproject.util.doc.RestTestForm;
import org.opencastproject.util.doc.Param.Type;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * A REST endpoint delegating functionality to the {@link ComposerService}
 */
@Path("/")
public class ComposerRestService {
  private static final Logger logger = LoggerFactory.getLogger(ComposerRestService.class);
  protected String docs;
  protected String serverUrl;
  protected ComposerService composerService;
  protected ComposerServiceDao dao;

  public void setComposerService(ComposerService composerService) {
    this.composerService = composerService;
  }

  public void setDao(ComposerServiceDao dao) {
    this.dao = dao;
  }

  public void activate(ComponentContext cc) {
    // Generate the docs, using the local server URL
    if (cc.getBundleContext().getProperty("serverUrl") == null) {
      serverUrl = UrlSupport.DEFAULT_BASE_URL;
    } else {
      serverUrl = cc.getBundleContext().getProperty("serverUrl");
    }
    docs = generateDocs();
  }

  protected void deactivate() {
  }

  /**
   * Encodes a track in a media package.
   * 
   * @param mediaPackage
   *          The MediaPackage referencing the media and metadata
   * @param audioSourceTrackId
   *          The ID of the audio source track in the media package to be encoded
   * @param videoSourceTrackId
   *          The ID of the video source track in the media package to be encoded
   * @param profileId
   *          The profile to use in encoding this track
   * @return The JAXB version of {@link Track} {@link TrackType}
   * @throws Exception
   */
  @POST
  @Path("encode")
  @Produces(MediaType.TEXT_XML)
  public Response encode(@FormParam("mediapackage") MediaPackageImpl mediaPackage,
          @FormParam("audioSourceTrackId") String audioSourceTrackId,
          @FormParam("videoSourceTrackId") String videoSourceTrackId, @FormParam("profileId") String profileId)
          throws Exception {
    // Ensure that the POST parameters are present
    if (mediaPackage == null || audioSourceTrackId == null || videoSourceTrackId == null || profileId == null) {
      return Response.status(Status.BAD_REQUEST).entity(
              "mediapackage, audioSourceTrackId, videoSourceTrackId, and profileId must not be null").build();
    }

    // Asynchronously encode the specified tracks
    String receipt;
    if (audioSourceTrackId == null) {
      receipt = composerService.encode(mediaPackage.toXml(), videoSourceTrackId, profileId);
    } else {
      receipt = composerService.encode(mediaPackage.toXml(), videoSourceTrackId, audioSourceTrackId, profileId);
    }
    if (receipt == null)
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Encoding failed").build();
    return Response.ok().entity(receipt).build();
  }

  /**
   * Encodes a track in a media package.
   * 
   * @param mediaPackage
   *          The MediaPackage referencing the media and metadata
   * @param audioSourceTrackId
   *          The ID of the audio source track in the media package to be encoded
   * @param sourceTrackId
   *          The ID of the video source track in the media package to be encoded
   * @param profileId
   *          The profile to use in encoding this track
   * @return A {@link Response} with the resulting {@link Track} in the response body
   * @throws Exception
   */
  @POST
  @Path("image")
  @Produces(MediaType.TEXT_XML)
  public Response image(@FormParam("mediapackage") MediaPackageImpl mediaPackage,
          @FormParam("sourceTrackId") String sourceTrackId, @FormParam("profileId") String profileId,
          @FormParam("time") long time) throws Exception {
    // Ensure that the POST parameters are present
    if (mediaPackage == null || sourceTrackId == null || profileId == null) {
      return Response.status(Status.BAD_REQUEST).entity("mediapackage, sourceTrackId, and profileId must not be null")
              .build();
    }

    // Asynchronously encode the specified tracks
    Receipt receipt = dao.createReceipt();
    receipt.setStatus(STATUS.RUNNING.toString());
    logger.debug("created receipt {}", receipt.getId());
    try {
      Attachment image = composerService.image(mediaPackage, sourceTrackId, profileId, time).get();
      return Response.ok().entity(documentToString(image)).build();
    } catch (Exception e) {
      logger.warn("Unable to extract image: " + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Receipt(null, STATUS.FAILED.toString()))
              .build();
    }
  }

  /**
   * @param image
   * @return
   */
  private String documentToString(Attachment image) {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      image.toManifest(doc, null);
      DOMSource domSource = new DOMSource();
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.transform(domSource, result);
      return writer.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @GET
  @Path("receipt/{id}.xml")
  @Produces(MediaType.TEXT_XML)
  public Response getReceipt(@PathParam("id") String id) {
    Receipt r = dao.getReceipt(id);
    if (r == null)
      return Response.status(Status.NOT_FOUND).entity("no receipt found with id " + id).build();
    return Response.ok().entity(r).build();
  }

  @GET
  @Path("profiles")
  @Produces(MediaType.TEXT_XML)
  public EncodingProfileList listProfiles() {
    List<EncodingProfileImpl> list = new ArrayList<EncodingProfileImpl>();
    for (EncodingProfile p : composerService.listProfiles()) {
      list.add((EncodingProfileImpl) p);
    }
    return new EncodingProfileList(list);
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("docs")
  public String getDocumentation() {
    return docs;
  }

  protected String generateDocs() {
    DocRestData data = new DocRestData("Composer", "Composer Service", "/composer/rest",
            new String[] { "$Rev$" });
    // profiles
    RestEndpoint profilesEndpoint = new RestEndpoint("profiles", RestEndpoint.Method.GET, "/profiles",
            "Retrieve the encoding profiles");
    profilesEndpoint.addStatus(org.opencastproject.util.doc.Status
            .OK("Results in an xml document describing the available encoding profiles"));
    profilesEndpoint.setTestForm(RestTestForm.auto());
    data.addEndpoint(RestEndpoint.Type.READ, profilesEndpoint);

    // receipt
    RestEndpoint receiptEndpoint = new RestEndpoint("receipt", RestEndpoint.Method.GET, "/receipt/{id}.xml",
            "Retrieve a receipt for an encoding task");
    receiptEndpoint
            .addStatus(org.opencastproject.util.doc.Status
                    .OK("Results in an xml document containing the status of the encoding job, and the track produced by this encoding job if it the task is finished"));
    receiptEndpoint.addPathParam(new Param("id", Param.Type.STRING, null, "the receipt id"));
    receiptEndpoint.addFormat(new Format("xml", null, null));
    receiptEndpoint.setTestForm(RestTestForm.auto());
    data.addEndpoint(RestEndpoint.Type.READ, receiptEndpoint);

    // encode
    RestEndpoint encodeEndpoint = new RestEndpoint(
            "encode",
            RestEndpoint.Method.POST,
            "/encode",
            "Starts an encoding process, based on the specified encoding profile ID, the media package, and the track ID in the media package to encode");
    encodeEndpoint.addStatus(org.opencastproject.util.doc.Status
            .OK("Results in an xml document containing the receipt for the encoding task"));
    encodeEndpoint.addRequiredParam(new Param("mediapackage", Type.TEXT, generateMediaPackage(),
            "The mediapackage containing the source tracks"));
    encodeEndpoint.addRequiredParam(new Param("audioSourceTrackId", Type.STRING, "track-1",
            "The track ID containing the audio stream"));
    encodeEndpoint.addRequiredParam(new Param("videoSourceTrackId", Type.STRING, "track-2",
            "The track ID containing the video stream"));
    encodeEndpoint.addRequiredParam(new Param("profileId", Type.STRING, "flash.http", "The encoding profile to use"));
    encodeEndpoint.setTestForm(RestTestForm.auto());
    data.addEndpoint(RestEndpoint.Type.WRITE, encodeEndpoint);

    // image
    RestEndpoint imageEndpoint = new RestEndpoint(
            "image",
            RestEndpoint.Method.POST,
            "/image",
            "Starts an image extraction process, based on the specified encoding profile ID, the media package, and the source track ID in the media package");
    imageEndpoint.addStatus(org.opencastproject.util.doc.Status
            .OK("Results in an xml document containing the image attachment"));
    imageEndpoint.addRequiredParam(new Param("mediapackage", Type.TEXT, generateMediaPackage(),
            "The mediapackage containing the source tracks"));
    imageEndpoint.addRequiredParam(new Param("time", Type.STRING, "1",
            "The number of seconds into the video to extract the image"));
    imageEndpoint.addRequiredParam(new Param("sourceTrackId", Type.STRING, "track-2",
            "The track ID containing the video stream"));
    imageEndpoint.addRequiredParam(new Param("profileId", Type.STRING, "engage-image.http",
            "The encoding profile to use"));
    imageEndpoint.setTestForm(RestTestForm.auto());
    data.addEndpoint(RestEndpoint.Type.WRITE, imageEndpoint);

    return DocUtil.generate(data);
  }

  protected String generateMediaPackage() {
    return "<ns2:mediapackage xmlns:ns2=\"http://mediapackage.opencastproject.org\" start=\"2007-12-05T13:40:00\" duration=\"1004400000\">\n"
            + "  <media>\n"
            + "    <track id=\"track-1\" type=\"presentation/source\">\n"
            + "      <mimetype>audio/mp3</mimetype>\n" + "      <url>"
            + serverUrl
            + "/workflow/samples/audio.mp3</url>\n"
            + "      <checksum type=\"md5\">950f9fa49caa8f1c5bbc36892f6fd062</checksum>\n"
            + "      <duration>10472</duration>\n"
            + "      <audio>\n"
            + "        <channels>2</channels>\n"
            + "        <bitdepth>0</bitdepth>\n"
            + "        <bitrate>128004.0</bitrate>\n"
            + "        <samplingrate>44100</samplingrate>\n"
            + "      </audio>\n"
            + "    </track>\n"
            + "    <track id=\"track-2\" type=\"presentation/source\">\n"
            + "      <mimetype>video/quicktime</mimetype>\n"
            + "      <url>"
            + serverUrl
            + "/workflow/samples/camera.mpg</url>\n"
            + "      <checksum type=\"md5\">43b7d843b02c4a429b2f547a4f230d31</checksum>\n"
            + "      <duration>14546</duration>\n"
            + "      <video>\n"
            + "        <device type=\"UFG03\" version=\"30112007\" vendor=\"Unigraf\" />\n"
            + "        <encoder type=\"H.264\" version=\"7.4\" vendor=\"Apple Inc\" />\n"
            + "        <resolution>640x480</resolution>\n"
            + "        <scanType type=\"progressive\" />\n"
            + "        <bitrate>540520</bitrate>\n"
            + "        <frameRate>2</frameRate>\n"
            + "      </video>\n"
            + "    </track>\n" + "  </media>\n" + "</ns2:mediapackage>";
  }
}
