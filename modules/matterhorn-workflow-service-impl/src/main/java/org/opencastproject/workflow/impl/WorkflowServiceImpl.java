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

import org.opencastproject.mediapackage.MediaPackage;
import org.opencastproject.mediapackage.MediaPackageMetadata;
import org.opencastproject.metadata.api.MediaPackageMetadataService;
import org.opencastproject.util.NotFoundException;
import org.opencastproject.workflow.api.ResumableWorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowBuilder;
import org.opencastproject.workflow.api.WorkflowDefinition;
import org.opencastproject.workflow.api.WorkflowInstance;
import org.opencastproject.workflow.api.WorkflowInstanceImpl;
import org.opencastproject.workflow.api.WorkflowOperationDefinition;
import org.opencastproject.workflow.api.WorkflowOperationException;
import org.opencastproject.workflow.api.WorkflowOperationHandler;
import org.opencastproject.workflow.api.WorkflowOperationInstance;
import org.opencastproject.workflow.api.WorkflowOperationInstanceImpl;
import org.opencastproject.workflow.api.WorkflowOperationResult;
import org.opencastproject.workflow.api.WorkflowOperationResultImpl;
import org.opencastproject.workflow.api.WorkflowQuery;
import org.opencastproject.workflow.api.WorkflowSelectionStrategy;
import org.opencastproject.workflow.api.WorkflowService;
import org.opencastproject.workflow.api.WorkflowSet;
import org.opencastproject.workflow.api.WorkflowInstance.WorkflowState;
import org.opencastproject.workflow.api.WorkflowOperationInstance.OperationState;
import org.opencastproject.workflow.api.WorkflowOperationResult.Action;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Implements WorkflowService with in-memory data structures to hold WorkflowOperations and WorkflowInstances.
 * WorkflowOperationHandlers are looked up in the OSGi service registry based on the "workflow.operation" property.
 * If the WorkflowOperationHandler's "workflow.operation" service registration property matches
 * WorkflowOperation.getName(), then the factory returns a WorkflowOperationRunner to handle that operation.
 * This allows for custom runners to be added or modified without affecting the workflow service itself.
 */
public class WorkflowServiceImpl implements WorkflowService, ManagedService {

  /** Logging facility */
  private static final Logger logger = LoggerFactory.getLogger(WorkflowServiceImpl.class);

  protected static final String WORKFLOW_DEFINITION_DEFAULT = "org.opencastproject.workflow.default.definition";

  /** TODO: Remove references to the component context once felix scr 1.2 becomes available */
  protected ComponentContext componentContext = null;

  /** The collection of workflow definitions */
  protected Map<String, WorkflowDefinition> workflowDefinitions = new HashMap<String, WorkflowDefinition>();

  /** The metadata services */
  private SortedSet<MediaPackageMetadataService> metadataServices;

  /**
   * A tuple of a workflow operation handler and the name of the operation it handles
   */
  public static class HandlerRegistration {
    public HandlerRegistration(String operationName, WorkflowOperationHandler handler) {
      this.operationName = operationName;
      this.handler = handler;
    }

    WorkflowOperationHandler handler;
    String operationName;

    public WorkflowOperationHandler getHandler() {
      return handler;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((handler == null) ? 0 : handler.hashCode());
      result = prime * result + ((operationName == null) ? 0 : operationName.hashCode());
      return result;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      HandlerRegistration other = (HandlerRegistration) obj;
      if (handler == null) {
        if (other.handler != null)
          return false;
      } else if (!handler.equals(other.handler))
        return false;
      if (operationName == null) {
        if (other.operationName != null)
          return false;
      } else if (!operationName.equals(other.operationName))
        return false;
      return true;
    }
  }

  /** The data access object responsible for storing and retrieving workflow instances */
  protected WorkflowServiceImplDao dao;

  /**
   * Constructs a new workflow service impl, with a priority-sorted map of metadata services
   */
  public WorkflowServiceImpl() {
    metadataServices = new TreeSet<MediaPackageMetadataService>(new Comparator<MediaPackageMetadataService>() {
      @Override
      public int compare(MediaPackageMetadataService o1, MediaPackageMetadataService o2) {
        return o1.getPriority() - o2.getPriority();
      }
    });
  }

  /**
   * Sets the DAO implementation to use in this service.
   * 
   * @param dao
   *          The dao to use for persistence
   */
  public void setDao(WorkflowServiceImplDao dao) {
    this.dao = dao;
  }

