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
package org.opencastproject.episode.endpoint;

import org.apache.commons.lang.StringUtils;
import org.opencastproject.episode.api.EpisodeService;
import org.opencastproject.episode.api.EpisodeServiceException;
import org.opencastproject.episode.api.EpisodeQuery;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.mediapackage.MediaPackageImpl;
import org.opencastproject.security.api.UnauthorizedException;
import org.opencastproject.util.NotFoundException;
import org.opencastproject.util.doc.rest.RestParameter;
import org.opencastproject.util.doc.rest.RestQuery;
import org.opencastproject.util.doc.rest.RestResponse;
import org.opencastproject.util.doc.rest.RestService;
import org.opencastproject.workflow.api.WorkflowDefinition;
import org.opencastproject.workflow.api.WorkflowParser;
import org.opencastproject.workflow.api.WorkflowParsingException;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.util.ArrayList;
import java.util.List;

/**
 * The REST endpoint
 */
@Path("/")
@RestService(name = "episode", title = "Episode Service",
    notes = {
        "All paths are relative to the REST endpoint base (something like http://your.server/files)",
        "If you notice that this service is not working as expected, there might be a bug! "
            + "You should file an error report with your server logs from the time when the error occurred: "
            + "<a href=\"http://opencast.jira.com\">Opencast Issue Tracker</a>"
    },
    abstractText = "This service indexes and queries available (distributed) episodes.")
public class EpisodeRestService {

  private static final Logger logger = LoggerFactory.getLogger(EpisodeRestService.class);

  protected EpisodeService episodeService;

  /**
   * Callback from OSGi that is called when this service is activated.
   *
   * @param cc OSGi component context
   */

  public void activate(ComponentContext cc) {
    // String serviceUrl = (String) cc.getProperties().get(RestConstants.SERVICE_PATH_PROPERTY);
  }

  public void setEpisodeService(EpisodeService episodeService) {
    this.episodeService = episodeService;
  }

  public String getSampleMediaPackage() {
    return "<ns2:mediapackage xmlns:ns2=\"http://mediapackage.opencastproject.org\" start=\"2007-12-05T13:40:00\" duration=\"1004400000\"><title>t1</title>\n"
        + "  <metadata>\n"
        + "    <catalog id=\"catalog-1\" type=\"dublincore/episode\">\n"
        + "      <mimetype>text/xml</mimetype>\n"
        + "      <url>https://opencast.jira.com/svn/MH/trunk/modules/matterhorn-kernel/src/test/resources/dublincore.xml</url>\n"
        + "      <checksum type=\"md5\">2b8a52878c536e64e20e309b5d7c1070</checksum>\n"
        + "    </catalog>\n"
        + "    <catalog id=\"catalog-3\" type=\"metadata/mpeg-7\" ref=\"track:track-1\">\n"
        + "      <mimetype>text/xml</mimetype>\n"
        + "      <url>https://opencast.jira.com/svn/MH/trunk/modules/matterhorn-kernel/src/test/resources/mpeg7.xml</url>\n"
        + "      <checksum type=\"md5\">2b8a52878c536e64e20e309b5d7c1070</checksum>\n"
        + "    </catalog>\n"
        + "  </metadata>\n" + "</ns2:mediapackage>";
  }

