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
package org.opencastproject.capture.admin;

import org.opencastproject.capture.api.AgentState;
import org.opencastproject.capture.api.Recording;
import org.opencastproject.capture.api.RecordingState;
import org.opencastproject.util.NotFoundException;
import org.opencastproject.workflow.api.WorkflowDatabaseException;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowInstance.WorkflowState;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowService;

import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.RollbackException;
import javax.persistence.spi.PersistenceProvider;

/**
 * API for the capture-admin service (MH-1336, MH-1394, MH-1457, MH-1475 and MH-1476).
 */
public class CaptureAgentStateService {

  /** Return value for successful operations */
  public static final int OK = 0;
  
  /** Return value for methods where an non-existent agent is specified */
  public static final int NO_SUCH_AGENT = -1;
  
  /** Return value for methods where an incorrect parameter is specified */
  public static final int BAD_PARAMETER = -2;

  /** Defines the name of the key in the properties file which is used to define the list of sticky agents */
  public static final String STICKY_AGENTS = "capture.admin.sticky.agents";

  private static final Logger logger = LoggerFactory.getLogger(CaptureAgentStateService.class);

  /** The JPA provider */
  protected PersistenceProvider persistenceProvider;

  /** The persistence properties */
  @SuppressWarnings({ "rawtypes" })
  protected Map persistenceProperties;

  /** The factory used to generate the entity manager */
  protected EntityManagerFactory emf = null;

  /** The workflow service */
  protected WorkflowService workflowService;

  private HashMap<String, Agent> agents;
  private HashMap<String, Recording> recordings;

  /**
   * @param persistenceProvider
   *          the persistenceProvider to set
   */
  public void setPersistenceProvider(PersistenceProvider persistenceProvider) {
    this.persistenceProvider = persistenceProvider;
  }

  /**
   * Sets the workflow service
   * 
   * @param workflowService
   *          the workflowService to set
   */
  public void setWorkflowService(WorkflowService workflowService) {
    this.workflowService = workflowService;
  }

  /**
   * @param persistenceProperties
   *          the persistenceProperties to set
   */
  @SuppressWarnings({ "rawtypes" })
  public void setPersistenceProperties(Map persistenceProperties) {
    this.persistenceProperties = persistenceProperties;
  }

  public CaptureAgentStateService() {
    logger.debug("CaptureAgentStateService starting.");
    agents = new HashMap<String, Agent>();
    recordings = new HashMap<String, Recording>();
  }

  @SuppressWarnings("unchecked")
  public void activate(ComponentContext cc) {
    emf = persistenceProvider.createEntityManagerFactory(
            getClass().getName(), persistenceProperties);

    // Pull all the existing agents into the in-memory structure
    EntityManager em = emf.createEntityManager();
    List<Agent> dbResults = null;
    Query q = em.createNamedQuery("Agent.getAll");
    dbResults = (List<Agent>) q.getResultList();
    em.close();

    if (dbResults != null) {
      for (Agent a : dbResults) {
        if (!agents.containsKey(a.getName())) {
          agents.put(a.getName(), a);
        }
      }
    }
  }

  public void deactivate() {
    if (emf != null) {
      emf.close();
    }
  }

  /**
   * Returns the last known state of a given agent.
   *
   * @param agentName The name of the agent.
   * @return The agent with a value for both its last checking time and its last known state.
   */
  public Agent getAgentState(String agentName) {
    Agent req = agents.get(agentName);
    // If that agent doesn't exist, return an unknown agent, else return the known agent
    if (req == null) {
      logger.debug("Agent {} does not exist in the system.", agentName);
    } else {
      logger.debug("Agent {} found, returning state.", agentName);
    }
    return req;
  }

  /**
   * Sets a given agent's state.  Note that this will create the agent if it does not already exist.
   * The state should be defined in {@link org.opencastproject.capture.api.api.AgentState}.
   *
   * @param agentName The name of the agent.
   * @param state The current state of the agent.
   * @see AgentState
   */
  public int setAgentState(String agentName, String state) {

    // Checks the state is not null nor empty
    if (StringUtils.isBlank(state)) {
      logger.debug("Unable to set agent state, state is blank or null.");
      return BAD_PARAMETER;
    } else if (StringUtils.isBlank(agentName)) {
      logger.debug("Unable to set agent state, agent name is blank or null.");
      return BAD_PARAMETER;
    } else if (!AgentState.KNOWN_STATES.contains(state)) {
      logger.warn("can not set agent to an invalid state: ", state);
      return BAD_PARAMETER;
    } else {
      logger.debug("Agent '{}' state set to '{}'", agentName, state);
    }

    Agent req = agents.get(agentName);
    // if the agent is known set the state
    if (req != null) {
      logger.debug("Setting Agent {} to state {}.", agentName, state);
      req.setState(state);
      agents.put(agentName, req);
      updateAgentInDatabase(req);
    } else {
      // If the agent doesn't exists, but the name is not null nor empty, create a new one.
      logger.debug("Creating Agent {} with state {}.", agentName, state);
      Agent a = new Agent(agentName, state, "", new Properties());
      agents.put(agentName, a);
      updateAgentInDatabase(a);
    }

    return OK;
  }
  
