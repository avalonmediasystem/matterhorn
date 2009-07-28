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
package org.opencastproject.workspace.impl;

import org.opencastproject.workingfilerepository.api.WorkingFileRepository;
import org.opencastproject.workspace.api.Workspace;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Dictionary;

/**
 * Implements a simple cache for remote URIs.  Delegates methods to {@link WorkingFileRepository}
 * wherever possible.
 * 
 * TODO Implement cache invalidation using the caching headers, if provided, from the remote server.
 */
public class WorkspaceImpl implements Workspace, ManagedService {
  private WorkingFileRepository repo;
  private String rootDirectory = "/tmp/matterhorn/workspace";
  
  public File get(URI uri) {
    String uriHash = DigestUtils.md5Hex(uri.toString());
    // See if there's a matching file under the root directory
    File f = new File(rootDirectory + File.separator + uriHash);
    if(f.exists()) {
      return f;
    } else {
      try {
        FileUtils.copyURLToFile(uri.toURL(), f);
        return f;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public void delete(String mediaPackageID, String mediaPackageElementID) {
    repo.delete(mediaPackageID, mediaPackageElementID);
  }

  public InputStream get(String mediaPackageID, String mediaPackageElementID) {
    return repo.get(mediaPackageID, mediaPackageElementID);
  }

  public void put(String mediaPackageID, String mediaPackageElementID, InputStream in) {
    repo.put(mediaPackageID, mediaPackageElementID, in);
  }

  public void setRepository(WorkingFileRepository repo) {
    this.repo = repo;
  }
  
  public void updated(Dictionary props) throws ConfigurationException {
    if(props.get("root") != null) {
      rootDirectory = (String)props.get("root");
    }
  }
}
