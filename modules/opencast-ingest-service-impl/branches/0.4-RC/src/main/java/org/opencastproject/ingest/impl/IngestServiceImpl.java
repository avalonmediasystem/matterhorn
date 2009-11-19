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
package org.opencastproject.ingest.impl;

import org.opencastproject.ingest.api.IngestService;
import org.opencastproject.inspection.api.MediaInspectionService;
import org.opencastproject.media.mediapackage.DefaultMediaPackageSerializerImpl;
import org.opencastproject.media.mediapackage.MediaPackage;
import org.opencastproject.media.mediapackage.MediaPackageBuilder;
import org.opencastproject.media.mediapackage.MediaPackageBuilderFactory;
import org.opencastproject.media.mediapackage.MediaPackageElement;
import org.opencastproject.media.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.media.mediapackage.MediaPackageException;
import org.opencastproject.media.mediapackage.Track;
import org.opencastproject.media.mediapackage.UnsupportedElementException;
import org.opencastproject.media.mediapackage.MediaPackageElement.Type;
import org.opencastproject.media.mediapackage.identifier.HandleBuilder;
import org.opencastproject.media.mediapackage.identifier.HandleBuilderFactory;
import org.opencastproject.media.mediapackage.identifier.HandleException;
import org.opencastproject.media.mediapackage.jaxb.MediapackageType;
import org.opencastproject.workspace.api.Workspace;

import org.apache.commons.io.FileUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Creates and augments Matterhorn MediaPackages. Stores media into the Working File Repository.
 */
public class IngestServiceImpl implements IngestService, ManagedService, EventHandler {
  private static final Logger logger = LoggerFactory.getLogger(IngestServiceImpl.class);
  private MediaPackageBuilder builder = null;
  private HandleBuilder handleBuilder = null;
  private Workspace workspace;
  private MediaInspectionService inspection;
  private String tempFolder;

  private String fs;

  public IngestServiceImpl() {
    logger.info("Ingest Service started.");
    builder = MediaPackageBuilderFactory.newInstance().newMediaPackageBuilder();
    handleBuilder = HandleBuilderFactory.newInstance().newHandleBuilder();
    fs = File.separator;
    tempFolder = System.getProperty("java.io.tmpdir") + "opencast" + fs + "ingest-temp" + fs;
  }

