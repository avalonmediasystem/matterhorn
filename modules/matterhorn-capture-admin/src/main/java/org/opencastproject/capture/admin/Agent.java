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

import java.util.Properties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * An in-memory construct to represent the state of a capture agent, and when it was last heard from.
 */
@Entity
@Table(name = "CAPTURE_AGENT_STATE")
@NamedQueries({ @NamedQuery(name = "Agent.getAll", query = "SELECT a FROM Agent a") })
public class Agent {

  /**
   * The name of the agent.
   */
  @Id
  protected String name;

  /**
   * The state of the agent. This should be defined from the constants in AgentState.
   */
  @Column(name = "STATE", nullable = false)
  protected String state;

  /**
   * The URL of the agent. This is determined from the referer header parameter when the agent is registered.
   * 
   */
  @Column(name = "URL")
  protected String url;

  /**
   * The time at which the agent last checked in with this service. Note that this is an absolute timestamp (ie,
   * milliseconds since 1970) rather than a relative timestamp (ie, it's been 3000 ms since it last checked in).
   */
  @Column(name = "LAST_HEARD_FROM", nullable = false)
  protected Long lastHeardFrom;

  /**
   * The capabilities the agent has Capabilities are the devices this agent can record from, with a friendly name
   * associated to determine their nature (e.g. PRESENTER --> dev/video0)
   */
  @Column(name = "CAPABILITIES", nullable = true)
  protected Properties capabilities;

  /**
   * Required 0-arg constructor for JAXB, creates a blank agent.
   */
  public Agent() {
  };

  /**
   * Builds a representation of the agent.
   * 
   * @param agentName
   *          The name of the agent.
   * @param agentState
   *          The state of the agent. This should be defined from the constants in AgentState
   */
  public Agent(String agentName, String agentState, String agentUrl, Properties capabilities) {
    name = agentName;
    this.setState(agentState);
    this.setUrl(agentUrl);
    // Agents with no capabilities are allowed. These can/will be updated after the agent is built if necessary.
    this.capabilities = capabilities;
  }

  /**
   * Gets the name of the agent.
   * 
   * @return The name of the agent.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the state of the agent, and updates the time it was last heard from.
   * 
   * @param newState
   *          The new state of the agent. This should defined from the constants in
   *          {@link org.opencastproject.capture.api.api.AgentState}. This can be equal to the current one if the goal
   *          is to update the timestamp.
   * @see AgentState
   */
  public void setState(String newState) {
    state = newState;
    setLastHeardFrom(System.currentTimeMillis());
  }

  /**
   * Gets the state of the agent.
   * 
   * @return The state of the agent. This should be defined from the constants in
   *         {@link org.opencastproject.capture.api.api.AgentState}.
   * @see AgentState
   */
  public String getState() {
    return state;
  }

  /**
   * Sets the url of the agent.
   * 
   * @param agentUrl
   *          The url of the agent as determined by the referer header field of its request while registering
   */
  public void setUrl(String agentUrl) {
    url = agentUrl;
  }

  /**
   * Gets the url of the agent.
   * 
   * @return the url of the agent.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the time at which the agent last checked in.
   * 
   * @param time
   *          The number of milliseconds since 1970 when the agent last checked in.
   */
  public void setLastHeardFrom(Long time) {
    lastHeardFrom = time;
  }

  /**
   * Gets the time at which the agent last checked in.
   * 
   * @return The number of milliseconds since 1970 when the agent last checked in.
   */
  public Long getLastHeardFrom() {
    return lastHeardFrom;
  }

  /**
   * Gets the capture agent's capability list.
   * 
   * @return The agent's capabilities, or null if there is an error.
   */
  public Properties getCapabilities() {
    return capabilities;
  }

  /**
   * Sets the capture agent's capability list.
   * 
   * @param capabilities
   *          The agent's capabilities.
   */
  public void setCapabilities(Properties capabilities) {
    this.capabilities = capabilities;
  }
}
