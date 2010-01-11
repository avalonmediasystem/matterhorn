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
package org.opencastproject.capture.impl;

import org.opencastproject.capture.admin.api.AgentState;
import org.opencastproject.capture.admin.api.RecordingState;
import org.opencastproject.capture.api.CaptureAgent;
import org.opencastproject.capture.api.StateService;
import org.opencastproject.capture.pipeline.PipelineFactory;
import org.opencastproject.media.mediapackage.MediaPackage;
import org.opencastproject.media.mediapackage.MediaPackageBuilderFactory;
import org.opencastproject.media.mediapackage.MediaPackageElement;
import org.opencastproject.media.mediapackage.MediaPackageElementBuilder;
import org.opencastproject.media.mediapackage.MediaPackageElementBuilderFactory;
import org.opencastproject.media.mediapackage.MediaPackageElementFlavor;
import org.opencastproject.media.mediapackage.MediaPackageElements;
import org.opencastproject.media.mediapackage.MediaPackageException;
import org.opencastproject.media.mediapackage.UnsupportedElementException;
import org.opencastproject.util.ZipUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gstreamer.Bus;
import org.gstreamer.GstObject;
import org.gstreamer.Pipeline;
import org.gstreamer.State;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 * Implementation of the Capture Agent: using gstreamer, generates several Pipelines
 * to store several tracks from a certain recording.
 */
public class CaptureAgentImpl implements CaptureAgent, ManagedService {
  private static final Logger logger = LoggerFactory.getLogger(CaptureAgentImpl.class);

  // TODO: move outside
  //private static final long default_capture_length = 1 * 60 * 60 * 1000; //1 Hour

  /** The agent's pipeline **/
  private Pipeline pipe = null;

  /** Keeps the recordings which have not been succesfully ingested yet **/
  HashMap<String, RecordingImpl> pendingRecordings = new HashMap<String,RecordingImpl>();

  /** The agent's current state.  Used for logging */
  private String agentState = null;
  /** A pointer to the current capture directory.  Note that this should be null except for when we are actually capturing */
  //private File currentCaptureDir = null;
  /** The properties object for the current capture.  NOTE THAT THIS WILL BE NULL IF THE AGENT IS NOT CURRENTLY CAPTURING. */
  //private Properties currentCaptureProps = null;

  /** A pointer to the state service.  This is where all of the recording state information should be kept. */
  private StateService stateService = null;
  
  /** Indicates the ID of the recording currently being recorded **/
  private String currentRecID = null;

  public CaptureAgentImpl() {
    logger.info("Starting CaptureAgentImpl.");
    setAgentState(AgentState.IDLE);
  }

  /**
   * Gets the state service this capture agent is pushing its state to
   * @return The service this agent pushes its state to.
   */
  public StateService getStateService() {
    return stateService;
  }

  /**
   * Sets the state service this capture agent should push its state to.
   * @param service The service to push the state information to
   */
  public void setStateService(StateService service) {
    stateService = service;
    setAgentState(agentState);
  }

