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
package org.opencastproject.util.doc.rest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation type is used for annotating responses for RESTful query. This annotation type needs to be kept until
 * runtime.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RestResponse {

  /**
   * @return a HTTP response code, such as 200, 400 etc. Developer should use constants in <a
   *         href="http://download.oracle.com/javaee/6/api/javax/servlet/http/HttpServletResponse.html"
   *         >javax.servlet.http.HttpServletResponse</a> instead of magic numbers.
   */
  int responseCode();

  /**
   * @return a description of the response.
   */
  String description();

}
