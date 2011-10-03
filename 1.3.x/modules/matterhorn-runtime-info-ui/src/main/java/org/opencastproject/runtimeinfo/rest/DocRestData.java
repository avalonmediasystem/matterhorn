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
package org.opencastproject.runtimeinfo.rest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the document model class which holds the data about a set of rest endpoints, build this one time and reuse it
 * whenever you need to generate rest documentation
 */
@Deprecated
public class DocRestData extends DocData {

  public static final String FORMAT_KEY = "{FORMAT}";

  public static final String SLASH = "/";
  
  protected List<RestEndpointHolder> holders;

  /**
   * Create the base data object for creating REST documents
   * 
   * @param name
   *          the name of the set of rest enpoints (must be alphanumeric (includes _) and no spaces or special chars)
   * @param title
   *          [OPTIONAL] the title of the document
   * @param url
   *          this is the absolute base URL for this endpoint, do not include the trailing slash (e.g. /workflow)
   * @param notes
   *          [OPTIONAL] an array of notes to add into the end of the doc
   */
  public DocRestData(String name, String title, String url, String[] notes) {
    super(name, title, notes);
    if (url == null || "".equals(url)) {
      throw new IllegalArgumentException("url cannot be blank");
    }
    this.meta.put("url", url);
    // create the endpoint holders
    this.holders = new Vector<RestEndpointHolder>(2);
    this.holders.add(new RestEndpointHolder(RestEndpoint.Type.READ.name(), "Read"));
    this.holders.add(new RestEndpointHolder(RestEndpoint.Type.WRITE.name(), "Write"));
  }

  @Override
  public Map<String, Object> toMap() {
    LinkedHashMap<String, Object> m = new LinkedHashMap<String, Object>();
    m.put("meta", this.meta);
    m.put("notes", this.notes);
    // only pass through the holders with things in them
    ArrayList<RestEndpointHolder> holdersList = new ArrayList<RestEndpointHolder>();
    for (RestEndpointHolder holder : this.holders) {
      if (!holder.getEndpoints().isEmpty()) {
        for (RestEndpoint endpoint : holder.getEndpoints()) {
          // validate the endpoint
          if (!endpoint.getPathParams().isEmpty()) {
            for (Param param : endpoint.getPathParams()) {
              if (!endpoint.getPath().contains("{" + param.getName() + "}")) {
                throw new IllegalArgumentException("Path (" + endpoint.getPath() + ") does not match path parameter ("
                        + param.getName() + ") for endpoint (" + endpoint.getName()
                        + "), the path must contain all path param names");
              }
            }
          }
          // validate the path in the endpoint
          Pattern pattern = Pattern.compile("\\{(.+?)\\}");
          Matcher matcher = pattern.matcher(endpoint.getPath());
          int count = 0;
          while (matcher.find()) {
            if (!FORMAT_KEY.equals(matcher.group())) {
              count++;
            }
          }
          if (count != endpoint.getPathParams().size()) {
            throw new IllegalArgumentException("Path (" + endpoint.getPath() + ") does not match path parameters ("
                    + endpoint.getPathParams() + ") for endpoint (" + endpoint.getName()
                    + "), the path must contain the same number of path params (" + count
                    + ") as the pathParams list (" + endpoint.getPathParams().size() + ")");
          }
          // handle the forms
          if (endpoint.getForm() != null) {
            RestTestForm form = endpoint.getForm();
            if (form.isAutoGenerated()) {
              // autogenerate the test form
              form = new RestTestForm(endpoint);
              endpoint.setTestForm(form); // replace
            }
            if (form.isEmpty()) {
              // clear the form if there is no data to test
              endpoint.setTestForm(null);
            }
          }
          // handle the endpoint auto format paths
          if (endpoint.isAutoPathFormat()) {
            if (!endpoint.getFormats().isEmpty()) {
              endpoint.setPathFormat("." + FORMAT_KEY);
              StringBuilder sb = new StringBuilder();
              sb.append(".{");
              for (Format format : endpoint.getFormats()) {
                if (sb.length() > 3) {
                  sb.append("|");
                }
                sb.append(format.getName());
              }
              sb.append("}");
              endpoint.setPathFormatHtml(sb.toString());
            }
          } else {
            endpoint.setPathFormat("");
            endpoint.setPathFormatHtml("");
          }
        }
        holdersList.add(holder);
      }
    }
    m.put("endpointHolders", holdersList);
    return m;
  }

  @Override
  public String getDefaultTemplatePath() {
    return TEMPLATE_DEFAULT;
  }

  @Override
  public String toString() {
    return "DOC:meta=" + meta + ", notes=" + notes + ", " + holders;
  }

  public void addEndpoint(RestEndpoint.Type type, RestEndpoint endpoint) {
    if (type == null || endpoint == null) {
      throw new IllegalArgumentException("type and endpoint must not be null");
    }
    RestEndpointHolder currentHolder = null;
    for (RestEndpointHolder holder : this.holders) {
      if (type.name().equals(holder.getName())) {
        currentHolder = holder;
        break;
      }
    }
    if (currentHolder == null) {
      throw new IllegalStateException("Could not find holder of type: " + type.name());
    }
    currentHolder.addEndPoint(endpoint);
  }

  /**
   * Creates an abstract section which is displayed at the top of the doc
   * 
   * @param abstractText
   *          any text to place at the top of the document, can be html markup but must be valid
   */
  public void setAbstract(String abstractText) {
    if (isBlank(abstractText)) {
      this.meta.remove("abstract");
    } else {
      this.meta.put("abstract", abstractText);
    }
  }

  /**
   * Validates paths: VALID: /sample , /sample/{thing} , /{my}/{path}.xml , /my/fancy_path/is/{awesome}.{FORMAT}
   * INVALID: sample, /sample/, /sa#$%mple/path
   * 
   * @param path
   *          the path value to check
   * @return true if this path is valid, false otherwise
   */
  public static boolean isValidPath(String path) {
    boolean valid = true;
    if (isBlank(path)) {
      valid = false;
    } else {
      if (SLASH.equals(path)) {
        valid = true;
      } else if (path.endsWith("/") || !path.startsWith("/")) {
        valid = false;
      } else {
        if (!path.matches("^[\\w\\/{}\\.]+$")) {
          valid = false;
        }
      }
    }
    return valid;
  }

}
