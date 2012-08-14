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
import org.opencastproject.smil.entity.MediaElement;
import org.opencastproject.smil.entity.ParallelElement;
import org.opencastproject.smil.entity.SequenceElement;
import org.opencastproject.smil.entity.Smil;
import org.opencastproject.util.NotFoundException;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowService;
import org.opencastproject.workspace.api.Workspace;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the SMIL service
 */
public class SmilServiceImpl implements SmilService {

  /**
   * the workspace
   */
  private Workspace workspace;
  /**
   * a marshaller to marshal an SMIL object to XML
   */
  private Marshaller smilMarshaller;
  /**
   * an unmarshaller to unmarshal the XML to a SMIL document
   */
  private Unmarshaller smilUnmarshaller;
  /**
   * the workflowservice
   */
  private WorkflowService workflowService;
  /**
   * the ingest service
   */
  private IngestService ingestService;

  private static final String SMIL_FLAVOR_STRING = "smil/smil";
  private static final MediaPackageElementFlavor SMIL_FLAVOR;
  private static final String SMIL_FILENAME = "smil.smil";
  private static final Logger logger = LoggerFactory.getLogger(SmilServiceImpl.class);

  static {
    SMIL_FLAVOR = MediaPackageElementFlavor.parseFlavor(SMIL_FLAVOR_STRING);
  }

  /**
   * called when service is activated
   * 
   * @param cc the context
   * @throws Exception
   */
  protected void activate(final ComponentContext cc) throws Exception {
    logger.info("activating SMIL Service");

    // set up de-/serialization
    ClassLoader cl = Smil.class.getClassLoader();
    JAXBContext jctx = JAXBContext.newInstance("org.opencastproject.smil.entity", cl);
    smilMarshaller = jctx.createMarshaller();
    smilUnmarshaller = jctx.createUnmarshaller();
  }

  @Override
  public Smil createNewSmil(long workflowId) throws SmilException, NotFoundException {
    Smil smil = new Smil();
    smil.setWorkflowId(workflowId);
    WorkflowInstance workflow = null;
    MediaPackage mp = null;
    try {
      workflow = workflowService.getWorkflowById(workflowId);
      mp = workflow.getMediaPackage();
      Catalog[] catalogs = mp.getCatalogs(SMIL_FLAVOR);
      if (catalogs.length != 0) {
        logger.debug("removing old SMIL Catalogs");
        for (Catalog c : catalogs) {
          mp.remove(c);
          workspace.delete(mp.getIdentifier().compact(), c.getIdentifier());
        }
      }

      mp = ingestService.addCatalog(IOUtils.toInputStream(smilToXML(smil)), SMIL_FILENAME,
          SMIL_FLAVOR, mp);
      workflow.setMediaPackage(mp);
      workflowService.update(workflow);

      catalogs = mp.getCatalogs(SMIL_FLAVOR);
      if (catalogs.length == 0) {
        throw new SmilException("SMIL Catalog is not in Mediapackage but should be");
      }
      smil.setId(catalogs[0].getIdentifier());
      logger.info("new ID of SMIL: " + smil.getId());
      storeSmil(smil);
    } catch (NotFoundException e) {
      throw e;
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
  public Smil addMediaElement(long workflowId, MediaElement e, String elementId)
      throws SmilException, NotFoundException {
    Smil smil = loadSmil(workflowId);
    logger.debug("trying to find path to track file");
    try {
      File f = workspace.get(new URI(e.getSrc()));
      logger.debug("found file " + f.getAbsolutePath());
      e.setSrc(f.getAbsolutePath());
    } catch (NotFoundException ex) {

    } catch (Exception ex) {
      throw new SmilException(ex.getMessage(), ex);
    }
    ParallelElement pe = smil.getParallel(elementId);
    if (pe == null) {
      throw new NotFoundException("ParallelElement with id: " + elementId + " not found");
    }
    smil.addElementTo(e, pe);
    storeSmil(smil);

    return smil;
  }

  @Override
  public Smil addParallelElement(long workflowId, ParallelElement p) throws SmilException,
      NotFoundException {
    Smil smil = loadSmil(workflowId);
    smil.getBody().getSequence().addParallel(p);
    storeSmil(smil);
    return smil;
  }

  @Override
  public Smil removeElement(long workflowId, String elementId) throws SmilException,
      NotFoundException {
    Smil smil = loadSmil(workflowId);

    smil.removeElement(elementId);

    storeSmil(smil);
    return smil;
  }

  @Override
  public Smil clearSmil(long workflowId) throws SmilException, NotFoundException {
    Smil smil = loadSmil(workflowId);

    smil.getBody().setSequence(new SequenceElement());

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
  private Smil loadSmil(long workflowId) throws SmilException, NotFoundException {
    Smil smil = null;
    try {
      WorkflowInstance workflow = workflowService.getWorkflowById(workflowId);
      MediaPackage mp = workflow.getMediaPackage();
      Catalog[] catalogs = mp.getCatalogs(SMIL_FLAVOR);
      if (catalogs.length == 0) {
        throw new NotFoundException("SMIL Document could not be found");
      }
      URI uri = workspace.getURI(mp.getIdentifier().compact(), catalogs[0].getIdentifier(),
          SMIL_FILENAME);
      File smilFile = workspace.get(uri);
      smil = (Smil) smilUnmarshaller.unmarshal(smilFile);
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new SmilException(e.getMessage());
    }
    return smil;
  }

  /**
   * store the SMIL document in the mediapackage
   * 
   * @param smil the SMIL document
   * @throws SmilException if something went wrong
   */
  private void storeSmil(Smil smil) throws SmilException {
    logger.debug("trying to store SMIL");
    try {
      WorkflowInstance workflow = workflowService.getWorkflowById(smil.getWorkflowId());
      MediaPackage mp = workflow.getMediaPackage();
      workspace.put(mp.getIdentifier().compact(), smil.getId(), SMIL_FILENAME,
          IOUtils.toInputStream(smilToXML(smil)));
    } catch (Exception e) {
      throw new SmilException(e.getMessage());
    }
  }

  /**
   * marshal a SMIL document to XML
   * 
   * @param smil the SMIL Object
   * @return the XML representation of the SMIL document
   * @throws JAXBException if something is wrong with the document
   */
  private String smilToXML(Smil smil) throws JAXBException {
    StringWriter sw = new StringWriter();
    smilMarshaller.marshal(smil, sw);
    return sw.toString();
  }

  /**
   * deactivating service
   */
  protected void deactivate() {
    logger.info("deactivating SMIL Service");
  }

  /**
   * set workspace
   * 
   * @param workspace the workspace
   */
  protected void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

  /**
   * set workflowService
   * 
   * @param workflowService the worklfowservice
   */
  protected void setWorkflowService(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  /**
   * set the ingestservice
   * 
   * @param ingestService the ingestservice
   */
  protected void setIngestService(IngestService ingestService) {
    this.ingestService = ingestService;
  }

}
