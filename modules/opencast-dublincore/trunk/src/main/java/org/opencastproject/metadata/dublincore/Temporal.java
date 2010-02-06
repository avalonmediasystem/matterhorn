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

package org.opencastproject.metadata.dublincore;

/**
 * A temporal is either an instant, a duration or a period.
 */
public abstract class Temporal<T> {

  enum Type {
    Instant, Duration, Period
  }

  private T temporal;
  private Type type;

  public Temporal(T temporal, Type type) {
    this.temporal = temporal;
    this.type = type;
  }

  public T getTemporal() {
    return temporal;
  }

  public Type getType() {
    return type;
  }

}
