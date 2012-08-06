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
package org.opencastproject.smil.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opencastproject.smil.api.SmilException;
import org.opencastproject.smil.api.SmilService;
import org.opencastproject.smil.entity.MediaElement;
import org.opencastproject.smil.entity.ParallelElement;
import org.opencastproject.smil.entity.SequenceElement;
import org.opencastproject.smil.entity.Smil;
import org.opencastproject.util.doc.rest.RestParameter;
import org.opencastproject.util.doc.rest.RestQuery;
import org.opencastproject.util.doc.rest.RestResponse;
import org.opencastproject.util.doc.rest.RestService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
@RestService(
             abstractText = "",
             name = "smil",
             notes = {
                 "All paths above are relative to the REST endpoint base (something like http://your.server/smil)",
                 "This Service doesn't manipulate data in the storage but simply changes the given XML data" },
             title = "SMIL RestService")
public class SmilRestService {

  private static final Logger logger = LoggerFactory
      .getLogger(SmilRestService.class);

  private static final String PARALLEL_TYPE = "parallel";
  private static final String SEQUENCE_TYPE = "sequence";

  private SmilService smilService;

  @GET
  @Path("testSmil")
  @Produces(MediaType.APPLICATION_XML)
  @RestQuery(
             description = "get a test SMIL file",
             name = "testSmil",
             returnDescription = "returns a test SMIL",
             reponses = { @RestResponse(
                                        description = "SMIL was created successfully",
                                        responseCode = 200) })
  public Response getTestSmil() {
    Smil smil = new Smil();

    SequenceElement s = new SequenceElement();

    MediaElement e = new MediaElement("test.mov", "0.0s", "1.35s");
    s.addElement(e);

    e = new MediaElement("test.mov", "2.43s", "5.543s");
    e.setMhElement("b190351e-8dfe-4596-9f1d-8417fae66596");
    s.addElement(e);

    e = new MediaElement("test.mov", "12.0s", "13.35s");
    s.addElement(e);

    ParallelElement p = new ParallelElement();

    p.addElement(s);

    smil.getBody().addElement(s);
    smil.getBody().addElement(p);

    return Response.ok(smil).build();
  }

  @GET
  @Path("new")
  @Produces(MediaType.APPLICATION_XML)
  @RestQuery(
             description = "get a new SMIL document",
             name = "new",
             restParameters = { @RestParameter(description = "The workflowId.",
                                               isRequired = false,
                                               name = "workflowId",
                                               type = RestParameter.Type.STRING) },
             returnDescription = "returns a new SMIL",
             reponses = {
                 @RestResponse(description = "SMIL was created successfully",
                               responseCode = 200),
                 @RestResponse(
                               description = "An error occurred while creating SMIL",
                               responseCode = 500) })
  public Response getNew(@QueryParam("workflowId") long workflowId) {
    logger.info("creating new Smil Document");
    Smil smil = null;
    try {
      smilService.createNewSmil(workflowId);
    } catch (SmilException e) {
      logger.error("error while creating new SMIL Document", e);
      return Response.serverError().entity(buildUnexpectedErrorMessage(e))
          .build();
    }
    return Response.ok(smil).build();
  }

  @GET
  @Path("get/{workflowId}")
  @Produces(MediaType.APPLICATION_XML)
  @RestQuery(
             description = "get a SMIL document by workflowId",
             name = "get",
             pathParameters = { @RestParameter(description = "The workflowId.",
                                               isRequired = false,
                                               name = "workflowId",
                                               type = RestParameter.Type.STRING) },
             returnDescription = "returns the associated SMIL",
             reponses = {
                 @RestResponse(description = "SMIL was retrieved successfully",
                               responseCode = 200),
                 @RestResponse(
                               description = "An error occurred while retrieving SMIL",
                               responseCode = 500) })
  public Response getSmil(@PathParam("workflowId") long workflowId) {
    Smil smil = null;
    try {
      smil = smilService.getSmil(workflowId);
    } catch (Exception e) {
      logger.error("error while retrieving SMIL", e);
      return Response.serverError().entity(buildUnexpectedErrorMessage(e))
          .build();
    }
    return Response.ok(smil).build();
  }

