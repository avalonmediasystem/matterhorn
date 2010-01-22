/**
 *  Copyright 2009 The Regents of the University of California
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

package org.opencastproject.media.mediapackage.identifier;

/**
 * Simple and straightforward implementation of the {@link Id} interface.
 */
public class IdImpl implements Id {

  /** The identifier */
  protected String id = null;

  /**
   * Creates a new serial identifier as created by {@link SerialBuilder}.
   * 
   * @param id
   *          the identifier
   */
  public IdImpl(String id) {
    this.id = id;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.media.mediapackage.identifier.Id#compact()
   */
  public String compact() {
    return id.replaceAll("/", "-").replaceAll("\\\\", "-");
  }

  @Override
  public String toString() {
    return compact();
  }
}
