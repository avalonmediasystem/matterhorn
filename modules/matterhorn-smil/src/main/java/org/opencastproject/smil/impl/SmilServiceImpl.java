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
package org.opencastproject.smil.impl;

import java.io.File;
import java.io.StringWriter;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.opencastproject.ingest.api.IngestService;
import org.opencastproject.mediapackage.Catalog;
import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.smil.api.SmilException;
import org.opencastproject.smil.api.SmilService;
import org.opencastproject.smil.entity.ContainerElement;
import org.opencastproject.smil.entity.Smil;
import org.opencastproject.smil.entity.SmilElement;
import org.opencastproject.util.NotFoundException;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowService;
import org.opencastproject.workspace.api.Workspace;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmilServiceImpl implements SmilService {

  private Workspace workspace;
  private Marshaller smilMarshaller;
  private Unmarshaller smilUnmarshaller;
  private WorkflowService workflowService;
  private IngestService ingestService;

  private static final String SMIL_FLAVOR_STRING = "smil/smil";
  private static final MediaPackageElementFlavor SMIL_FLAVOR;
  private static final String SMIL_FILENAME = "smil.smil";
  private static final Logger logger = LoggerFactory
      .getLogger(SmilServiceImpl.class);

  static {
    SMIL_FLAVOR = MediaPackageElementFlavor.parseFlavor(SMIL_FLAVOR_STRING);
  }

  protected void activate(final ComponentContext cc) throws Exception {
    logger.info("activating SMIL Service");

    // set up de-/serialization
    ClassLoader cl = Smil.class.getClassLoader();
    JAXBContext jctx = JAXBContext.newInstance(
        "org.opencastproject.smil.entity", cl);
    smilMarshaller = jctx.createMarshaller();
    smilUnmarshaller = jctx.createUnmarshaller();
  }

  @Override
  public Smil createNewSmil(long workflowId) throws SmilException {
    Smil smil = new Smil();
    smil.setWorkflowId(workflowId);
    WorkflowInstance workflow = null;
    MediaPackage mp = null;
    try {
      workflow = workflowService.getWorkflowById(workflowId);
      mp = workflow.getMediaPackage();

      mp = ingestService.addCatalog(IOUtils.toInputStream(smilToXML(smil)),
          SMIL_FILENAME, SMIL_FLAVOR, mp);
      workflow.setMediaPackage(mp);
      workflowService.update(workflow);

      Catalog[] catalogs = mp.getCatalogs(SMIL_FLAVOR);
      if (catalogs.length == 0) {
        throw new SmilException(
            "SMIL Catalog is not in Mediapackage but should be");
      }
      smil.setId(catalogs[0].getIdentifier());
      logger.info("new ID of SMIL: " + smil.getId());
      storeSmil(smil);
    } catch (Exception e) {
      throw new SmilException(e.getMessage(), e);
    }
    return smil;
  }

  @Override
  public Smil getSmil(long workflowId) throws SmilException, NotFoundException {
    return loadSmil(workflowId);
  }

  @Override
  public Smil addElement(long workflowId, SmilElement e, String elementId)
      throws SmilException, NotFoundException {
    Smil smil = loadSmil(workflowId);
    ContainerElement ce = smil.getContainerElement(elementId);
    if (ce == null) {
      throw new NotFoundException("could not find ContainerElement with id "
          + elementId);
    }
    smil.addElementToContainer(e, ce);
    storeSmil(smil);
    return smil;
  }
  
  /**
   * load a SMIL document
   * 
   * @param workflowId the workflowID the SMIL Document belongs to
   * @return the retrieved SMIL document
   * 
   * @throws SmilException
   * @throws NotFoundException
   */
  private Smil loadSmil(long workflowId) throws SmilException,
      NotFoundException {
    Smil smil = null;
    try {
      WorkflowInstance workflow = workflowService.getWorkflowById(workflowId);
      MediaPackage mp = workflow.getMediaPackage();
      Catalog[] catalogs = mp.getCatalogs(SMIL_FLAVOR);
      if (catalogs.length == 0) {
        throw new NotFoundException("SMIL Document could not be found");
      }
      URI uri = workspace.getURI(mp.getIdentifier().compact(),
          catalogs[0].getIdentifier(), SMIL_FILENAME);
      File smilFile = workspace.get(uri);
      smil = (Smil) smilUnmarshaller.unmarshal(smilFile);
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new SmilException(e.getMessage());
    }
    return smil;
  }

  private void storeSmil(Smil smil) throws SmilException {
    logger.debug("trying to store SMIL");
    try {
      WorkflowInstance workflow = workflowService.getWorkflowById(smil
          .getWorkflowId());
      MediaPackage mp = workflow.getMediaPackage();
      workspace.put(mp.getIdentifier().compact(), smil.getId(), SMIL_FILENAME,
          IOUtils.toInputStream(smilToXML(smil)));
    } catch (Exception e) {
      throw new SmilException(e.getMessage());
    }
  }

  private String smilToXML(Smil smil) throws JAXBException {
    StringWriter sw = new StringWriter();
    smilMarshaller.marshal(smil, sw);
    return sw.toString();
  }

  protected void deactivate() {
    logger.info("deactivating SMIL Service");
  }

  protected void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

  protected void setWorkflowService(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  protected void setIngestService(IngestService ingestService) {
    this.ingestService = ingestService;
  }

}
