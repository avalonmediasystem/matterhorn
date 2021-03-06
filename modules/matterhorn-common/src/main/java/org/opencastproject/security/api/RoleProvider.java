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
package org.opencastproject.security.api;

/**
 * Mix-in interface for directories that can list known roles.
 */
public interface RoleProvider {

  /**
   * Gets all known roles.
   * 
   * @return the roles
   */
  String[] getRoles();

  /**
   * Returns the roles for this user or an empty array if no roles are applicable.
   * 
   * @param userName
   *          the user id
   * @return the set of roles
   */
  String[] getRolesForUser(String userName);

  /**
   * Returns the identifier for the organization that is defining this set of roles.
   * 
   * @return the defining organization
   */
  String getOrganization();

}
