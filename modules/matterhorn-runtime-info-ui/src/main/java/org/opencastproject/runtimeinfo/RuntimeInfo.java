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
package org.opencastproject.runtimeinfo;

import static org.opencastproject.rest.RestConstants.SERVICES_FILTER;

import org.opencastproject.rest.RestConstants;
import org.opencastproject.security.api.Organization;
import org.opencastproject.security.api.SecurityService;
import org.opencastproject.security.api.User;
import org.opencastproject.util.UrlSupport;
import org.opencastproject.util.doc.rest.RestQuery;
import org.opencastproject.util.doc.rest.RestResponse;
import org.opencastproject.util.doc.rest.RestService;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * This REST endpoint provides information about the runtime environment, including the services and user interfaces
 * deployed and the current login context.
 * 
 * If the 'org.opencastproject.anonymous.feedback.url' is set in config.properties, this service will also update the
 * opencast project with the contents of the {@link #getRuntimeInfo()} json feed.
 */
@Path("/")
@RestService(name = "RuntimeInfo", title = "Runtime Information", notes = "", abstractText = "This service provides information about the runtime environment, including the servives that are deployed and the current user context.")
public class RuntimeInfo {

  private static final Logger logger = LoggerFactory.getLogger(RuntimeInfo.class);

  /** Configuration properties id */
  private static final String ADMIN_URL_PROPERTY = "org.opencastproject.admin.ui.url";
  private static final String ENGAGE_URL_PROPERTY = "org.opencastproject.engage.ui.url";
  private static final String SERVER_URL_PROPERTY = "org.opencastproject.server.url";

  /** The rest publisher looks for any non-servlet with the 'opencast.service.path' property */
  public static final String SERVICE_FILTER = "(&(!(objectClass=javax.servlet.Servlet))("
          + RestConstants.SERVICE_PATH_PROPERTY + "=*))";

  private SecurityService securityService;
  private BundleContext bundleContext;
  private URL serverUrl;
  private URL engageBaseUrl;
  private URL adminBaseUrl;

  protected void setSecurityService(SecurityService securityService) {
    this.securityService = securityService;
  }

  protected ServiceReference[] getRestServiceReferences() throws InvalidSyntaxException {
    return bundleContext.getAllServiceReferences(null, SERVICES_FILTER);
  }

  protected ServiceReference[] getUserInterfaceServiceReferences() throws InvalidSyntaxException {
    return bundleContext.getAllServiceReferences(Servlet.class.getName(), "(&(alias=*)(classpath=*))");
  }

  public void activate(ComponentContext cc) throws MalformedURLException {
    logger.debug("start()");

    this.bundleContext = cc.getBundleContext();

    serverUrl = new URL(bundleContext.getProperty(SERVER_URL_PROPERTY));

    // Get admin UI url
    String adminBaseUrlStr = bundleContext.getProperty(ADMIN_URL_PROPERTY);
    if (StringUtils.isBlank(adminBaseUrlStr))
      adminBaseUrl = serverUrl;
    else
      adminBaseUrl = new URL(adminBaseUrlStr);
    logger.debug("Default admin server url is {}", adminBaseUrlStr);

    // Get engage UI url
    String engageBaseUrlStr = bundleContext.getProperty(ENGAGE_URL_PROPERTY);
    if (StringUtils.isBlank(engageBaseUrlStr))
      engageBaseUrl = serverUrl;
    else
      engageBaseUrl = new URL(engageBaseUrlStr);
    logger.debug("Default engage server url is {}", engageBaseUrl);
  }

  public void deactivate() {
    // Nothing to do
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("components.json")
  @RestQuery(name = "services", description = "List the REST services and user interfaces running on this host", reponses = { @RestResponse(description = "The components running on this host", responseCode = HttpServletResponse.SC_OK) }, returnDescription = "")
  @SuppressWarnings("unchecked")
  public String getRuntimeInfo(@Context HttpServletRequest request) throws MalformedURLException {
    Organization organization = securityService.getOrganization();

    // Get request protocol and port
    String targetScheme = request.getScheme();

    // Create the engage target URL
    URL targetEngageBaseUrl = null;
    String organizationEngageURL = organization.getProperties().get(ENGAGE_URL_PROPERTY);
    if (StringUtils.isNotBlank(organizationEngageURL)) {
      try {
        targetEngageBaseUrl = new URL(organizationEngageURL);
      } catch (MalformedURLException e) {
        logger.warn("Engage url '{}' of organization '{}' is malformed", organizationEngageURL, organization.getId());
        targetEngageBaseUrl = new URL(targetScheme, engageBaseUrl.getHost(), engageBaseUrl.getPort(),
                engageBaseUrl.getFile());
      }
    } else {
      targetEngageBaseUrl = new URL(targetScheme, engageBaseUrl.getHost(), engageBaseUrl.getPort(),
              engageBaseUrl.getFile());
    }

    // Create the admin target URL
    URL targetAdminBaseUrl = null;
    String organizationAdminURL = organization.getProperties().get(ADMIN_URL_PROPERTY);
    if (StringUtils.isNotBlank(organizationAdminURL)) {
      try {
        targetAdminBaseUrl = new URL(organizationAdminURL);
      } catch (MalformedURLException e) {
        logger.warn("Admin url '{}' of organization '{}' is malformed", organizationAdminURL, organization.getId());
        targetAdminBaseUrl = new URL(targetScheme, adminBaseUrl.getHost(), adminBaseUrl.getPort(),
                adminBaseUrl.getFile());
      }
    } else {
      targetAdminBaseUrl = new URL(targetScheme, adminBaseUrl.getHost(), adminBaseUrl.getPort(), adminBaseUrl.getFile());
    }

    JSONObject json = new JSONObject();
    json.put("engage", targetEngageBaseUrl.toString());
    json.put("admin", targetAdminBaseUrl.toString());
    json.put("rest", getRestEndpointsAsJson(request));
    json.put("ui", getUserInterfacesAsJson());

    return json.toJSONString();
  }

  @GET
  @Path("me.json")
  @Produces(MediaType.APPLICATION_JSON)
  @RestQuery(name = "me", description = "Information about the curent user", reponses = { @RestResponse(description = "Returns information about the current user", responseCode = HttpServletResponse.SC_OK) }, returnDescription = "")
  @SuppressWarnings("unchecked")
  public String getMyInfo() {
    JSONObject json = new JSONObject();

    User user = securityService.getUser();
    json.put("username", user.getUserName());

    // Add the current user's roles
    JSONArray roles = new JSONArray();
    for (String role : user.getRoles()) {
      roles.add(role);
    }
    json.put("roles", roles);

    // Add the current user's organizational information
    Organization org = securityService.getOrganization();

    JSONObject jsonOrg = new JSONObject();
    jsonOrg.put("id", org.getId());
    jsonOrg.put("name", org.getName());
    jsonOrg.put("adminRole", org.getAdminRole());
    jsonOrg.put("anonymousRole", org.getAnonymousRole());

    // and organization properties
    JSONObject orgProps = new JSONObject();
    jsonOrg.put("properties", orgProps);
    for (Entry<String, String> entry : org.getProperties().entrySet()) {
      orgProps.put(entry.getKey(), entry.getValue());
    }
    json.put("org", jsonOrg);

    return json.toJSONString();
  }

  @SuppressWarnings("unchecked")
  protected JSONArray getRestEndpointsAsJson(HttpServletRequest request) throws MalformedURLException {
    JSONArray json = new JSONArray();
    ServiceReference[] serviceRefs = null;
    try {
      serviceRefs = getRestServiceReferences();
    } catch (InvalidSyntaxException e) {
      e.printStackTrace();
    }
    if (serviceRefs == null)
      return json;
    for (ServiceReference servletRef : sort(serviceRefs)) {
      String version = servletRef.getBundle().getVersion().toString();
      String description = (String) servletRef.getProperty(Constants.SERVICE_DESCRIPTION);
      String type = (String) servletRef.getProperty(RestConstants.SERVICE_TYPE_PROPERTY);
      String servletContextPath = (String) servletRef.getProperty(RestConstants.SERVICE_PATH_PROPERTY);
      JSONObject endpoint = new JSONObject();
      endpoint.put("description", description);
      endpoint.put("version", version);
      endpoint.put("type", type);
      URL url = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), servletContextPath);
      endpoint.put("path", servletContextPath);
      endpoint.put("docs", UrlSupport.concat(url.toExternalForm(), "/docs")); // This is a Matterhorn convention
      endpoint.put("wadl", UrlSupport.concat(url.toExternalForm(), "/?_wadl&_type=xml")); // This triggers a
      json.add(endpoint);
    }
    return json;
  }

  @SuppressWarnings("unchecked")
  protected JSONArray getUserInterfacesAsJson() {
    JSONArray json = new JSONArray();
    ServiceReference[] serviceRefs = null;
    try {
      serviceRefs = getUserInterfaceServiceReferences();
    } catch (InvalidSyntaxException e) {
      e.printStackTrace();
    }
    if (serviceRefs == null)
      return json;
    for (ServiceReference ref : sort(serviceRefs)) {
      String description = (String) ref.getProperty(Constants.SERVICE_DESCRIPTION);
      String version = ref.getBundle().getVersion().toString();
      String alias = (String) ref.getProperty("alias");
      String welcomeFile = (String) ref.getProperty("welcome.file");
      String welcomePath = "/".equals(alias) ? alias + welcomeFile : alias + "/" + welcomeFile;
      JSONObject endpoint = new JSONObject();
      endpoint.put("description", description);
      endpoint.put("version", version);
      endpoint.put("welcomepage", serverUrl + welcomePath);
      json.add(endpoint);
    }
    return json;
  }

  /**
   * Returns the array of references sorted by their {@link Constants.SERVICE_DESCRIPTION} property.
   * 
   * @param references
   *          the referencens
   * @return the sorted set of references
   */
  protected static SortedSet<ServiceReference> sort(ServiceReference[] references) {
    // Sort the service references
    SortedSet<ServiceReference> sortedServiceRefs = new TreeSet<ServiceReference>(new Comparator<ServiceReference>() {
      @Override
      public int compare(ServiceReference o1, ServiceReference o2) {
        String o1Description = (String) o1.getProperty(Constants.SERVICE_DESCRIPTION);
        if (StringUtils.isBlank(o1Description))
          o1Description = o1.toString();
        String o2Description = (String) o2.getProperty(Constants.SERVICE_DESCRIPTION);
        if (StringUtils.isBlank(o2Description))
          o2Description = o2.toString();
        return o1Description.compareTo(o2Description);
      }
    });
    sortedServiceRefs.addAll(Arrays.asList(references));
    return sortedServiceRefs;
  }
}
