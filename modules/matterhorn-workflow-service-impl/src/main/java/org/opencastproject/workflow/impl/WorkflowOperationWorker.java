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
package org.opencastproject.workflow.impl;

import org.opencastproject.workflow.api.ResumableWorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowDatabaseException;
import org.opencastproject.workflow.api.WorkflowInstanceImpl;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationInstance.OperationState;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.api.WorkflowOperationResult.Action;
import org.opencastproject.workflow.api.WorkflowParsingException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Handles execution of a workflow operation.
 */
final class WorkflowOperationWorker {

  private static final Logger logger = LoggerFactory.getLogger(WorkflowOperationWorker.class);

  protected WorkflowOperationHandler handler = null;
  protected WorkflowInstanceImpl workflow = null;
  protected WorkflowServiceImpl service = null;
  protected Map<String, String> properties = null;

  /**
   * Creates a worker that will execute the given handler and thereby the current operation of the workflow instance.
   * When the worker is finished, a callback will be made to the workflow service reporting either success or failure of
   * the current workflow operation.
   * 
   * @param handler
   *          the workflow operation handler
   * @param workflow
   *          the workflow instance
   * @param service
   *          the workflow service.
   */
  public WorkflowOperationWorker(WorkflowOperationHandler handler, WorkflowInstanceImpl workflow,
          WorkflowServiceImpl service) {
    this.handler = handler;
    this.workflow = workflow;
    this.service = service;
  }

  /**
   * Creates a worker that will execute the given handler and thereby the current operation of the workflow instance.
   * When the worker is finished, a callback will be made to the workflow service reporting either success or failure of
   * the current workflow operation.
   * 
   * @param handler
   *          the workflow operation handler
   * @param workflow
   *          the workflow instance
   * @param properties
   *          the properties used to execute the operation
   * @param service
   *          the workflow service.
   */
  public WorkflowOperationWorker(WorkflowOperationHandler handler, WorkflowInstanceImpl workflow,
          Map<String, String> properties, WorkflowServiceImpl service) {
    this(handler, workflow, service);
    this.properties = properties;
  }

  /**
   * Creates a worker that still needs an operation handler to be set. When the worker is finished, a callback will be
   * made to the workflow service reporting either success or failure of the current workflow operation.
   * 
   * @param workflow
   *          the workflow instance
   * @param properties
   *          the properties used to execute the operation
   * @param service
   *          the workflow service.
   */
  public WorkflowOperationWorker(WorkflowInstanceImpl workflow, Map<String, String> properties,
          WorkflowServiceImpl service) {
    this(null, workflow, service);
    this.properties = properties;
  }

  /**
   * Sets the workflow operation handler to use.
   * 
   * @param operationHandler
   *          the handler
   */
  public void setHandler(WorkflowOperationHandler operationHandler) {
    this.handler = operationHandler;
  }

  /**
   * Executes the workflow operation logic.
   */
  public void execute() {
    WorkflowOperationInstance operation = workflow.getCurrentOperation();
    if (handler == null)
      throw new IllegalStateException("Handler must be set first");
    try {
      WorkflowOperationResult result = null;
      switch (operation.getState()) {
        case INSTANTIATED:
          result = start();
          break;
        case PAUSED:
          result = resume();
          break;
        default:
          throw new IllegalStateException("Workflow operation '" + operation + "' is in unexpected state '"
                  + operation.getState() + "'");
      }
      if (result == null || Action.CONTINUE.equals(result.getAction()) || Action.SKIP.equals(result.getAction())) {
        if (handler != null) {
          handler.destroy(workflow);
        }
      }
      service.handleOperationResult(workflow, result);
    } catch (Exception e) {
      logger.error("Workflow operation '{}' failed with error: {}", handler, e.getMessage());
      Throwable t = e.getCause();
      if (t != null) {
        logger.error("Original cause for the failure is: {}", t.getMessage(), t);
      } else {
        logger.error("Cause for the failure is", e);
      }
      try {
        service.handleOperationException(workflow, e);
      } catch (Exception e2) {
        logger.error("Error handling workflow operation '{}' failure: {}",
                new Object[] { handler, e2.getMessage(), e2 });
        e.printStackTrace();
      }
    }
  }