  @GET
  @Path("add/{type:parallel|sequence}/{workflowId}/{elementId}")
  @Produces(MediaType.APPLICATION_XML)
  @RestQuery(
             description = "add a parallel or sequence Element to given Element",
             name = "addElement",
             pathParameters = {
                 @RestParameter(description = "The workflowId.",
                                isRequired = false, name = "workflowId",
                                type = RestParameter.Type.STRING),
                 @RestParameter(
                                description = "the type of the ContainerElement",
                                isRequired = false, name = "type",
                                type = RestParameter.Type.STRING),
                 @RestParameter(
                                description = "the elementId the element should be added to",
                                isRequired = false, name = "elementId",
                                type = RestParameter.Type.STRING) },
             returnDescription = "returns the edited SMIL",
             reponses = {
                 @RestResponse(
                               description = "SMIL was manipulated successfully",
                               responseCode = 200),
                 @RestResponse(
                               description = "An error occurred while editing SMIL",
                               responseCode = 500) })
  public Response addElementTo(@PathParam("workflowId") long workflowId,
                               @PathParam("elementId") String elementId,
                               @PathParam("type") String type) {
    Smil smil = null;

    try {
      if (type.equals(PARALLEL_TYPE)) {
        smil = smilService.addElement(workflowId, new ParallelElement(),
            elementId);
      } else if (type.equals(SEQUENCE_TYPE)) {
        smil = smilService.addElement(workflowId, new SequenceElement(),
            elementId);
      } else {
        throw new SmilException("type " + type + " is not allowed");
      }
    } catch (Exception e) {
      logger.error("error while adding element SMIL", e);
      return Response.serverError().entity(buildUnexpectedErrorMessage(e))
          .build();
    }
    return Response.ok(smil).build();
  }

  @GET
  @Path("addMediaElement//{workflowId}/{elementId}")
  @Produces(MediaType.APPLICATION_XML)
  @RestQuery(
             description = "adds a media element to the specified container element",
             name = "addMediaElement",
             returnDescription = "returns the edited SMIL document",
             pathParameters = {
                 @RestParameter(description = "The workflowId.",
                                isRequired = false, name = "workflowId",
                                type = RestParameter.Type.STRING),
                 @RestParameter(
                                description = "the elementId the element should be added to",
                                isRequired = false, name = "elementId",
                                type = RestParameter.Type.STRING) },
             restParameters = {
                 @RestParameter(description = "the type of the element",
                                isRequired = false, name = "type",
                                type = RestParameter.Type.STRING),
                 @RestParameter(description = "the begin of the element",
                                isRequired = false, name = "clipBegin",
                                type = RestParameter.Type.STRING),
                 @RestParameter(description = "the end of the element",
                                isRequired = false, name = "clipEnd",
                                type = RestParameter.Type.STRING),
                 @RestParameter(description = "the source of the element",
                                isRequired = false, name = "src",
                                type = RestParameter.Type.STRING),
                 @RestParameter(
                                description = "the corrosponding matterhorn element to this element",
                                isRequired = false, name = "mhElement",
                                type = RestParameter.Type.STRING), },
             reponses = {
                 @RestResponse(
                               description = "SMIL was manipulated successfully",
                               responseCode = 200),
                 @RestResponse(
                               description = "An error occurred while editing SMIL",
                               responseCode = 500) })
  public Response addMediaElement(@PathParam("workflowId") long workflowId,
                                  @PathParam("elementId") String elementId,
                                  @QueryParam("type") String type,
                                  @QueryParam("clipBegin") String clipBegin,
                                  @QueryParam("clipEnd") String clipEnd,
                                  @QueryParam("src") String src,
                                  @QueryParam("mhElement") String mhElement) {
    Smil smil = null;

    try {
      MediaElement m = new MediaElement();
      m.setType(type);
      m.setClipBegin(clipBegin);
      m.setClipEnd(clipEnd);
      m.setSrc(src);
      m.setMhElement(mhElement);
      smil = smilService.addElement(workflowId, m, elementId);
    } catch (Exception e) {
      logger.error("error while adding media element");
      return Response.serverError().entity(buildUnexpectedErrorMessage(e))
          .build();
    }

    return Response.ok(smil).build();
  }

  /**
   * Builds an error message in case of an unexpected error in an endpoint method, includes the
   * exception type and message if existing.
   * 
   * TODO append stack trace
   * 
   * @param e Exception that was thrown
   * @return error message
   */
  private String buildUnexpectedErrorMessage(Exception e) {
    StringBuilder sb = new StringBuilder();
    sb.append("Unexpected error (").append(e.getClass().getName()).append(")");
    String message = e.getMessage();
    if (message != null && message.length() > 0) {
      sb.append(": ").append(message);
    }
    return sb.toString();
  }

  protected void activate(ComponentContext cc) throws Exception {
    logger.info("starting SMIL REST service");
  }

  protected void setSmilService(SmilService smilService) {
    this.smilService = smilService;
  }

}
