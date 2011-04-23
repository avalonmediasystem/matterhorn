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
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * This annotation type is used for annotating RESTful query(each java method, instead of the class). This annotation
 * type needs to be kept until runtime.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RestQuery {

  /**
   * @return the name of the query.
   */
  String name();

  /**
   * @return a description of the query.
   */
  String description();

  /**
   * @return a description of what is returned.
   */
  String returnDescription();

  /**
   * @return a list of possible responses from this query.
   */
  RestResponse[] reponses();

  /**
   * @return a list of path parameters for this query.
   */
  RestParameter[] pathParameters() default { };

  /**
   * @return a list of query parameters for this query.
   */
  RestParameter[] restParameters() default { };

  /**
   * @return a body parameter for this query.
   */
  RestParameter bodyParameter() default @RestParameter(defaultValue = "", description = "", isRequired = false, name = "", type = RestParameter.Type.NO_PARAMETER);
}