  @POST
  @Path("add")
  @RestQuery(name = "add", description = "Adds a mediapackage to the episode service.",
      restParameters = {
          @RestParameter(description = "The media package to add to the search index.",
              isRequired = true, name = "mediapackage", type = RestParameter.Type.TEXT, defaultValue = "${this.sampleMediaPackage}")
      },
      reponses = {
          @RestResponse(description = "The mediapackage was added, no content to return.", responseCode = HttpServletResponse.SC_NO_CONTENT),
          @RestResponse(description = "There has been an internal error and the mediapackage could not be added", responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
      },
      returnDescription = "No content is returned.")
  public Response add(@FormParam("mediapackage") MediaPackageImpl mediaPackage) throws EpisodeServiceException {
    try {
      episodeService.add(mediaPackage);
      return Response.noContent().build();
    } catch (Exception e) {
      throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
    }
  }

  @DELETE
  @Path("delete")
  @RestQuery(name = "remove", description = "Remove an episode from the archive.",
      restParameters = {
          @RestParameter(description = "The media package ID to remove from the archive.",
              isRequired = true, name = "id", type = RestParameter.Type.STRING)
      },
      reponses = {
          @RestResponse(description = "The mediapackage was removed, no content to return.", responseCode = HttpServletResponse.SC_NO_CONTENT),
          @RestResponse(description = "There has been an internal error and the mediapackage could not be deleted", responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
      },
      returnDescription = "No content is returned.")
  public Response delete(@FormParam("id") String mediaPackageId) throws EpisodeServiceException, NotFoundException {
    try {
      if (mediaPackageId != null && episodeService.delete(mediaPackageId))
        return Response.noContent().build();
      else
        throw new NotFoundException();
    } catch (Exception e) {
      throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
    }
  }

  @POST
  @Path("lock")
  @RestQuery(name = "lock", description = "Flag a mediapackage as locked.",
      restParameters = {
          @RestParameter(description = "The media package to lock.",
              isRequired = true, name = "id", type = RestParameter.Type.TEXT, defaultValue = "${this.sampleMediaPackage}")
      },
      reponses = {
          @RestResponse(description = "The mediapackage was locked, no content to return.", responseCode = HttpServletResponse.SC_NO_CONTENT),
          @RestResponse(description = "There has been an internal error and the mediapackage could not be locked", responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
      },
      returnDescription = "No content is returned.")
  public Response lock(@FormParam("id") String mediaPackageId) {
    try {
      if (mediaPackageId != null && episodeService.lock(mediaPackageId, true))
        return Response.noContent().build();
      else
        throw new NotFoundException();
    } catch (Exception e) {
      throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
    }
  }

  @POST
  @Path("unlock")
  @RestQuery(name = "unlock", description = "Flag a mediapackage as unlocked.",
      restParameters = {
          @RestParameter(description = "The media package to unlock.",
              isRequired = true, name = "id", type = RestParameter.Type.TEXT, defaultValue = "${this.sampleMediaPackage}")
      },
      reponses = {
          @RestResponse(description = "The mediapackage was unlocked, no content to return.", responseCode = HttpServletResponse.SC_NO_CONTENT),
          @RestResponse(description = "There has been an internal error and the mediapackage could not be locked", responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
      },
      returnDescription = "No content is returned.")
  public Response unlock(@FormParam("id") String mediaPackageId) {
    try {
      if (mediaPackageId != null && episodeService.lock(mediaPackageId, false))
        return Response.noContent().build();
      else
        throw new NotFoundException();
    } catch (Exception e) {
      throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
    }
  }

  @POST
  @Path("applyworkflow")
  @RestQuery(name = "applyworkflow", description = "Apply a workflow to a list of media packages. Choose to either provide "
      + "a workflow definition or a workflow definition identifier.",
      restParameters = {
          @RestParameter(description = "The workflow definition in XML format.",
              isRequired = false, name = "definition", type = RestParameter.Type.TEXT),
          @RestParameter(description = "The workflow definition ID.",
              isRequired = false, name = "definition", type = RestParameter.Type.TEXT),
          @RestParameter(description = "A list of media package ids.",
              isRequired = true, name = "id", type = RestParameter.Type.STRING)
      },
      reponses = {
          @RestResponse(description = "The workflows have been started.", responseCode = HttpServletResponse.SC_NO_CONTENT)
      },
      returnDescription = "No content is returned.")
  public Response applyWorkflow(@FormParam("definition") String workflowDefinitionXml,
                                @FormParam("definitionId") String workflowDefinitionId,
                                @FormParam("id") List<String> mediaPackageId) throws UnauthorizedException {
    if (mediaPackageId == null || mediaPackageId.size() == 0)
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    boolean workflowDefinitionXmlPresent = StringUtils.isNotBlank(workflowDefinitionXml);
    boolean workflowDefinitionIdPresent = StringUtils.isNotBlank(workflowDefinitionId);
    if (!(workflowDefinitionXmlPresent ^ workflowDefinitionIdPresent))
      throw new WebApplicationException(Response.Status.BAD_REQUEST);
    if (workflowDefinitionXmlPresent) {
    WorkflowDefinition workflowDefinition;
    try {
      workflowDefinition = WorkflowParser.parseWorkflowDefinition(workflowDefinitionXml);
    } catch (WorkflowParsingException e) {
      throw new WebApplicationException(e);
    }
    episodeService.applyWorkflow(workflowDefinition, mediaPackageId);
    return Response.noContent().build();
    } else {
      episodeService.applyWorkflow(workflowDefinitionId, mediaPackageId);
      return Response.noContent().build();
    }
  }

  @GET
  @Path("series.{format:xml|json}")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @RestQuery(name = "series", description = "Search for series matching the query parameters.",
      pathParameters = {
          @RestParameter(description = "The output format (json or xml) of the response body.", isRequired = true, name = "format", type = RestParameter.Type.STRING)
      },
      restParameters = {
          @RestParameter(description = "The series ID. If the additional boolean parameter \"episodes\" is \"true\", "
              + "the result set will include this series episodes.", isRequired = false, name = "id", type = RestParameter.Type.STRING),
          @RestParameter(description = "Any series that matches this free-text query. If the additional boolean parameter \"episodes\" is \"true\", "
              + "the result set will include this series episodes.", isRequired = false, name = "q", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "false", description = "Whether to include this series episodes. This can be used in combination with \"id\" or \"q\".", isRequired = false, name = "episodes", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "false", description = "Whether to include this series information itself. This can be used in combination with \"id\" or \"q\".", isRequired = false, name = "series", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "0", description = "The maximum number of items to return per page.", isRequired = false, name = "limit", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "0", description = "The page number.", isRequired = false, name = "offset", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "false", description = "Whether this is an administrative query", isRequired = false, name = "admin", type = RestParameter.Type.STRING)
      },
      reponses = {
          @RestResponse(description = "The request was processed succesfully.", responseCode = HttpServletResponse.SC_OK)
      },
      returnDescription = "The search results, expressed as xml or json.")
  public Response getEpisodeAndSeriesById(@QueryParam("id") String id, @QueryParam("q") String text,
                                          @QueryParam("episodes") boolean includeEpisodes, @QueryParam("series") boolean includeSeries,
                                          @QueryParam("limit") int limit, @QueryParam("offset") int offset, @PathParam("format") String format) {

    EpisodeQuery query = new EpisodeQuery();

    // If id is specified, do a search based on id
    if (!StringUtils.isBlank(id)) {
      query.withId(id);
    }

    // Include series data in the results?
    query.includeSeries(includeSeries);

    // Include episodes in the result?
    query.includeEpisodes(includeEpisodes);

    // Include free-text search?
    if (!StringUtils.isBlank(text)) {
      query.withText(text);
    }

    query.withPublicationDateSort(true);
    query.withLimit(limit);
    query.withOffset(offset);

    // Return the right format
    if ("json".equals(format))
      return Response.ok(episodeService.getByQuery(query)).type(MediaType.APPLICATION_JSON).build();
    else
      return Response.ok(episodeService.getByQuery(query)).type(MediaType.APPLICATION_XML).build();
  }

  @GET
  @Path("episode.{format:xml|json}")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @RestQuery(name = "episodes", description = "Search for episodes matching the query parameters.",
      pathParameters = {
          @RestParameter(description = "The output format (json or xml) of the response body.", isRequired = true, name = "format", type = RestParameter.Type.STRING)
      },
      restParameters = {
          @RestParameter(description = "The ID of the single episode to be returned, if it exists.", isRequired = false, name = "id", type = RestParameter.Type.STRING),
          @RestParameter(description = "Any episode that matches this free-text query.", isRequired = false, name = "q", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "false", description = "Whether to include this series episodes. This can be used in combination with \"id\" or \"q\".", isRequired = false, name = "episodes", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "0", description = "The maximum number of items to return per page.", isRequired = false, name = "limit", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "0", description = "The page number.", isRequired = false, name = "offset", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "false", description = "Whether this is an administrative query", isRequired = false, name = "admin", type = RestParameter.Type.STRING)
      },
      reponses = {
          @RestResponse(description = "The request was processed succesfully.", responseCode = HttpServletResponse.SC_OK)
      },
      returnDescription = "The search results, expressed as xml or json.")
  public Response getEpisode(@QueryParam("id") String id, @QueryParam("q") String text,
                             @QueryParam("tag") String[] tags, @QueryParam("flavor") String[] flavors, @QueryParam("limit") int limit,
                             @QueryParam("offset") int offset, @PathParam("format") String format) {

    // Prepare the flavors
    List<MediaPackageElementFlavor> flavorSet = new ArrayList<MediaPackageElementFlavor>();
    if (flavors != null) {
      for (String f : flavors) {
        try {
          flavorSet.add(MediaPackageElementFlavor.parseFlavor(f));
        } catch (IllegalArgumentException e) {
          logger.debug("invalid flavor '{}' specified in query", f);
        }
      }
    }

    EpisodeQuery search = new EpisodeQuery();
    search.withId(id).withElementFlavors(flavorSet.toArray(new MediaPackageElementFlavor[flavorSet.size()]))
        .withElementTags(tags).withLimit(limit).withOffset(offset);
    if (!StringUtils.isBlank(text))
      search.withText(text);
    else
      search.withPublicationDateSort(true);

    // Return the results using the requested format
    if ("json".equals(format))
      return Response.ok(episodeService.getByQuery(search)).type(MediaType.APPLICATION_JSON).build();
    else
      return Response.ok(episodeService.getByQuery(search)).type(MediaType.APPLICATION_XML).build();
  }

  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @RestQuery(name = "episodesAndSeries", description = "Search for episodes and series matching the query parameters.",
      restParameters = {
          @RestParameter(description = "The output format (json or xml) of the response body.", isRequired = false, name = "format", type = RestParameter.Type.STRING),
          @RestParameter(description = "Any episode or series that matches this free-text query.", isRequired = false, name = "q", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "0", description = "The maximum number of items to return per page.", isRequired = false, name = "limit", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "0", description = "The page number.", isRequired = false, name = "offset", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "false", description = "Whether this is an administrative query", isRequired = false, name = "admin", type = RestParameter.Type.STRING)
      },
      reponses = {
          @RestResponse(description = "The request was processed succesfully.", responseCode = HttpServletResponse.SC_OK),
          @RestResponse(description = "Wrong output format specified.", responseCode = HttpServletResponse.SC_NOT_ACCEPTABLE)
      },
      returnDescription = "The search results, expressed as xml or json.")
  public Response getEpisodesAndSeries(@QueryParam("q") String text, @QueryParam("limit") int limit,
                                       @QueryParam("offset") int offset, @QueryParam("format") String format, @QueryParam("admin") boolean admin)
      throws EpisodeServiceException, UnauthorizedException {

    // format may be null or empty (not specified), or 'json' or 'xml'
    if ((format == null) || format.matches("(json|xml)?")) {
      EpisodeQuery query = new EpisodeQuery();
      query.includeEpisodes(true);
      query.includeSeries(true);
      query.withLimit(limit);
      query.withOffset(offset);
      if (!StringUtils.isBlank(text))
        query.withText(text);
      else
        query.withPublicationDateSort(true);

      // Build the response
      ResponseBuilder rb = Response.ok();

      if (admin) {
        rb.entity(episodeService.getForAdministrativeRead(query)).type(MediaType.APPLICATION_JSON);
      } else {
        rb.entity(episodeService.getByQuery(query)).type(MediaType.APPLICATION_JSON);
      }

      if ("json".equals(format)) {
        rb.type(MediaType.APPLICATION_JSON);
      } else {
        rb.type(MediaType.TEXT_XML);
      }

      return rb.build();
    }

    return Response.status(Response.Status.NOT_ACCEPTABLE).build();
  }

  @GET
  @Path("lucene.{format:xml|json}")
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  @RestQuery(name = "lucene", description = "Search a lucene query.",
      pathParameters = {
          @RestParameter(description = "The output format (json or xml) of the response body.", isRequired = true, name = "format", type = RestParameter.Type.STRING)
      },
      restParameters = {
          @RestParameter(defaultValue = "", description = "The lucene query.", isRequired = false, name = "q", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "0", description = "The maximum number of items to return per page.", isRequired = false, name = "limit", type = RestParameter.Type.STRING),
          @RestParameter(defaultValue = "0", description = "The page number.", isRequired = false, name = "offset", type = RestParameter.Type.STRING)
      },
      reponses = {
          @RestResponse(description = "The request was processed succesfully.", responseCode = HttpServletResponse.SC_OK)
      },
      returnDescription = "The search results, expressed as xml or json")
  public Response getByLuceneQuery(@QueryParam("q") String q, @QueryParam("limit") int limit,
                                   @QueryParam("offset") int offset, @PathParam("format") String format) {
    EpisodeQuery query = new EpisodeQuery();
    if (!StringUtils.isBlank(q))
      query.withQuery(q);
    else
      query.withPublicationDateSort(true);
    query.withLimit(limit);
    query.withOffset(offset);

    if ("json".equals(format))
      return Response.ok(episodeService.getByQuery(query)).type(MediaType.APPLICATION_JSON).build();
    else
      return Response.ok(episodeService.getByQuery(query)).type(MediaType.APPLICATION_XML).build();
  }
}