  @SuppressWarnings("unchecked")
  public void updated(Dictionary props) throws ConfigurationException {
    // Update any configuration properties here
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.ingest.api.IngestService#addZippedMediaPackage(java.io.InputStream)
   */
  public MediaPackage addZippedMediaPackage(InputStream mediaPackage) throws Exception {
    // locally unzip the mediaPackage
    String tempPath = tempFolder + UUID.randomUUID().toString();
    ZipInputStream zipStream = new ZipInputStream(mediaPackage);
    ZipEntry entry = null;
    try {
      ArrayList<String> allFiles = new ArrayList<String>();
      createDirectory(tempPath);
      while ((entry = zipStream.getNextEntry()) != null) {
        String fileName = entry.getName();
        logger.info("Unzipping " + fileName);
        allFiles.add(fileName);
        FileOutputStream fout = new FileOutputStream(tempPath + File.separator + fileName);
        byte[] buffer = new byte[1024];
        int trueCount;
        while ((trueCount = zipStream.read(buffer)) != -1) {
          fout.write(buffer, 0, trueCount);
        }
        zipStream.closeEntry();
        fout.close();
      }
      zipStream.close();
    } catch (FileNotFoundException e) {
      logger.error("Error while decompressing media package! Files could not be written. " + e.getMessage());
      throw (e);
    } catch (IOException e) {
      logger.error("Error while decompressing media package! " + e.getMessage());
      throw (e);
    }
    // check media package and write data to file repo
    File manifest = new File(tempPath + File.separator + "manifest.xml");
    MediaPackage mp;
    try {
      builder.setSerializer(new DefaultMediaPackageSerializerImpl(new File(tempPath)));
      mp = builder.loadFromManifest(manifest.toURI().toURL().openStream());
      mp.renameTo(handleBuilder.createNew());
      builder.createNew();
      for (MediaPackageElement element : mp.elements()) {
        element.setIdentifier(UUID.randomUUID().toString());
        String elId = element.getIdentifier();
        element = inspect(element);
        String filename = element.getURL().getFile();
        filename = filename.substring(filename.lastIndexOf("/"));
        URL newUrl = addContentToRepo(mp, elId, filename, element.getURL().openStream());
        element.setURL(newUrl);
      }
      removeDirectory(tempFolder);
    } catch (Exception e) {
      logger.error("Ingest service: Failed to ingest media package!");
      throw (e);
    }
    // broadcast event
    ingest(mp);
    return mp;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.ingest.api.IngestService#createMediaPackage()
   */
  public MediaPackage createMediaPackage() throws MediaPackageException,
          org.opencastproject.util.ConfigurationException, HandleException {
    MediaPackage mediaPackage;
    try {
      mediaPackage = builder.createNew();
    } catch (MediaPackageException e) {
      logger.error("INGEST:Failed to create media package " + e.getLocalizedMessage());
      throw e;
    }
    return mediaPackage;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.ingest.api.IngestService#addMediaPackageTrack(URL, MediaPackageElementFlavor,
   *      MediaPackage)
   */
  public MediaPackage addTrack(URL url, MediaPackageElementFlavor flavor, MediaPackage mediaPackage)
          throws MediaPackageException, UnsupportedElementException, IOException {
    String elementId = UUID.randomUUID().toString();
    URL newUrl = addContentToRepo(mediaPackage, elementId, url);
    return addContentToMediaPackage(mediaPackage, elementId, newUrl, MediaPackageElement.Type.Track, flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.ingest.api.IngestService#addMediaPackageTrack(InputStream, MediaPackageElementFlavor,
   *      MediaPackage)
   */
  public MediaPackage addTrack(InputStream file, MediaPackageElementFlavor flavor, MediaPackage mediaPackage)
          throws MediaPackageException, UnsupportedElementException, IOException {
    String elementId = UUID.randomUUID().toString();
    URL newUrl = addContentToRepo(mediaPackage, elementId, file);
    return addContentToMediaPackage(mediaPackage, elementId, newUrl, MediaPackageElement.Type.Track, flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.ingest.api.IngestService#addMediaPackageCatalog(URL, MediaPackageElementFlavor,
   *      MediaPackage)
   */
  public MediaPackage addCatalog(URL url, MediaPackageElementFlavor flavor, MediaPackage mediaPackage)
          throws MediaPackageException, UnsupportedElementException, IOException {
    String elementId = UUID.randomUUID().toString();
    URL newUrl = addContentToRepo(mediaPackage, elementId, url);
    return addContentToMediaPackage(mediaPackage, elementId, newUrl, MediaPackageElement.Type.Catalog, flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.ingest.api.IngestService#addMediaPackageCatalog(InputStream, MediaPackageElementFlavor,
   *      MediaPackage)
   */
  public MediaPackage addCatalog(InputStream file, MediaPackageElementFlavor flavor, MediaPackage mediaPackage)
          throws MediaPackageException, UnsupportedElementException, IOException {
    String elementId = UUID.randomUUID().toString();
    URL newUrl = addContentToRepo(mediaPackage, elementId, file);
    return addContentToMediaPackage(mediaPackage, elementId, newUrl, MediaPackageElement.Type.Catalog, flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.ingest.api.IngestService#addMediaPackageAttachment(URL, MediaPackageElementFlavor,
   *      MediaPackage)
   */
  public MediaPackage addAttachment(URL url, MediaPackageElementFlavor flavor, MediaPackage mediaPackage)
          throws MediaPackageException, UnsupportedElementException, IOException {
    String elementId = UUID.randomUUID().toString();
    URL newUrl = addContentToRepo(mediaPackage, elementId, url);
    return addContentToMediaPackage(mediaPackage, elementId, newUrl, MediaPackageElement.Type.Attachment, flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.ingest.api.IngestService#addMediaPackageAttachment(InputStream, MediaPackageElementFlavor,
   *      MediaPackage)
   */
  public MediaPackage addAttachment(InputStream file, MediaPackageElementFlavor flavor, MediaPackage mediaPackage)
          throws MediaPackageException, UnsupportedElementException, IOException {
    String elementId = UUID.randomUUID().toString();
    URL newUrl = addContentToRepo(mediaPackage, elementId, file);
    return addContentToMediaPackage(mediaPackage, elementId, newUrl, MediaPackageElement.Type.Attachment, flavor);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.ingest.api.IngestService#ingest(java.lang.String,
   *      org.opencastproject.notification.api.NotificationService)
   */
  public void ingest(MediaPackage mp) throws IllegalStateException, Exception{
    
    // broadcast event
    if (eventAdmin != null) {
      logger.info("Broadcasting event...");
      Dictionary<String, String> properties = new Hashtable<String, String>();
      
      // converting media package to String presentation
      MediapackageType mpt = MediapackageType.fromXml(mp.toXml());
      properties.put("mediaPackage", mpt.toXml());
      Event event = new Event("org/opencastproject/ingest/INGEST_DONE", properties);

      // waiting 3000 ms for confirmation from Conductor service
      synchronized (this) {
        try {
          eventAdmin.postEvent(event);
          logger.info("Waiting for answer...");
          this.wait(3000);
        } catch (InterruptedException e) {
          logger.warn("Waiting for answer interupted: " + e.getMessage());
        }
      }

      // processing of confirmation
      if (errorFlag) {
        logger.error("Received exception from Conductor service: " + error.getLocalizedMessage());
        errorFlag = false;
        throw new Exception("Exception durring media package processing in Conductor service: ", error);
      } else if (ackFlag) {
        logger.info("Received ACK message: Conductor processed event succesfully");
        ackFlag = false;
      } else {
        logger.warn("Timeout occured while waiting for ACK message from Conductor service");
      }
    } else {
      // no EventAdmin available
      logger.error("Ingest service: Broadcasting event failed - Event admin not available");
      throw new IllegalStateException("EventAdmin not available");
    }
  }
  
  // ----------------------------------------------
  // -------- processing of Conductor ACK ---------
  // ----------------------------------------------

  private boolean errorFlag = false;
  private boolean ackFlag = false;
  private Throwable error = null;

  /**
   * {@inheritDoc} If event contains exception property, exception has occured during processing sent media package in
   * Conductor service.
   * 
   * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
   */
  public void handleEvent(Event event) {

    ackFlag = true;

    if (event.getProperty("exception") != null) {
      errorFlag = true;
      error = (Throwable) event.getProperty("exception");
    }

    synchronized (this) {
      this.notifyAll();
    }
  }

  // -----------------------------------------------
  // --------------------- end ---------------------
  // -----------------------------------------------

  private EventAdmin eventAdmin;

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.ingest.api.IngestService#discardMediaPackage(java.lang.String)
   */
  public void discardMediaPackage(MediaPackage mp) {
    String mediaPackageId = mp.getIdentifier().compact();
    for (MediaPackageElement element : mp.getAttachments()) {
      workspace.delete(mediaPackageId, element.getIdentifier());
    }
    for (MediaPackageElement element : mp.getCatalogs()) {
      workspace.delete(mediaPackageId, element.getIdentifier());
    }
    for (MediaPackageElement element : mp.getTracks()) {
      workspace.delete(mediaPackageId, element.getIdentifier());
    }
  }

  private URL addContentToRepo(MediaPackage mp, String elementId, URL url) throws IOException, MediaPackageException,
          UnsupportedElementException {
    workspace.put(mp.getIdentifier().compact(), elementId, url.openStream());
    return workspace.getURL(mp.getIdentifier().compact(), elementId);
    // return addContentToMediaPackage(mp, elementId, newUrl, type, flavor);
  }

  private URL addContentToRepo(MediaPackage mp, String elementId, InputStream file) throws MediaPackageException,
          UnsupportedElementException, IOException {
    workspace.put(mp.getIdentifier().compact(), elementId, file);
    return workspace.getURL(mp.getIdentifier().compact(), elementId);
    // return addContentToMediaPackage(mp, elementId, url, type, flavor);
  }

  private URL addContentToRepo(MediaPackage mp, String elementId, String filename, InputStream file)
          throws MediaPackageException, UnsupportedElementException, IOException {
    workspace.put(mp.getIdentifier().compact(), elementId, filename, file);
    return workspace.getURL(mp.getIdentifier().compact(), elementId);
    // return addContentToMediaPackage(mp, elementId, url, type, flavor);
  }

  private MediaPackage addContentToMediaPackage(MediaPackage mp, String elementId, URL url,
          MediaPackageElement.Type type, MediaPackageElementFlavor flavor) throws MediaPackageException,
          UnsupportedElementException {
    try {
      MediaPackageElement mpe = mp.add(url, type, flavor);
      mp.remove(mpe);
      mpe = inspect(mpe);
      mp.add(mpe);
      mpe.setIdentifier(elementId);
    } catch (MediaPackageException mpe) {
      logger.error("Failed to access media package for ingest");
      throw mpe;
    } catch (UnsupportedElementException uee) {
      logger.error("Unsupported element for ingest");
      throw uee;
    }
    return mp;

  }

  private void createDirectory(String dir) {
    File f = new File(dir);
    if (!f.exists()) {
      try {
        FileUtils.forceMkdir(f);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void removeDirectory(String dir) {
    File f = new File(dir);
    if (f.exists()) {
      try {
        FileUtils.deleteDirectory(f);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private MediaPackageElement inspect(MediaPackageElement element) {
    if (inspection == null)
      return element;
    if (element.getElementType() == Type.Track) {
      return inspection.enrich((Track) element, false);
    }
    return element;
  }

  // ---------------------------------------------
  // --------- config ---------
  // ---------------------------------------------
  public void setTempFolder(String tempFolder) {
    this.tempFolder = tempFolder;
  }

  // ---------------------------------------------
  // --------- bind and unbind bundles ---------
  // ---------------------------------------------
  public void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

  public void setMediaInspection(MediaInspectionService inspection) {
    this.inspection = inspection;
  }

  public void unsetMediaInspection(MediaInspectionService inspection) {
    this.inspection = null;
  }

  public void setEventAdmin(EventAdmin eventAdmin) {
    this.eventAdmin = eventAdmin;
  }

  public void unsetEventAdmin(EventAdmin eventAdmin) {
    this.eventAdmin = null;
  }

}
