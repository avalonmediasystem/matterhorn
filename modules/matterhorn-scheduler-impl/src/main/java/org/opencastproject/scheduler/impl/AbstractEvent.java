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

package org.opencastproject.scheduler.impl;

import java.util.Hashtable;
import java.util.Set;

public abstract class AbstractEvent {
  
  public Hashtable<String, String> metadataTable;
  
  public String getValue (String key) {
   return metadataTable.get(key);
   
  }
  
  public Set<String> getKeySet () {
    return metadataTable.keySet();
  }
  
  public boolean containsKey (String key) {
    return metadataTable.containsKey(key);
  }
}
