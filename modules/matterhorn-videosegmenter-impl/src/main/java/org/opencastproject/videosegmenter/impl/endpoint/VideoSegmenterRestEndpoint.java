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
package org.opencastproject.videosegmenter.impl.endpoint;

import org.opencastproject.job.api.JobProducer;
import org.opencastproject.kernel.rest.AbstractJobProducerEndpoint;
import org.opencastproject.rest.RestConstants;
import org.opencastproject.serviceregistry.api.ServiceRegistry;
import org.opencastproject.util.DocUtil;
import org.opencastproject.util.doc.DocRestData;
import org.opencastproject.util.doc.Param;
import org.opencastproject.util.doc.Param.Type;
import org.opencastproject.util.doc.RestEndpoint;
import org.opencastproject.util.doc.RestTestForm;
import org.opencastproject.videosegmenter.api.VideoSegmenterService;

import org.osgi.service.component.ComponentContext;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The REST endpoint for the {@link VideoSegmenterService} service
 */
@Path("")
public class VideoSegmenterRestEndpoint extends AbstractJobProducerEndpoint {

  /** The rest docs */
  protected String docs;

  /** The video segmenter */
  protected VideoSegmenterService service;

  /** The service registry */
  protected ServiceRegistry serviceRegistry = null;

  /**
   * Callback from OSGi that is called when this service is activated.
   * 
   * @param cc
   *          OSGi component context
   */
  public void activate(ComponentContext cc) {
    String serviceUrl = (String) cc.getProperties().get(RestConstants.SERVICE_PATH_PROPERTY);
    docs = generateDocs(serviceUrl);
  }

  /**
   * Callback from the OSGi declarative services to set the service registry.
   * 
   * @param serviceRegistry
   *          the service registry
   */
  protected void setServiceRegistry(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  /**
   * Sets the segmenter
   * 
   * @param videoSegmenter
   *          the segmenter
   */
  protected void setVideoSegmenter(VideoSegmenterService videoSegmenter) {
    this.service = videoSegmenter;
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  @Path("docs")
  public String getDocs() {
    return docs;
  }

  protected String generateDocs(String serviceUrl) {
    DocRestData data = new DocRestData("videoSegmenter", "Video Segmentation Service", serviceUrl,
            new String[] { "$Rev$" });
    // analyze
    RestEndpoint analyzeEndpoint = new RestEndpoint("segment", RestEndpoint.Method.POST, "/",
            "Submit a track for segmentation");
    analyzeEndpoint.addStatus(org.opencastproject.util.doc.Status
            .ok("The job ID to use when polling for the resulting mpeg7 catalog"));
    analyzeEndpoint.addRequiredParam(new Param("track", Type.TEXT, "", "The track to segment."));
    analyzeEndpoint.setTestForm(RestTestForm.auto());
    data.addEndpoint(RestEndpoint.Type.WRITE, analyzeEndpoint);

    return DocUtil.generate(data);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.kernel.rest.AbstractJobProducerEndpoint#getService()
   */
  @Override
  public JobProducer getService() {
    if (service instanceof JobProducer)
      return (JobProducer) service;
    else
      return null;
  }

}
