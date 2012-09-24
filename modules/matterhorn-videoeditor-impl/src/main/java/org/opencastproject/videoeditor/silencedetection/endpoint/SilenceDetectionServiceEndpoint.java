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

package org.opencastproject.videoeditor.silencedetection.endpoint;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.opencastproject.job.api.JaxbJob;
import org.opencastproject.job.api.Job;
import org.opencastproject.job.api.JobProducer;
import org.opencastproject.mediapackage.MediaPackageElementParser;
import org.opencastproject.mediapackage.Track;
import org.opencastproject.rest.AbstractJobProducerEndpoint;
import org.opencastproject.serviceregistry.api.ServiceRegistry;
import org.opencastproject.util.doc.rest.RestParameter;
import org.opencastproject.util.doc.rest.RestQuery;
import org.opencastproject.util.doc.rest.RestResponse;
import org.opencastproject.util.doc.rest.RestService;
import org.opencastproject.videoeditor.silencedetection.api.SilenceDetectionService;

/**
 *
 * @author wsmirnow
 */
@Path("/")
@RestService(name = "SilenceDetectionServiceEndpoint", title = "Silence Detection Service REST Endpoint",
        abstractText = "Detect silent sequences in audio file.", 
        notes = {"Detect silent sequences in audio file." })
public class SilenceDetectionServiceEndpoint extends AbstractJobProducerEndpoint {

  private SilenceDetectionService silenceDetectionService;
  private ServiceRegistry serviceRegistry;
  
  @POST
  @Path("/detect")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
  @RestQuery(name = "detect", description = "Create silence detection job.", 
          returnDescription = "Silence detection job.",
          restParameters = {
            @RestParameter(name = "track", type = RestParameter.Type.TEXT,
              description = "Track where to run silence detection.", isRequired = true),
            @RestParameter(name = "format", type = RestParameter.Type.STRING,
              description = "Output format.", isRequired = false, defaultValue = "xml")
          },
          reponses = {
            @RestResponse(description = "Silence detection job created successfully.", responseCode = 200),
            @RestResponse(description = "Internal server error.", responseCode = 500)
          })
  public Response detect(@QueryParam("track") String trackXml, @QueryParam("format") String format) {
    try {
      Track track = (Track) MediaPackageElementParser.getFromXml(trackXml);
      Job job = silenceDetectionService.detect(track);
      return getEntityResponse(job, format);
    } catch (Exception ex) {
      return Response.serverError().entity(ex.getMessage()).build();
    }
  }
  
  private Response getEntityResponse(Job job, String format) {
    if ("json".equals(format)) {
      return Response.ok(new JaxbJob(job)).type(MediaType.APPLICATION_JSON).build();
    } else if ("xml".equals(format)) {
      return Response.ok(new JaxbJob(job)).type(MediaType.APPLICATION_XML).build();
    }
    return Response.serverError().entity("Unknown response format! Please chose xml or json.").build();
  }
  
  @Override
  public JobProducer getService() {
    if (silenceDetectionService instanceof JobProducer) {
      return (JobProducer) silenceDetectionService;
    } else return null;
  }

  @Override
  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  public void setSilenceDetectionService(SilenceDetectionService silenceDetectionService) {
    this.silenceDetectionService = silenceDetectionService;
  }
  
  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
}