  /**
   * FIXME -- add javadocs
   * 
   * @param agentName
   * @param agentUrl
   * @return
   */
  public boolean setAgentUrl(String agentName, String agentUrl) {
    Agent req = agents.get(agentName);
    if (req != null) {
      req.setUrl(agentUrl);
      agents.put(agentName, req);
      updateAgentInDatabase(req);
    } else {
      return false;
    }
    return true;
  }

  /**
   * Remove an agent from the system, if the agent exists.
   *
   * @param agentName The name of the agent.
   * 
   */
  public int removeAgent(String agentName) {
    if (agents.containsKey(agentName)) {
      logger.debug("Removing Agent {}.", agentName);
      agents.remove(agentName);
      deleteAgentFromDatabase(agentName);
      return OK;
    }
    return NO_SUCH_AGENT;
  }

  /**
   * Returns the list of known agents.
   *
   * @return A {@link java.util.Map} of name-agent pairs.
   */
  public Map<String, Agent> getKnownAgents() {
    return agents;
  }

  /**
   * Returns the list of known agent capabilities.
   *
   * @return A {@link java.util.Properties} of name-value capability pairs.
   */
  public Properties getAgentCapabilities(String agentName) {
    Agent req = agents.get(agentName);
    // if the agent is known set the state
    if (req != null) {
      Properties temp = agents.get(agentName).getCapabilities();
      return temp;
    } else {
      return null;
    }
  }

  /**
   * Sets the capabilities for the specified agent
   * @param agentName
   * @param capabilities
   * @return One of the constants defined in this class
   */
  public int setAgentCapabilities(String agentName, Properties capabilities) {
    Agent req = agents.get(agentName);
    if (req != null) {
      logger.debug("Setting Agent {}'s capabilities", agentName);
      req.setCapabilities(capabilities);
      updateAgentInDatabase(req);
    } else {
      // If the agent doesn't exists, but the name is not null nor empty, create a new one.
      if (StringUtils.isBlank(agentName)) {
        logger.debug("Unable to set agent state, agent name is blank or null.");
        return BAD_PARAMETER;
      }

      logger.debug("Creating Agent {} with state {}.", agentName, AgentState.UNKNOWN);
      Agent a = new Agent(agentName, AgentState.UNKNOWN, "", capabilities);
      agents.put(agentName, a);
      updateAgentInDatabase(a);
    }

    return OK;
  }
  
  /**
   * Gets the state of a recording, if it exists.
   *
   * @param id The id of the recording.
   * @return The state of the recording, or null if it does not exist.  This should be defined from {@link org.opencastproject.capture.api.api.RecordingState}.
   * @see RecordingState
   */
  public Recording getRecordingState(String id) {
    Recording req = recordings.get(id);
    // If that recording doesn't exist, return null
    if (req == null)
      logger.debug("Recording {} does not exist in the system.", id);
    else
      logger.debug("Recording {} found, returning state.", id);

    return req;

  }

  /**
   * Updates the state of a recording with the given state, if it exists.
   *
   * @param id The id of the recording in the system.
   * @param state The state to set for that recording.  This should be defined from {@link org.opencastproject.capture.api.api.RecordingState}.
   * @see RecordingState
   */
  public boolean setRecordingState(String id, String state) {
    if (state == null)
      throw new IllegalArgumentException("state can not be null");
    if (!RecordingState.KNOWN_STATES.contains(state)) {
      logger.warn("Invalid recording state: {}.", state);
      return false;
    }
    Recording req = recordings.get(id);
    if (req != null) {
      if (state.equals(req.getState())) {
        logger.debug("Recording state not changed");
        //Reset the state anyway so that the last-heard-from time is correct...
        req.setState(state);
        return true;
      } else {
        logger.debug("Setting Recording {} to state {}.", id, state);
        req.setState(state);
        updateWorkflow(id, state);
        return true;
      }
    } else {
      if (StringUtils.isBlank(id)) {
        logger.debug("Unable to set recording state, recording name is blank or null.");
        return false;
      } else if (StringUtils.isBlank(state)) {
        logger.debug("Unable to set recording state, recording state is blank or null.");
        return false;
      }
      logger.debug("Creating Recording {} with state {}.", id, state);
      RecordingImpl r = new RecordingImpl(id, state);
      recordings.put(id, r);
      updateWorkflow(id, state);
      return true;
    }
  }

