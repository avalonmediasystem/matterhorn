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
package org.opencastproject.smil.entity;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.opencastproject.mediapackage.MediaPackage;

@XmlRootElement(name = "smil", namespace = "http://www.w3.org/2006/SMIL30/WD/Language")
@XmlSeeAlso({ SequenceElement.class, MediaElement.class, BodyElement.class, ParallelElement.class })
/**
 * base for the XML representation of SMIL
 */
public class Smil {

  private BodyElement body;

  private String id;

  private MediaPackage mediaPackage;

  private long workflowId;

  public Smil() {
    body = new BodyElement();
  }

  /**
   * get a ParallelElement with a given id
   * 
   * @param elementId the elementId of the ParallelElement
   * @return the ParallelElement if it exists, null else
   */
  public ParallelElement getParallel(String elementId) {
    return this.body.getSequence().getParallel(elementId);
  }

  /**
   * add a MediaElement to a given ParallelElement
   * 
   * @param e the MediaElement to add
   * @param to the ParallelElement to add the MediaElement to
   */
  public void addElementTo(MediaElement e, ParallelElement to) {
    to.addElement(e);
  }

  /**
   * remove an element from the document, can be a MediaElement or a ParallelElement
   * 
   * @param elementId the id of the element to remove
   */
  public void removeElement(String elementId) {
    for (ParallelElement p : getBody().getSequence().getElements()) {
      if (p.getId().equals(elementId)) {
        getBody().getSequence().removeParallel(p);
      } else {
        for (MediaElement e : p.getElements()) {
          if (e.getId().equals(elementId)) {
            p.getElements().remove(e);
          }
        }
      }
    }
  }
  
  /**
   * clears the document by removing all elements
   */
  public void clearDocument() {
    for (ParallelElement p : body.getSequence().getElements()) {
      body.getSequence().removeParallel(p);
    }
  }

  @XmlElement(name = "body", type = BodyElement.class)
  public BodyElement getBody() {
    return body;
  }

  public void setBody(BodyElement body) {
    this.body = body;
  }

  @XmlAttribute
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @XmlTransient
  public MediaPackage getMediaPackage() {
    return mediaPackage;
  }

  public void setMediaPackage(MediaPackage mediaPackage) {
    this.mediaPackage = mediaPackage;
  }

  @XmlAttribute
  public long getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(long workflowId) {
    this.workflowId = workflowId;
  }

  /**
   * marshal a SMIL document to XML
   * 
   * @return the XML representation of the SMIL document
   * @throws JAXBException if something is wrong with the document
   */
  public String toXML() throws JAXBException {
    StringWriter sw = new StringWriter();
    JAXBContext jctx = JAXBContext.newInstance("org.opencastproject.smil.entity", Smil.class.getClassLoader());
    Marshaller smilMarshaller = jctx.createMarshaller();
    smilMarshaller.marshal(this, sw);
    return sw.toString();
  }
  
  /**
   * unmarshal a SMIL document from XML
   * 
   * @return SMIL document represented by XML
   * @throws JAXBException if something is wrong with the document
   */
  public static Smil fromXML(String smilXML) throws JAXBException {
    JAXBContext jctx = JAXBContext.newInstance(Smil.class);
    Unmarshaller smilUnmarshaller = jctx.createUnmarshaller();
    return (Smil) smilUnmarshaller.unmarshal(new StringReader(smilXML));
  }
  
  /**
   * unmarshal a SMIL document from XML file
   * 
   * @return SMIL document represented by XML file
   * @throws JAXBException if something is wrong with the document
   */
  public static Smil fromXML(File smilXMLFile) throws JAXBException {
    JAXBContext jctx = JAXBContext.newInstance(Smil.class);
    Unmarshaller smilUnmarshaller = jctx.createUnmarshaller();
    return (Smil) smilUnmarshaller.unmarshal(smilXMLFile);
  }
}
