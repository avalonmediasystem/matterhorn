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
package org.opencastproject.job.api;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Marshals and unmarshals {@link Job}s.
 */
public class JobParser {
  private static final JAXBContext jaxbContext;

  static {
    StringBuilder sb = new StringBuilder();
    sb.append("org.opencastproject.mediapackage");
    sb.append(":org.opencastproject.mediapackage.attachment");
    sb.append(":org.opencastproject.mediapackage.track");
    sb.append(":org.opencastproject.job.api");
    try {
      jaxbContext = JAXBContext.newInstance(sb.toString(), JobParser.class.getClassLoader());
    } catch (JAXBException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Parses an xml string representing a {@link Job}
   * 
   * @param serializedForm
   *          The serialized data
   * @return The job
   */
  public static Job parseJob(String serializedForm) throws IOException {
    return parseJob(IOUtils.toInputStream(serializedForm, "UTF-8"));
  }

  /**
   * Parses a stream representing a {@link Job}
   * 
   * @param in
   *          The serialized data
   * @param format
   *          the serialization format
   * @return The receipt
   */
  public static Job parseJob(InputStream in) throws IOException {
    Unmarshaller unmarshaller;
    try {
      unmarshaller = jaxbContext.createUnmarshaller();
      return unmarshaller.unmarshal(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in), JaxbJob.class)
      .getValue();
    } catch (Exception e) {
      throw new IOException(e);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

}