  /**
   * Removes a recording from the system, if the recording exists.
   *
   * @param id The id of the recording to remove.
   */
  public boolean removeRecording(String id) {
    logger.debug("Removing Recording {}.", id);
    if (recordings.remove(id) != null) {
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * Gets the state of all recordings in the system.
   *
   * @return A map of recording-state pairs.
   */
  public Map<String,Recording> getKnownRecordings() {
    return recordings;
  }
  
  /**
   * Gets a Set of ids of all known recordings.
   * @return Set<String> recording ids
   */
  public List<String> getKnownRecordingsIds() {
    LinkedList<String> ids = new LinkedList<String>();
    for (Entry<String, Recording> e : recordings.entrySet()) {
      ids.add(e.getValue().getID());
    }
    return ids;
  }
  
  /**
   * Updates or adds an agent to the database.
   * 
   * @param a
   *          The Agent you wish to modify or add in the database.
   */
  private synchronized void updateAgentInDatabase(Agent a) {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = null;
    try {
      tx = em.getTransaction();
      tx.begin();
      Agent existing = em.find(Agent.class, a.getName());
      if (existing != null) {
        existing.setCapabilities(a.getCapabilities());
        existing.setLastHeardFrom(a.getLastHeardFrom());
        existing.setState(a.getState());
        em.merge(existing);
      } else {
        em.persist(a);
      }
      tx.commit();
    } catch (RollbackException e) {
      logger.warn("Unable to commit to DB in updateAgent.");
    } finally {
      em.close();
    }
  }

  /**
   * Removes an agent from the database.
   * 
   * @param agentName
   *          The name of the agent you wish to remove.
   */
  private synchronized void deleteAgentFromDatabase(String agentName) {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = null;
    try {
      tx = em.getTransaction();
      tx.begin();
      Agent existing = em.find(Agent.class, agentName);
      if (existing != null) {
        em.remove(existing);
      }
      tx.commit();
    } catch (RollbackException e) {
      logger.warn("Unable to commit to DB in deleteAgent.");
    } finally {
      em.close();
    }
  }

  /**
   * Resumes a workflow instance associated with this capture, if one exists.
   * 
   * @param recordingId
   *          the recording id, which is assumed to correspond to the scheduled event id
   * @param state
   *          the new state for this recording
   */
  protected void updateWorkflow(String recordingId, String state) {
    if (!RecordingState.CAPTURING.equals(state) && !RecordingState.UPLOADING.equals(state) && !state.endsWith("_error")) {
      logger.debug("Recording state updated to {}.  Not updating an associated workflow.", state);
      return;
    }

    WorkflowInstance workflowToUpdate = null;
    try {
      workflowToUpdate = workflowService.getWorkflowById(Long.parseLong(recordingId));
    } catch (NumberFormatException e) {
      logger.warn("Recording id '{}' is not a long, and is therefore not a valid workflow identifier", recordingId, e);
      return;
    } catch (WorkflowDatabaseException e) {
      logger.warn("Unable to update workflow for recording {}: {}", recordingId, e);
      return;
    } catch (NotFoundException e) {
      logger.warn("Unable to find a workflow with id='{}'", recordingId);
      return;
    }
    try {
      if (state.endsWith("_error")) {
        workflowToUpdate.getCurrentOperation().setState(WorkflowOperationInstance.OperationState.FAILED);
        workflowToUpdate.setState(WorkflowState.FAILED);
        workflowService.update(workflowToUpdate);
        logger.info("Recording status changed to '{}', failing workflow '{}'", state, workflowToUpdate.getId());
      } else {
        workflowService.resume(workflowToUpdate.getId());
        logger.info("Recording status changed to '{}', resuming workflow '{}'", state, workflowToUpdate.getId());
      }
    } catch (Exception e) {
      logger.warn("Unable to update workflow {}: {}", workflowToUpdate.getId(), e);
    }
  }

}
