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
package org.opencastproject.deliver.store;

/**
 * Exception thrown when an invalid key is used.
 *
 * @author Jonathan A. Smith
 */

public class InvalidKeyException extends RuntimeException {
    
    /**
     * Constructs an InvalidKeyException.
     * 
     * @param message description of key error
     */

    public InvalidKeyException(String message) {
        super(message);
    }

}