  public void addMetadataService(MediaPackageMetadataService service) {
    metadataServices.add(service);
  }

  public void removeMetadataService(MediaPackageMetadataService service) {
    metadataServices.remove(service);
  }

  /**
   * Activate this service implementation via the OSGI service component runtime
   */
  public void activate(ComponentContext componentContext) {
    this.componentContext = componentContext;
  }

  /**
   * Deactivate this service.
   */
  public void deactivate() {
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
   */
  @SuppressWarnings("unchecked")
  public void updated(Dictionary props) throws ConfigurationException {
    // Update any configuration properties here
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#listAvailableWorkflowDefinitions()
   */
  public List<WorkflowDefinition> listAvailableWorkflowDefinitions() {
    List<WorkflowDefinition> list = new ArrayList<WorkflowDefinition>();
    for (Entry<String, WorkflowDefinition> entry : workflowDefinitions.entrySet()) {
      list.add((WorkflowDefinition) entry.getValue());
    }
    Collections.sort(list, new Comparator<WorkflowDefinition>() {
      public int compare(WorkflowDefinition o1, WorkflowDefinition o2) {
        return o1.getId().compareTo(o2.getId());
      }
    });
    return list;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#isRunnable(org.opencastproject.workflow.api.WorkflowDefinition)
   */
  public boolean isRunnable(WorkflowDefinition workflowDefinition) {
    List<String> availableOperations = listAvailableOperationNames();
    List<WorkflowDefinition> checkedWorkflows = new ArrayList<WorkflowDefinition>();
    boolean runnable = isRunnable(workflowDefinition, availableOperations, checkedWorkflows);
    int wfCount = checkedWorkflows.size() - 1;
    if (runnable)
      logger.info("Workflow {}, containing {} derived workflows, is runnable", workflowDefinition, wfCount);
    else
      logger.warn("Workflow {}, containing {} derived workflows, is not runnable", workflowDefinition, wfCount);
    return runnable;
  }

  /**
   * Tests the workflow definition for its runnability. This method is a helper for
   * {@link #isRunnable(WorkflowDefinition)} that is suited for recursive calling.
   * 
   * @param workflowDefinition
   *          the definition to test
   * @param availableOperations
   *          list of currently available operation handlers
   * @param checkedWorkflows
   *          list of checked workflows, used to avoid circular checking
   * @return <code>true</code> if all bits and pieces used for executing <code>workflowDefinition</code> are in place
   */
  private boolean isRunnable(WorkflowDefinition workflowDefinition, List<String> availableOperations,
          List<WorkflowDefinition> checkedWorkflows) {
    if (checkedWorkflows.contains(workflowDefinition))
      return true;

    // Test availability of operation handler and catch workflows
    for (WorkflowOperationDefinition op : workflowDefinition.getOperations()) {
      if (!availableOperations.contains(op.getId())) {
        logger.info("{} is not runnable due to missing operation {}", workflowDefinition, op);
        return false;
      }
      String catchWorkflow = op.getExceptionHandlingWorkflow();
      if (catchWorkflow != null) {
        WorkflowDefinition catchWorkflowDefinition = getWorkflowDefinitionById(catchWorkflow);
        if (catchWorkflowDefinition == null) {
          logger.info("{} is not runnable due to missing catch workflow {} on operation {}", new Object[] {
                  workflowDefinition, catchWorkflow, op });
          return false;
        }
        if (!isRunnable(catchWorkflowDefinition, availableOperations, checkedWorkflows))
          return false;
      }
    }

    // Add the workflow to the list of checked workflows
    if (!checkedWorkflows.contains(workflowDefinition))
      checkedWorkflows.add(workflowDefinition);
    return true;
  }

  /**
   * Gets the currently registered workflow operation handlers.
   * 
   * @return All currently registered handlers
   */
  public Set<HandlerRegistration> getRegisteredHandlers() {
    Set<HandlerRegistration> set = new HashSet<HandlerRegistration>();
    ServiceReference[] refs;
    try {
      refs = componentContext.getBundleContext().getServiceReferences(WorkflowOperationHandler.class.getName(), null);
    } catch (InvalidSyntaxException e) {
      throw new RuntimeException(e);
    }
    for (ServiceReference ref : refs) {
      WorkflowOperationHandler handler = (WorkflowOperationHandler) componentContext.getBundleContext().getService(ref);
      set.add(new HandlerRegistration((String) ref.getProperty(WORKFLOW_OPERATION_PROPERTY), handler));
    }
    return set;
  }

  protected WorkflowOperationHandler getWorkflowOperationHandler(String operationId) {
    for (HandlerRegistration reg : getRegisteredHandlers()) {
      if (reg.operationName.equals(operationId))
        return reg.handler;
    }
    return null;
  }

  /**
   * Lists the names of each workflow operation. Operation names are availalbe for use if there is a registered
   * {@link WorkflowOperationHandler} with an equal {@link WorkflowServiceImpl#WORKFLOW_OPERATION_PROPERTY} property.
   * 
   * @return The {@link List} of available workflow operation names
   */
  protected List<String> listAvailableOperationNames() {
    List<String> list = new ArrayList<String>();
    for (HandlerRegistration reg : getRegisteredHandlers()) {
      list.add(reg.operationName);
    }
    return list;
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.workflow.api.WorkflowService#registerWorkflowDefinition(org.opencastproject.workflow.api.WorkflowDefinition)
   */
  public void registerWorkflowDefinition(WorkflowDefinition workflow) {
    if(workflow == null || workflow.getId() == null) {
      throw new IllegalArgumentException("Workflow must not be null, and must contain an ID");
    }
    String id = workflow.getId();
    if(workflowDefinitions.containsKey(id)) {
      throw new IllegalStateException("A workflow definition with ID '" + id + "' is already registered.");
    }
    workflowDefinitions.put(id, workflow);
  }

  /**
   * {@inheritDoc}
   * @see org.opencastproject.workflow.api.WorkflowService#unregisterWorkflowDefinition(java.lang.String)
   */
  public void unregisterWorkflowDefinition(String workflowDefinitionId) {
    workflowDefinitions.remove(workflowDefinitionId);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#getWorkflowById(java.lang.String)
   */
  public WorkflowInstance getWorkflowById(String id) {
    return dao.getWorkflowById(id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#start(org.opencastproject.workflow.api.WorkflowDefinition,
   *      org.opencastproject.mediapackage.MediaPackage, java.lang.String, java.util.Map)
   */
  @Override
  public WorkflowInstance start(WorkflowDefinition workflowDefinition, MediaPackage mediaPackage,
          String parentWorkflowId, Map<String, String> properties) {
    if (workflowDefinition == null)
      throw new IllegalArgumentException("workflow definition must not be null");
    if (mediaPackage == null)
      throw new IllegalArgumentException("mediapackage must not be null");
    if (parentWorkflowId != null && getWorkflowById(parentWorkflowId) == null)
      throw new IllegalArgumentException("Parent workflow " + parentWorkflowId + " not found");

    String id = UUID.randomUUID().toString();
    logger.info("Starting a new workflow instance with ID={}", id);

    WorkflowInstanceImpl workflowInstance = new WorkflowInstanceImpl(workflowDefinition, mediaPackage,
            parentWorkflowId, properties);
    workflowInstance.setId(id);
    workflowInstance.setState(WorkflowInstance.WorkflowState.RUNNING);

    WorkflowInstance configuredInstance = updateConfiguration(workflowInstance, properties);

    // Before we persist this, extract the metadata
    populateMediaPackageMetadata(configuredInstance.getMediaPackage());

    dao.update(configuredInstance);
    run(configuredInstance);
    return configuredInstance;
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#start(org.opencastproject.workflow.api.WorkflowDefinition,
   *      org.opencastproject.mediapackage.MediaPackage)
   */
  public WorkflowInstance start(WorkflowDefinition workflowDefinition, MediaPackage mediaPackage,
          Map<String, String> properties) {
    return start(workflowDefinition, mediaPackage, null, properties);
  }

  protected WorkflowInstance updateConfiguration(WorkflowInstance instance, Map<String, String> properties) {
    if (properties == null)
      return instance;
    try {
      String xml = replaceVariables(WorkflowBuilder.getInstance().toXml(instance), properties);
      WorkflowInstanceImpl workflow = (WorkflowInstanceImpl) WorkflowBuilder.getInstance().parseWorkflowInstance(xml);
      workflow.init(); // needed to keep the current operation setting intact
      return workflow;
    } catch (Exception e) {
      throw new IllegalStateException("Unable to replace workflow instance variables", e);
    }
  }

  // TODO: this could be far more efficient, consider using e.g. velocity or freemarker
  protected String replaceVariables(String source, Map<String, String> properties) {
    if (properties == null) {
      return source;
    } else {
      for (Entry<String, String> prop : properties.entrySet()) {
        String key = "\\$\\{" + prop.getKey() + "\\}";
        source = source.replaceAll(key, prop.getValue());
      }
      return source;
    }
  }

  /**
   * Does a lookup of available operation handlers for the given workflow operation.
   * 
   * @param operation
   *          the operation definition
   * @return the handler or <code>null</code>
   */
  protected WorkflowOperationHandler selectOperationHandler(WorkflowOperationInstance operation) {
    List<WorkflowOperationHandler> handlerList = new ArrayList<WorkflowOperationHandler>();
    for (HandlerRegistration handlerReg : getRegisteredHandlers()) {
      if (handlerReg.operationName != null && handlerReg.operationName.equals(operation.getId())) {
        handlerList.add(handlerReg.handler);
      }
    }
    // Select one of the possibly multiple operation handlers. TODO Allow for a pluggable strategy for this mechanism
    if (handlerList.size() > 0) {
      int index = (int) Math.round((handlerList.size() - 1) * Math.random());
      return handlerList.get(index);
    }
    logger.warn("No workflow operation handlers found for operation {}", operation.getId());
    return null;
  }

  Executor ex = Executors.newCachedThreadPool();

  protected void run(final WorkflowInstance wfi) {
    WorkflowOperationInstance operation = wfi.getCurrentOperation();
    if (operation == null)
      operation = wfi.next();
    WorkflowOperationHandler operationHandler = selectOperationHandler(operation);
    // If there is no handler for the operation, mark this workflow as failed
    if (operationHandler == null) {
      logger.warn("No handler available to execute operation {}", operation);
      throw new IllegalStateException("Unable to find a workflow handler for " + operation);
    }
    ex.execute(new WorkflowOperationWorker(operationHandler, wfi, this));
  }

  /**
   * Returns the workflow identified by <code>id</code> or <code>null</code> if no such definition was found.
   * 
   * @param id
   *          the workflow definition id
   * @return the workflow
   */
  public WorkflowDefinition getWorkflowDefinitionById(String id) {
    return workflowDefinitions.get(id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#stop(java.lang.String)
   */
  public void stop(String workflowInstanceId) throws NotFoundException {
    WorkflowInstanceImpl instance = (WorkflowInstanceImpl) getWorkflowById(workflowInstanceId);
    if(instance == null) throw new NotFoundException("Workflow ID='" + workflowInstanceId + "' does not exist");
    instance.setState(WorkflowState.STOPPED);
    update(instance);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#suspend(java.lang.String)
   */
  public void suspend(String workflowInstanceId) throws NotFoundException {
    WorkflowInstanceImpl instance = (WorkflowInstanceImpl) getWorkflowById(workflowInstanceId);
    if(instance == null) throw new NotFoundException("Workflow ID='" + workflowInstanceId + "' does not exist");
    instance.setState(WorkflowState.PAUSED);
    update(instance);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#resume(java.lang.String)
   */
  @Override
  public void resume(String id) throws NotFoundException {
    resume(id, new HashMap<String, String>());
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#resume(java.lang.String)
   */
  public void resume(String workflowInstanceId, Map<String, String> properties) throws NotFoundException {
    WorkflowInstanceImpl workflowInstance = (WorkflowInstanceImpl) updateConfiguration(
            getWorkflowById(workflowInstanceId), properties);
    if(workflowInstance == null) throw new NotFoundException("Workflow ID='" + workflowInstanceId + "' does not exist");
    workflowInstance.setState(WorkflowInstance.WorkflowState.RUNNING);
    dao.update(workflowInstance);
    run(workflowInstance);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#update(org.opencastproject.workflow.api.WorkflowInstance)
   */
  public void update(WorkflowInstance workflowInstance) {
    dao.update(workflowInstance);
  }

  /**
   * Removes a workflow instance.
   * 
   * @param id
   *          The id of the workflow instance to remove
   */
  public void removeFromDatabase(String id) {
    dao.remove(id);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#countWorkflowInstances()
   */
  public long countWorkflowInstances() {
    return dao.countWorkflowInstances();
  }

  public WorkflowSet getWorkflowInstances(WorkflowQuery query) {
    return dao.getWorkflowInstances(query);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#newWorkflowQuery()
   */
  public WorkflowQuery newWorkflowQuery() {
    return new WorkflowQueryImpl();
  }

  public void handleOperationException(WorkflowInstance workflow, WorkflowOperationException e) {
    // Add the exception's localized message to the workflow instance
    workflow.addErrorMessage(e.getLocalizedMessage());
    
    WorkflowOperationInstance currentOperation = workflow.getCurrentOperation();
    if (currentOperation.isFailWorkflowOnException()) {
      String errorDefId = currentOperation.getExceptionHandlingWorkflow();
      if (errorDefId != null) {
        int currentOperationPosition = workflow.getOperations().indexOf(currentOperation);
        List<WorkflowOperationInstance> operations = new ArrayList<WorkflowOperationInstance>();
        operations.addAll(workflow.getOperations().subList(0, currentOperationPosition + 1));
        WorkflowDefinition errorDef = getWorkflowDefinitionById(errorDefId);
        if (errorDef == null) {
          throw new IllegalStateException("Unable to run error workflow " + errorDefId);
        }
        for (WorkflowOperationDefinition def : errorDef.getOperations()) {
          operations.add(new WorkflowOperationInstanceImpl(def));
        }
        workflow.getOperations().clear();
        workflow.getOperations().addAll(operations);
      }
    }
    currentOperation.setState(OperationState.FAILED);
    dao.update(workflow);
    handleOperationResult(workflow, new WorkflowOperationResultImpl(workflow.getMediaPackage(), null, Action.CONTINUE));
  }

  /**
   */
  void handleOperationResult(WorkflowInstance workflow, WorkflowOperationResult result) {
    if (result == null) {
      // Just continue using the workflow's current mediapackage, but log a warning
      logger.warn("Handling a null operation result for workflow {}", workflow);
      result = new WorkflowOperationResultImpl(workflow.getMediaPackage(), null, Action.CONTINUE);
    } else {
      // Update the workflow's mediapackage if a new one was produced in this operation
      MediaPackage mp = result.getMediaPackage();
      if (mp != null) {
        workflow.setMediaPackage(mp);
      }
    }
    WorkflowOperationHandler handler = null;
    if (Action.PAUSE.equals(result.getAction())) {
      WorkflowOperationInstance currentOperation = workflow.getCurrentOperation();
      handler = getWorkflowOperationHandler(currentOperation.getId());
      if (!(handler instanceof ResumableWorkflowOperationHandler)) {
        throw new IllegalStateException("Operation " + currentOperation.getId() + " is not resumable");
      }
      ResumableWorkflowOperationHandler resumableHandler = (ResumableWorkflowOperationHandler) handler;
      try {
        URL url = resumableHandler.getHoldStateUserInterfaceURL(workflow);
        if (url != null) {
          String holdActionTitle = resumableHandler.getHoldActionTitle();
          ((WorkflowOperationInstanceImpl) currentOperation).setHoldActionTitle(holdActionTitle);
          ((WorkflowOperationInstanceImpl) currentOperation).setHoldStateUserInterfaceUrl(url);
        }
      } catch (WorkflowOperationException e) {
        logger.warn("unable to replace workflow ID in the hold state URL", e);
      }
      workflow.setState(WorkflowState.PAUSED);
      workflow = updateConfiguration(workflow, result.getProperties());
      dao.update(workflow);
      return;
    }

    WorkflowState dbState = getWorkflowById(workflow.getId()).getState();

    // If the workflow was paused while the operation was still working, accept the updated mediapackage
    // and properties, but do not continue on.
    if (result.getAction().equals(Action.CONTINUE) && WorkflowState.PAUSED.equals(dbState)) {
      workflow.setState(WorkflowState.PAUSED);
      workflow = updateConfiguration(workflow, result.getProperties());
      dao.update(workflow);
      return;
    }

    // If the workflow was stopped while the operation was still working, accept the updated mediapckage
    // and properties, but do not continue on.
    if (WorkflowState.STOPPED.equals(dbState)) {
      workflow.setState(WorkflowState.STOPPED);
      workflow = updateConfiguration(workflow, result.getProperties());
      dao.update(workflow);
      return;
    }

    WorkflowOperationInstance nextOperation = workflow.next(); // Be careful... this increments the current operation
    if (nextOperation == null) {
      if (Action.CONTINUE.equals(result.getAction())) {
        workflow.setState(WorkflowState.SUCCEEDED);
        for (WorkflowOperationInstance op : workflow.getOperations()) {
          if (op.getState().equals(WorkflowOperationInstance.OperationState.FAILED)) {
            if (op.isFailWorkflowOnException()) {
              workflow.setState(WorkflowState.FAILED);
              break;
            }
          }
        }
      } else {
        workflow.setState(WorkflowState.PAUSED);
      }
      dao.update(workflow);
    } else {
      if (Action.CONTINUE.equals(result.getAction())) {
        workflow.setState(WorkflowState.RUNNING);
      } else {
        workflow.setState(WorkflowState.PAUSED);
      }
      dao.update(workflow);
      if (Action.CONTINUE.equals(result.getAction())) {
        this.run(workflow);
      }
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#start(org.opencastproject.workflow.api.WorkflowDefinition,
   *      org.opencastproject.mediapackage.MediaPackage)
   */
  @Override
  public WorkflowInstance start(WorkflowDefinition workflowDefinition, MediaPackage mediaPackage) {
    if (workflowDefinition == null)
      throw new IllegalArgumentException("workflow definition must not be null");
    if (mediaPackage == null)
      throw new IllegalArgumentException("mediapackage must not be null");
    Map<String, String> properties = new HashMap<String, String>();
    return start(workflowDefinition, mediaPackage, properties);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#start(org.opencastproject.mediapackage.MediaPackage,
   *      java.util.Map)
   */
  @Override
  public WorkflowInstance start(MediaPackage mediaPackage, Map<String, String> properties) {
    if (mediaPackage == null)
      throw new IllegalArgumentException("mediapackage must not be null");
    WorkflowDefinition def = getWorkflowDefinition(mediaPackage, properties);
    return start(def, mediaPackage, properties);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.opencastproject.workflow.api.WorkflowService#start(org.opencastproject.mediapackage.MediaPackage)
   */
  @Override
  public WorkflowInstance start(MediaPackage mediaPackage) {
    if (mediaPackage == null)
      throw new IllegalArgumentException("mediapackage must not be null");
    Map<String, String> properties = new HashMap<String, String>();
    WorkflowDefinition def = getWorkflowDefinition(mediaPackage, properties);
    return start(def, mediaPackage, properties);
  }

  /**
   * Gets the workflow definition for a submitted mediapackage and properties.
   * 
   * @param mediaPackage
   * @param properties
   * @return The workflow definition
   * @throws IllegalStateException
   *           if no acceptable workflow definition can be found
   */
  protected WorkflowDefinition getWorkflowDefinition(MediaPackage mediaPackage, Map<String, String> properties) {
    // Get the default workflow, either from a WorkflowSelectionStrategy or from the bundle context
    WorkflowDefinition def = null;
    ServiceReference ref = componentContext.getBundleContext().getServiceReference(
            WorkflowSelectionStrategy.class.getName());
    if (ref != null) {
      WorkflowSelectionStrategy strategy = (WorkflowSelectionStrategy) componentContext.getBundleContext().getService(
              ref);
      def = strategy.getWorkflowDefinition(mediaPackage, properties);
    }
    // Still no definition? Try finding the default workflow definition ID in the bundle context
    if (def == null) {
      String defaultId = componentContext.getBundleContext().getProperty(WORKFLOW_DEFINITION_DEFAULT);
      if (defaultId != null) {
        def = getWorkflowDefinitionById(defaultId);
      }
    }
    // If there is still no definition defined, we can not continue
    if (def == null)
      throw new IllegalStateException("Unable to determine the default workflow definition");
    return def;
  }

  /**
   * Reads the available metadata from the dublin core catalog (if there is one).
   * 
   * @param mp
   *          the media package
   */
  protected void populateMediaPackageMetadata(MediaPackage mp) {
    if (metadataServices.size() == 0) {
      logger.warn("No metadata services are registered, so no mediapackage metadata can be extracted from catalogs");
      return;
    }
    for (MediaPackageMetadataService metadataService : metadataServices) {
      MediaPackageMetadata metadata = metadataService.getMetadata(mp);
      if (metadata != null) {
        if (mp.getDate().getTime() == 0) {
          mp.setDate(metadata.getDate());
        }
        if (mp.getLanguage() == null || mp.getLanguage().isEmpty()) {
          mp.setLanguage(metadata.getLanguage());
        }
        if (mp.getLicense() == null || mp.getLicense().isEmpty()) {
          mp.setLicense(metadata.getLicense());
        }
        if (mp.getSeries() == null || mp.getSeries().isEmpty()) {
          mp.setSeries(metadata.getSeriesIdentifier());
        }
        if (mp.getSeriesTitle() == null || mp.getSeriesTitle().isEmpty()) {
          mp.setSeriesTitle(metadata.getSeriesTitle());
        }
        if (mp.getTitle() == null || mp.getTitle().isEmpty()) {
          mp.setTitle(metadata.getTitle());
        }
      }
    }
  }
}
