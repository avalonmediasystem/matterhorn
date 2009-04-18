/**
 *  Copyright 2009 Opencast Project (http://www.opencastproject.org)
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
package org.opencastproject.repository.impl;

import org.opencastproject.repository.api.OpencastRepository;
import org.opencastproject.rest.OpencastRestService;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/repository")
public class RepositoryRestService implements OpencastRestService {
  protected OpencastRepository repo;
  public RepositoryRestService(OpencastRepository repo) {
    this.repo = repo;
  }

  /**
   * Returns the data stored at a path in the repository as an binary stream
   */
  @GET
  @Path("/data/{path:.*}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public InputStream getData(@PathParam("path") String path) {
    return repo.getObject(InputStream.class, "/" + path);
  }

  /**
   * TODO Returns the metadata from a path in the repository.  Should we specify the return format?
   */
  @GET
  @Path("/metadata/{key}/{path:.*}")
  @Produces(MediaType.TEXT_PLAIN)
  public String getMetadata(@PathParam("key") String key, @PathParam("path") String path) {
    return null;
  }

  /**
   * TODO Posts new metadata to the key at a path in the repository
   * @param key
   * @param path
   * @param metadata
   */
  @PUT
  @POST
  @Path("/metadata/{key}/{path:.*}")
  public void putMetadata(@PathParam("key") String key, @PathParam("path") String path,
      @FormParam("metadata") String metadata) {
  }
  
  /**
   * Copies an servlet request's file upload directly into the repository
   */
  @PUT
  @POST
  @Path("/data/{path:.*}")
  public void putData(@PathParam("path") String path, @Context HttpServletRequest request) {
    if ( ! ServletFileUpload.isMultipartContent(request)) {
      throw new RuntimeException("This URL is for uploading media bundles.");
    }
    ServletFileUpload upload = new ServletFileUpload();
    FileItemStream fileItemStream = null;
    try {
      FileItemIterator iter = upload.getItemIterator(request);
      while (iter.hasNext()) {
        FileItemStream item = iter.next();
        if ( ! item.isFormField()) {
          fileItemStream = item;
          break;
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    if(fileItemStream == null) {
      throw new RuntimeException("unable to parse a file from this request");
    }
    if(repo == null) {
      throw new RuntimeException("unable to connect to media repository");
    }
    try {
      repo.putObject(fileItemStream.openStream(), "/" + path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}