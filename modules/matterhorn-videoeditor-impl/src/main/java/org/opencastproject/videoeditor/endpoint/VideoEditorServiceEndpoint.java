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

package org.opencastproject.videoeditor.endpoint;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.opencastproject.job.api.JaxbJob;
import org.opencastproject.job.api.Job;
import org.opencastproject.job.api.JobProducer;
import org.opencastproject.rest.AbstractJobProducerEndpoint;
import org.opencastproject.serviceregistry.api.ServiceRegistry;
import org.opencastproject.smil.entity.Smil;
import org.opencastproject.util.doc.rest.RestParameter;
import org.opencastproject.util.doc.rest.RestQuery;
import org.opencastproject.util.doc.rest.RestResponse;
import org.opencastproject.util.doc.rest.RestService;
import org.opencastproject.videoeditor.api.VideoEditorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wsmirnow
 */
@Path("/")
@RestService(name = "VideoEditorServiceEndpoint", title = "Video Editor Service REST Endpoint",
        abstractText = "Process smil documents (trimm media files).", 
        notes = {"Video Editor Service consumes a smil document with media segments and creates an video." })
public class VideoEditorServiceEndpoint extends AbstractJobProducerEndpoint {

  private static final Logger logger = LoggerFactory.getLogger(VideoEditorServiceEndpoint.class);
  
  private ServiceRegistry serviceRegistry;
  private VideoEditorService videoEditorService;
  
  
  @POST
  @Path("/process-smil")
  @Produces({ MediaType.APPLICATION_XML })
  @RestQuery(name = "processsmil", description = "Create smil processing job.", 
          returnDescription = "Smil processing job.",
          restParameters = {
            @RestParameter(name = "smil", type = RestParameter.Type.TEXT,
              description = "Smil document to process.", isRequired = true)
          },
          reponses = {
            @RestResponse(description = "Smil processing job created successfully.", responseCode = 200),
            @RestResponse(description = "Internal server error.", responseCode = 500)
          })
  public Response processSmil(@QueryParam("smil") Smil smil) {
    try {
      Job job = videoEditorService.processSmil(smil);
      return Response.ok(new JaxbJob(job)).build();
    } catch (Exception ex) {
      return Response.serverError().entity(ex.getMessage()).build();
    }
  }
  
  @Override
  public JobProducer getService() {
    if (videoEditorService instanceof JobProducer)
      return (JobProducer) videoEditorService;
    else 
      return null;
  }

  @Override
  public ServiceRegistry getServiceRegistry() {
    return serviceRegistry;
  }

  public void setVideoEditorService(VideoEditorService videoEditorService) {
    this.videoEditorService = videoEditorService;
  }
  
  public void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }
}