  /**
   * Unsets the state service which this capture agent should push its state to.
   */
  public void unsetStateService() {
    stateService = null;
  }


  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.recorder.api.CaptureAgent#startCapture()
   */
  @Override
  public String startCapture() {

    logger.info("Starting capture using default values for MediaPackage and properties.");

    // Creates default MediaPackage
    MediaPackage pack;
    try {
      pack = MediaPackageBuilderFactory.newInstance().newMediaPackageBuilder().createNew();
    } catch (org.opencastproject.util.ConfigurationException e) {
      logger.error("Wrong configuration for the default media package: {}.", e.getMessage());
      return null;
    } catch (MediaPackageException e) {
      logger.error("Media Package exception: {}.", e.getMessage());
      return null;
    }

    return startCapture(pack, null);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.recorder.api.CaptureAgent#startCapture(org.opencastproject.media.mediapackage.MediaPackage)
   */
  @Override
  public String startCapture(MediaPackage mediaPackage) {

    logger.info("Starting capture using default values for the capture properties and a passed in media package.");

    return startCapture(mediaPackage, null);

  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.recorder.api.CaptureAgent#startCapture(java.util.HashMap)
   */
  @Override
  public String startCapture(Properties properties) {
    logger.info("Starting capture using a passed in properties and default media package.");

    // Creates default MediaPackage
    MediaPackage pack;
    try {
      pack = MediaPackageBuilderFactory.newInstance().newMediaPackageBuilder().createNew();
    } catch (org.opencastproject.util.ConfigurationException e) {
      logger.error("Wrong configuration for the default media package: {}.", e.getMessage());
      return null;
    } catch (MediaPackageException e) {
      logger.error("Media Package exception: {}.", e.getMessage());
      return null;
    }

    return startCapture(pack, properties);
  }

  /**
   * {@inheritDoc}
   * 
   * @see 
   *      org.opencastproject.recorder.api.CaptureAgent#startCapture(org.opencastproject.media.mediapackage.MediaPackage,
   *      HashMap properties)
   */
  @Override
  public String startCapture(MediaPackage mediaPackage, Properties properties) {

    if (currentRecID != null || !agentState.equals(AgentState.IDLE)) {
      logger.warn("Unable to start capture, a different capture is still in progress in {}.",
              pendingRecordings.get(currentRecID).getDir().getAbsolutePath());
      return null;
    } else {
      setAgentState(AgentState.CAPTURING);
    }
    
    // Creates a new recording object, checking if it was correctly initialized
    RecordingImpl newRec = new RecordingImpl(mediaPackage, properties);
    if (newRec.getRecordingID() == null) {
      logger.error("Couldn't create a valid recording ID");
      setAgentState(AgentState.IDLE);
      return null;
    }

    // Checks there is no duplicate ID
    String recordingID = newRec.getRecordingID();

    if (pendingRecordings.containsKey(recordingID)) {
      logger.error("There is already a recording with ID {}", recordingID);
      setAgentState(AgentState.IDLE);
      setRecordingState(recordingID, RecordingState.CAPTURE_ERROR);
      return null;
    } else {
      pendingRecordings.put(recordingID, newRec);
      currentRecID = recordingID;
    }


    logger.info("Initializing devices for capture.");


    pipe = PipelineFactory.create(newRec.getProperties());

    if (pipe == null) {
      logger.error("Capture {} could not start, pipeline was null!", recordingID);
      setAgentState(AgentState.IDLE);
      return null;
    }

    Bus bus = pipe.getBus();
    bus.connect(new Bus.EOS() {
      /**
       * {@inheritDoc}
       * @see org.gstreamer.Bus.EOS#endOfStream(org.gstreamer.GstObject)
       */
      public void endOfStream(GstObject arg0) {
        logger.debug("Pipeline received EOS.");
      }
    });
    bus.connect(new Bus.ERROR() {
      /**
       * {@inheritDoc}
       * @see org.gstreamer.Bus.ERROR#errorMessage(org.gstreamer.GstObject, int, java.lang.String)
       */
      public void errorMessage(GstObject arg0, int arg1, String arg2) {
        logger.error(arg0.getName() + ": " + arg2);
        stopCapture();
      }
    });

    pipe.play();

    while (pipe.getState() != State.PLAYING);
    logger.info("{} started.", pipe.getName());

    setRecordingState(recordingID, RecordingState.CAPTURING);
    //setAgentState(AgentState.CAPTURING);
    return recordingID;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.capture.api.CaptureAgent#stopCapture()
   */
  @Override
  public boolean stopCapture() {
    if (pipe == null) {
      logger.warn("Pipeline is null, unable to stop capture.");
      setAgentState(AgentState.IDLE);
      return false;
    }

    if (currentRecID == null) { 
      logger.warn("There is no currentRecID assigned, but the Pipeline is not null!!!");
      pipe.stop();
      pipe = null;
      setAgentState(AgentState.IDLE);
      return false;
    }

    RecordingImpl theRec = pendingRecordings.get(currentRecID);
    File stopFlag = new File(theRec.getDir(), "capture.stopped");

    //Take the properties out of the class level variable so that we can start capturing again immediately without worrying about overwriting them.
    //Properties cur = currentCaptureProps;
    //currentCaptureProps = null;
    currentRecID = null;

    //Update the states of everything.
    //String recordingID = cur.getProperty(CaptureParameters.RECORDING_ID);
    setRecordingState(theRec.getRecordingID(), RecordingState.CAPTURE_FINISHED);
    setAgentState(AgentState.IDLE);
    //currentCaptureDir = null;

    try {
      // Sending End Of Stream event to the Pipeline so its components stop appropriately
      //pipe.sendEvent(new EOSEvent());
      //while (pipe.getState() != State.NULL);
      //pipe.setState(State.NULL);
      pipe.stop();

      // Gst.deinit();

      stopFlag.createNewFile();
    } catch (IOException e) {
      setRecordingState(theRec.getRecordingID(), RecordingState.UPLOAD_ERROR);
      logger.error("IOException: Could not create \"capture.stopped\" file: {}.", e.getMessage());
      return false; 
    }
    /*
    try {
      IngestJob.scheduleJob(cur, state_service);
    } catch (IOException e) {
      logger.error("IOException while attempting to schedule ingest for recording {}.", recordingID);
      setRecordingState(recordingID, RecordingState.UPLOAD_ERROR);
      return false;
    } catch (SchedulerException e) {
      logger.error("SchedulerException while attempting to schedule ingest for recording {}: {}.", recordingID, e);
      setRecordingState(recordingID, RecordingState.UPLOAD_ERROR);
      return false;
    }
     */
    return true;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.capture.api.CaptureAgent#stopCapture()
   */
  @Override
  public boolean stopCapture(String recordingID) {
    if (currentRecID != null) {
      //String current_id = currentCaptureProps.getProperty(CaptureParameters.RECORDING_ID); 
      if (recordingID.equals(currentRecID)) {
        return stopCapture();
      }
    }
    return false;
  }

  // TODO: This should go in a separate method. Was in createManifest but makes it too long.
  /*// Create a metadata catalog with some info
  // TODO: Should this be moved out to its own method?
  try {
    // Create the document to hold the metadata
    metaFile = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    
    // Keep the root node of the document
    Element root = metaFile.createElement("agent-metadata");
    
    // Inserts the agent name node
    Element agentName = metaFile.createElement("agent-name");
    // FIXME: Where do we get the Capture Agent name?
    agentName.setTextContent("Change Me");
    root.appendChild(agentName);
    
    // Inserts the "mappings" node
    Node mappings = metaFile.createElement("mappings");
    root.appendChild(mappings);
    
    // Inserts the friendly names and the mappings
    for (String fName : friendlyNames) {
      //// Friendly name
      Element friendlyName = metaFile.createElement("friendly-name");
      friendlyName.setTextContent(fName);
      mappings.appendChild(friendlyName);
      //// Mapping
      Element mapping = metaFile.createElement("mapping");
      mapping.setAttribute("friendly-name", fName);
      Element input = metaFile.createElement("input");
      String inputDevice = recording.getProperty(
              CaptureParameters.CAPTURE_DEVICE_PREFIX +
              fName +
              CaptureParameters.CAPTURE_DEVICE_SOURCE
              );
      input.setTextContent(inputDevice);
      Element flavor = metaFile.createElement("flavor");
      // TODO: This should be changed according with the solution adopted for the "TODO" in line 449
      if (fName.equals("PRESENTER") || fName.equals("AUDIO"))
        flavor.setTextContent(MediaPackageElements.PRESENTER_TRACK.toString());
      else if (fName.equals("SCREEN"))
        flavor.setTextContent(MediaPackageElements.PRESENTATION_TRACK.toString());
      
      mapping.appendChild(input);
      mapping.appendChild(flavor);
      mappings.appendChild(mapping);
    }
    
    // Inserts the zipName
    Element zNameElement = metaFile.createElement("zip-name");
    zNameElement.setTextContent(recording.getZipName());
    root.appendChild(zNameElement);
    
    // Adds this document to the MediaPackage
    recording.getMediaPackage().add(new URI(recording.getAgentCatalogName()));
    
  } catch (ParserConfigurationException e) {
    logger.error("Parser Exception when creating an XML Document: {}", e.getMessage());
    return false;
  } catch (UnsupportedElementException e) {
    logger.error("The agent metadata file seems not to be supported by the MediaPackage implementation: {}", e.getMessage());
    return false;
  } catch (URISyntaxException e) {
    logger.error("Incorrect URI for the agent metadata file: {}. Cause: {}", recording.getAgentCatalogName(), e.getMessage());
    return false;
  }*/

  /**
   * Generates the manifest.xml file from the files specified in the properties
   * @return A status boolean 
   */
  public boolean createManifest(String recID) {

    RecordingImpl recording = pendingRecordings.get(recID);    
    if (recording == null) {
      logger.error("[createManifest] Recording {} not found!", recID);
      return false;
    } else
      logger.debug("Generating manifest for recording {}", recID);

    String[] friendlyNames = recording.getProperty(CaptureParameters.CAPTURE_DEVICE_NAMES).split(",");
    Document metaFile = null;
    
    // Includes the tracks in the MediaPackage
    try {
      MediaPackageElementBuilder elemBuilder = MediaPackageElementBuilderFactory.newInstance().newElementBuilder();
      MediaPackageElementFlavor flavor = null; 

      // Adds the files present in the Properties
      for (String name : friendlyNames) {
        name = name.trim();

        if (name == "")
          continue;

        // TODO: This should be modified to allow a more flexible way of detecting the track flavour.
        // Suggestions: a dedicated class or a/several field(s) in the properties indicating what type of track is each
        if (name.equals("PRESENTER") || name.equals("AUDIO"))
          flavor = MediaPackageElements.PRESENTER_TRACK;
        else if (name.equals("SCREEN"))
          flavor = MediaPackageElements.PRESENTATION_TRACK;
        
        String outputProperty = CaptureParameters.CAPTURE_DEVICE_PREFIX  + name + CaptureParameters.CAPTURE_DEVICE_DEST;
        File outputFile = new File(recording.getDir(), recording.getProperty(outputProperty));

        // Adds the file to the MediaPackage
        if (outputFile.exists())
          recording.getMediaPackage().add(elemBuilder.elementFromURI(new URI(outputFile.getName()),
                  MediaPackageElement.Type.Track,
                  flavor));
        else 
          logger.warn ("Required file {} not found", outputFile.getName());
      } 

      // TODO: Attach files outside this class, before calling startCapture()
      // Adds the rest of the files (in case some attachment was left there by the scheduler)
      /* File[] files = captureDir.listFiles();
      for (File item : files)
        // Discards the "capture.stopped" file and the files in the properties --they have already been processed
        // Also checks the file exists
        if (item.exists() && (!props.contains(item.getName().trim())) && (!item.getName().equals("capture.stopped")))
          pkg.add(new URI(item.getName()));*/
    } catch (UnsupportedElementException e) {
      logger.error("Unsupported Element Exception: {}.", e.getMessage());
      return false;
    } catch (URISyntaxException e) {
      logger.error("URI Exception: {}.", e.getMessage());
      return false;
    }
        
    // Serialize the metadata file and the MediaPackage
    try {
      // Gets the manifest.xml as a Document object
      Document doc = recording.getMediaPackage().toXml();

      // Defines a transformer to convert the object in a xml file
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      // Initializes StreamResult with File object to save to file
      File manifestFile = new File(recording.getDir(), recording.getManifestName());
      StreamResult stResult = new StreamResult(new FileOutputStream(manifestFile));
      DOMSource source = new DOMSource(doc);
      transformer.transform(source, stResult);

      // Closes the stream to make sure all the content is written to the file
      stResult.getOutputStream().close();
      
      // TODO: Move this out to another method, together with the code block commented above this method
      /*// Serializes the metadata catalog
      stResult = new StreamResult(new FileOutputStream(new File(recording.getDir(), recording.getAgentCatalogName())));
      source = new DOMSource(metaFile);
      transformer.transform(source, stResult);

      // Closes the stream to make sure all the content is written to the file
      stResult.getOutputStream().close();*/
      
      // Stores the File reference to the MediaPackage in the corresponding recording
      recording.setManifest(manifestFile);

    } catch (MediaPackageException e) {
      logger.error("MediaPackage Exception: {}.", e.getMessage());
      return false;
    } catch (TransformerException e) {
      logger.error("Transformer Exception: {}.", e.getMessage());
      return false;
    } catch (IOException e) {
      logger.error("I/O Exception: {}.", e.getMessage());
      return false;
    }

    return true;
  }

  /**
   * Compresses the files contained in the output directory
   * @param ZIP_NAME - The name of the zip file created
   * @return A File reference to the file zip created
   */
  public File zipFiles(String recID) {

    RecordingImpl recording = pendingRecordings.get(recID);

    if (recording == null) {
      logger.error("[createManifest] Recording {} not found!", recID);
      return null;
    }

    Iterable<MediaPackageElement> mpElements = recording.getMediaPackage().elements();
    Vector<File> filesToZip = new Vector<File>();
    
    // Adds the manifest first
    filesToZip.add(recording.getManifest());
    
    for (MediaPackageElement item : mpElements) {
      File tmpFile = new File(recording.getDir(), item.getURI().toString());
      // TODO: Is this really a warning or should we fail completely and return an error?
      if (!tmpFile.exists())
        logger.warn("Required file {} doesn't exist!", tmpFile.getName());
      filesToZip.add(tmpFile);
    }
    
    logger.info("Zipping {} files:", filesToZip.size());
    for (File f : filesToZip)
      logger.info("--> {}", f.getName());
    
    return ZipUtil.zip(filesToZip.toArray(new File[filesToZip.size()]), recording.getDir().getAbsolutePath() + File.separator + recording.getZipName());
  }

  
  /**
   * Sends a file to the REST ingestion service
   * @param url : The service URL
   * @param fileDesc : The descriptor for the media package
   */
  public int ingest(String recID) {
    
    RecordingImpl recording = pendingRecordings.get(recID);
    
    if (recording == null) {
      logger.error("[createManifest] Recording {} not found!", recID);
      return -1;
    }

    String url = recording.getProperty(CaptureParameters.INGEST_ENDPOINT_URL);
    //File baseDir = recording.getDir();

    HttpClient client = new DefaultHttpClient();
    HttpPost postMethod = new HttpPost(url);
    int retValue = -1;

    File fileDesc = new File(recording.getDir(), "media.zip");

    try {
      // Sets the file as the body of the request
      FileEntity myFileEntity = new FileEntity(fileDesc, URLConnection.getFileNameMap().getContentTypeFor(fileDesc.getName()));

      logger.warn("Sending the file " + fileDesc.getAbsolutePath() + " with a size of "+ fileDesc.length());

      postMethod.setEntity(myFileEntity);

      // Send the file
      HttpResponse response = client.execute(postMethod);

      retValue = response.getStatusLine().getStatusCode();

      //singleton.setRecordingState(recordingID, RecordingState.UPLOAD_FINISHED);
    } catch (ClientProtocolException e) {
      logger.error("Failed to submit the data: {}.", e.getMessage());
      //singleton.setRecordingState(recordingID, RecordingState.UPLOAD_ERROR);
    } catch (IOException e) {
      logger.error("I/O Exception: {}.", e.getMessage());
      //singleton.setRecordingState(recordingID, RecordingState.UPLOAD_ERROR);
    } finally {
      client.getConnectionManager().shutdown();
      //setAgentState(AgentState.IDLE);
    }

    return retValue;
  } 

  /**
   * Sets the machine's current encoding status
   * 
   * @param state The state for the agent.  Defined in AgentState.
   * @see org.opencastproject.capture.api.AgentState
   */
  private void setAgentState(String state) {
    agentState = state;
    if (stateService != null) {
      stateService.setAgentState(agentState);
    } else {
      logger.warn("State service for capture agent is null, unable to push updates to remote server!");
    }
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.capture.api.CaptureAgent#getAgentState()
   */
  public String getAgentState() {
    return agentState;
  }

  /**
   * Convenience method which wraps calls to the state_service to make sure it's not going to null pointer on me.
   * @param recordingID The ID of the recording to update
   * @param state The state to update the recording to
   */
  // TODO: Move this to the Recording class
  private void setRecordingState(String recordingID, String state) {
    if (stateService != null) {
      stateService.setRecordingState(recordingID, state);
    } else {
      logger.warn("State service for capture agent is null, unable to push updates to remote server!");
    }
  }

  /**
   * @param recID
   * @return A Recording with ID recID, or null if it doesn't exists
   */
  public RecordingImpl getRecording(String recID) {
    return pendingRecordings.get(recID);
  }

  public void updated(Dictionary props) throws ConfigurationException {
    // Update any configuration properties here
  }

}