  /**
   * Starts executing the workflow operation.
   * 
   * @return the workflow operation result
   * @throws WorkflowOperationException
   *           if executing the workflow operation handler fails
   * @throws WorkflowDatabaseException
   *           if updating the workflow fails
   * @throws WorkflowParsingException
   *           if serializing the workflow fails
   */
  public WorkflowOperationResult start() throws WorkflowOperationException, WorkflowDatabaseException,
          WorkflowParsingException {
    WorkflowOperationInstance operation = workflow.getCurrentOperation();

    // Do we need to execute the operation?
    String executeCondition = operation.getExecutionCondition(); // if
    String skipCondition = operation.getSkipCondition(); // unless

    if (StringUtils.isNotBlank(executeCondition)
            && WorkflowServiceImpl.PROPERTY_PATTERN.matcher(executeCondition).matches()) {
      operation.setState(OperationState.SKIPPED);
    } else if (StringUtils.isNotBlank(skipCondition)
            && !WorkflowServiceImpl.PROPERTY_PATTERN.matcher(skipCondition).matches()) {
      operation.setState(OperationState.SKIPPED);
    } else {
      operation.setState(OperationState.RUNNING);
    }

    try {
      WorkflowOperationResult result = null;
      if (OperationState.SKIPPED.equals(operation.getState())) {
        // Allow for null handlers when we are skipping an operation
        if (handler != null) {
          result = handler.skip(workflow);
        }
      } else {
        if (handler == null) {
          // If there is no handler for the operation, yet we are supposed to run it, we must fail
          logger.warn("No handler available to execute operation '{}'", operation.getId());
          throw new IllegalStateException("Unable to find a workflow handler for '" + operation.getId() + "'");
        }
        result = handler.start(workflow);
      }
      if (result != null && Action.PAUSE.equals(result.getAction())) {
        operation.setState(OperationState.PAUSED);
      } else if (result != null && Action.SKIP.equals(result.getAction())) {
        operation.setState(OperationState.SKIPPED);
      } else {
        operation.setState(OperationState.SUCCEEDED);
      }
      return result;
    } catch (Exception e) {
      operation.setState(OperationState.FAILED);
      if (e instanceof WorkflowOperationException)
        throw (WorkflowOperationException) e;
      throw new WorkflowOperationException(e);
    }
  }

  /**
   * Resumes a previously suspended workflow operation. Note that only workflow operation handlers that implement
   * {@link ResumableWorkflowOperationHandler} can be resumed.
   * 
   * @return the workflow operation result
   * @throws WorkflowOperationException
   *           if executing the workflow operation handler fails
   * @throws WorkflowDatabaseException
   *           if updating the workflow fails
   * @throws WorkflowParsingException
   *           if serializing the workflow fails
   * @throws IllegalStateException
   *           if the workflow operation cannot be resumed
   */
  public WorkflowOperationResult resume() throws WorkflowOperationException, WorkflowDatabaseException,
          WorkflowParsingException, IllegalStateException {
    if (!(handler instanceof ResumableWorkflowOperationHandler)) {
      throw new IllegalStateException("An attempt was made to resume a non-resumable operation");
    }
    ResumableWorkflowOperationHandler resumableHandler = (ResumableWorkflowOperationHandler) handler;
    WorkflowOperationInstance operation = workflow.getCurrentOperation();
    operation.setState(OperationState.RUNNING);

    try {
      WorkflowOperationResult result = resumableHandler.resume(workflow, properties);
      if (result == null || Action.CONTINUE.equals(result.getAction())) {
        operation.setState(OperationState.SUCCEEDED);
      }
      if (result != null && Action.PAUSE.equals(result.getAction())) {
        operation.setState(OperationState.PAUSED);
      }
      return result;
    } catch (Exception e) {
      operation.setState(OperationState.FAILED);
      if (e instanceof WorkflowOperationException)
        throw (WorkflowOperationException) e;
      throw new WorkflowOperationException(e);
    }
  }

}
