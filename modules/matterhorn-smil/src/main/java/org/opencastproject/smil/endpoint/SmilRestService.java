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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
  @Path("new.{format:xml|json}")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @RestQuery(
             description = "get a new SMIL document",
             name = "new",
             pathParameters = { @RestParameter(
                                               description = "The output format (json or xml) of the response body.",
                                               isRequired = true,
                                               name = "format",
                                               type = RestParameter.Type.STRING) },
             returnDescription = "returns a new SMIL",
             reponses = { @RestResponse(
                                        description = "SMIL was created successfully",
                                        responseCode = 200) })
  public Response getNew(@PathParam("format") String format) {
    if ("json".equals(format)) {
      return Response.ok(new Smil()).type(MediaType.APPLICATION_JSON).build();
    } else {
      return Response.ok(new Smil()).type(MediaType.APPLICATION_XML).build();
    }
  }

  protected void activate(ComponentContext cc) throws Exception {
    logger.info("starting SMIL REST service");
  }

}
