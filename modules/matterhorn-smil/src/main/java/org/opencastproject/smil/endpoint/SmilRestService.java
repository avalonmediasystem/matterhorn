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

import javax.ws.rs.DefaultValue;
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
import org.opencastproject.smil.entity.Smil;
import org.opencastproject.util.NotFoundException;
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

  private static final Logger logger = LoggerFactory.getLogger(SmilRestService.class);

  private SmilService smilService;

  @GET
  @Path("testSmil")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @RestQuery(
             description = "get a test SMIL file",
             name = "testSmil",
             returnDescription = "returns a test SMIL",
             restParameters = { @RestParameter(
                                               description = "the return format",
                                               name = "format",
                                               isRequired = false,
                                               type = RestParameter.Type.STRING) },
             reponses = { @RestResponse(
                                        description = "SMIL was created successfully",
                                        responseCode = 200) })
  public Response getTestSmil(@QueryParam("format") @DefaultValue("xml") String format) {
    Smil smil = new Smil();
    
    ParallelElement p = new ParallelElement();
    smil.getBody().getSequence().addParallel(p);
    
    MediaElement m = new MediaElement();
    m.setClipBegin("123.123s");
    m.setClipEnd("321.312s");
    m.setType("video");
    m.setSrc("http://www.google.de/avi.avi");
    
    p.addElement(m);
    m = new MediaElement();
    m.setClipBegin("123.123s");
    m.setClipEnd("321.312s");
    m.setType("video");
    m.setSrc("http://www.google.de/avi.avi");
    p.addElement(m);
    
    p = new ParallelElement();
    smil.getBody().getSequence().addParallel(p);
    
    m = new MediaElement();
    m.setClipBegin("123.123s");
    m.setClipEnd("321.312s");
    m.setType("video");
    m.setSrc("http://www.google.de/avi.avi");
    
    p.addElement(m);
    m = new MediaElement();
    m.setClipBegin("123.123s");
    m.setClipEnd("321.312s");
    m.setType("video");
    m.setSrc("http://www.google.de/avi.avi");
    p.addElement(m);

    return getEntityResponse(smil, format);
  }

  @GET
  @Path("new")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @RestQuery(
             description = "get a new SMIL document",
             name = "new",
             restParameters = {
                 @RestParameter(
                                description = "The workflowId.",
                                isRequired = false,
                                name = "workflowId",
                                type = RestParameter.Type.STRING),
                 @RestParameter(
                                description = "the return format",
                                name = "format",
                                isRequired = false,
                                type = RestParameter.Type.STRING) },
             returnDescription = "returns a new SMIL",
             reponses = {
                 @RestResponse(description = "SMIL was created successfully", responseCode = 200),
                 @RestResponse(
                               description = "An error occurred while creating SMIL",
                               responseCode = 500),
                 @RestResponse(
                               description = "the SMIL document could not be found inside the worklfow",
                               responseCode = 404) })
  public Response getNew(@QueryParam("workflowId") long workflowId,
                         @QueryParam("format") @DefaultValue("xml") String format) {
    logger.debug("creating new Smil Document");
    Smil smil = null;
    try {
      smil = smilService.createNewSmil(workflowId);
    } catch (SmilException e) {
      logger.error("error while creating new SMIL Document");
      return Response.serverError().entity(buildUnexpectedErrorMessage(e)).build();
    } catch (NotFoundException e) {
      logger.error("could not find workflow {}", workflowId);
      return Response.status(404).entity(buildUnexpectedErrorMessage(e)).build();
    }
    return getEntityResponse(smil, format);
  }

  @GET
  @Path("get/{workflowId}")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @RestQuery(
             description = "get a SMIL document by workflowId",
             name = "get",
             pathParameters = { @RestParameter(
                                               description = "The workflowId.",
                                               isRequired = false,
                                               name = "workflowId",
                                               type = RestParameter.Type.STRING) },
             restParameters = { @RestParameter(
                                               description = "the return format",
                                               name = "format",
                                               isRequired = false,
                                               type = RestParameter.Type.STRING) },
             returnDescription = "returns the associated SMIL",
             reponses = {
                 @RestResponse(description = "SMIL was retrieved successfully", responseCode = 200),
                 @RestResponse(
                               description = "An error occurred while retrieving SMIL",
                               responseCode = 500) })
  public Response getSmil(@PathParam("workflowId") long workflowId,
                          @QueryParam("format") @DefaultValue("xml") String format) {
    logger.debug("retrieving SMIL document from workflow: {}", workflowId);
    Smil smil = null;
    try {
      smil = smilService.getSmil(workflowId);
    } catch (NotFoundException e) {
      logger.error("could not find SMIL");
      return Response.status(404).entity(buildUnexpectedErrorMessage(e)).build();
    } catch (SmilException e) {
      logger.error("error while retrieving SMIL");
      return Response.serverError().entity(buildUnexpectedErrorMessage(e)).build();
    }
    return getEntityResponse(smil, format);
  }

  @GET
  @Path("addParallel/{workflowId}")
  @Produces(MediaType.TEXT_PLAIN)
  @RestQuery(
             description = "add a parallel or sequence Element to given Element",
             name = "addParallel",
             pathParameters = { @RestParameter(
                                               description = "the elementId the element should be added to",
                                               isRequired = false,
                                               name = "workflowId",
                                               type = RestParameter.Type.STRING) },
             restParameters = { @RestParameter(
                                               description = "the return format",
                                               name = "format",
                                               isRequired = false,
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
                               @QueryParam("format") @DefaultValue("xml") String format) {
    ParallelElement p = new ParallelElement();
    try {
      smilService.addParallelElement(workflowId, p);
    } catch (NotFoundException e) {
      logger.error("could not find SMIL");
      return Response.status(404).entity(buildUnexpectedErrorMessage(e)).build();
    } catch (SmilException e) {
      logger.error("error while adding element to SMIL");
      return Response.serverError().entity(buildUnexpectedErrorMessage(e)).build();
    }
    return Response.ok(p.getId()).build();
  }

  @GET
  @Path("addMediaElement/{workflowId}/{elementId}")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @RestQuery(
             description = "adds a media element to the specified container element",
             name = "addMediaElement",
             returnDescription = "returns the edited SMIL document",
             pathParameters = {
                 @RestParameter(
                                description = "The workflowId.",
                                isRequired = false,
                                name = "workflowId",
                                type = RestParameter.Type.STRING),
                 @RestParameter(
                                description = "the elementId the element should be added to",
                                isRequired = false,
                                name = "elementId",
                                type = RestParameter.Type.STRING) },
             restParameters = {
                 @RestParameter(
                                description = "the type of the element",
                                isRequired = false,
                                name = "type",
                                type = RestParameter.Type.STRING),
                 @RestParameter(
                                description = "the return format",
                                name = "format",
                                isRequired = false,
                                type = RestParameter.Type.STRING),
                 @RestParameter(
                                description = "the begin of the element",
                                isRequired = false,
                                name = "clipBegin",
                                type = RestParameter.Type.STRING),
                 @RestParameter(
                                description = "the end of the element",
                                isRequired = false,
                                name = "clipEnd",
                                type = RestParameter.Type.STRING),
                 @RestParameter(
                                description = "the source of the element",
                                isRequired = false,
                                name = "src",
                                type = RestParameter.Type.STRING),
                 @RestParameter(
                                description = "the corrosponding matterhorn element to this element",
                                isRequired = false,
                                name = "mhElement",
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
                                  @QueryParam("mhElement") String mhElement,
                                  @QueryParam("format") @DefaultValue("xml") String format) {
    Smil smil = null;

    try {
      MediaElement m = new MediaElement();
      m.setType(type);
      m.setClipBegin(clipBegin);
      m.setClipEnd(clipEnd);
      m.setSrc(src);
      m.setMhElement(mhElement);
      smil = smilService.addMediaElement(workflowId, m, elementId);
    } catch (NotFoundException e) {
      logger.error("could not find SMIL");
      return Response.status(404).entity(buildUnexpectedErrorMessage(e)).build();
    } catch (SmilException e) {
      logger.error("error while adding media element to SMIL");
      return Response.serverError().entity(buildUnexpectedErrorMessage(e)).build();
    }

    return getEntityResponse(smil, format);
  }

  /**
   * 
   * @param smil
   * @param format
   * @return
   */
  private Response getEntityResponse(Smil smil, String format) {
    try {
      if ("json".equals(format)) {
        return Response.ok(smil).type(MediaType.APPLICATION_JSON).build();
      } else {
        return Response.ok(smil).type(MediaType.APPLICATION_XML).build();
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      return Response.serverError().entity(buildUnexpectedErrorMessage(e)).build();
    }
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
