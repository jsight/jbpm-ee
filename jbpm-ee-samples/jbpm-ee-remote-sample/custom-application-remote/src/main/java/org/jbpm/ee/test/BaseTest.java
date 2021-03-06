package org.jbpm.ee.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jboss.ejb.client.EJBClientContext;
import org.jbpm.ee.services.ProcessService;
import org.jbpm.ee.services.TaskService;
import org.jbpm.ee.services.WorkItemService;
import org.jbpm.ee.services.ejb.interceptors.SerializationInterceptor;
import org.jbpm.ee.services.model.KieReleaseId;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.api.task.model.TaskSummary;
import org.slf4j.Logger;

public abstract class BaseTest {
	
	static {
		EJBClientContext.getCurrent().registerInterceptor(0, new SerializationInterceptor());
	}
	
	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(BaseTest.class);
	
	protected abstract ProcessService getProcessService();
	protected abstract TaskService getTaskService();
	protected abstract WorkItemService getWorkItemService();
	
	protected static final KieReleaseId taskTestReleaseId = new KieReleaseId("org.jbpm.jbpm-ee", "jbpm-ee-kjar-sample", "1.0.0-SNAPSHOT");
	protected static final String taskTestProcessId = "testTaskProcess.bpmn2";
	
	protected static final KieReleaseId loanTestReleaseId = new KieReleaseId("org.jbpm.jbpm-ee", "jbpm-ee-kjar-sample", "1.0.0-SNAPSHOT");
	protected static final String loanTestProcessId = "testWorkItemProcess.bpmn2";
	
	@WebMethod
	public Long startProcess() {
		ProcessService processService =  getProcessService();
		
		final String variableKey = "processString";
		
		Map<String, Object> processVariables = new HashMap<String, Object>();
		processVariables.put(variableKey, "Initial");
		
		ProcessInstance processInstance = processService.startProcess(taskTestReleaseId, taskTestProcessId, processVariables);
		LOG.info("Process Instance: "+processInstance.getId());
		
		return processInstance.getId();
	}
	
	@WebMethod
	public int taskCount() {
		TaskService taskService = getTaskService();;
		List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner("abaxter", "en-UK");
		
		return tasks.size();
	}
	
	@WebMethod
	public String listTasks() {

		TaskService service = getTaskService();
		List<TaskSummary> tasks = service.getTasksAssignedAsPotentialOwner("abaxter", "en-UK");
		
		StringBuilder builder = new StringBuilder();
		for(TaskSummary summary : tasks) {
			builder.append("Task: "+summary.getId()+", "+summary.getName()+", "+summary.getStatus()+"\n");
		}
			
		return builder.toString();
	}
	
	@WebMethod
	public String completeTaskAsNull() {

		TaskService service = getTaskService();
		List<TaskSummary> tasks = service.getTasksAssignedAsPotentialOwner("abaxter", "en-UK");
		
		int i=0;
		for(TaskSummary summary : tasks) {
			if(summary.getActualOwner() == null || !"abaxter".equals(summary.getActualOwner().getId())) {
					service.claim(summary.getId(), "abaxter");
					service.start(summary.getId(), "abaxter");
			}
			
			service.complete(summary.getId(), "abaxter", null);
			i++;
		}
		
		return "Completed: "+i;
	}
	
	@WebMethod
	public String completeTasks() {

		TaskService service = getTaskService();
		List<TaskSummary> tasks = service.getTasksAssignedAsPotentialOwner("abaxter", "en-UK");
		
		int i=0;
		for(TaskSummary summary : tasks) {
			service.claim(summary.getId(), "abaxter");
			service.start(summary.getId(), "abaxter");
			
			Map<String,Object> testResults = new HashMap<String,Object>();
			testResults.put("ResultValue", "Hello World");
			service.complete(summary.getId(), "abaxter", testResults);
			i++;
		}
		
		return "Completed: "+i;
	}
	
	@WebMethod
	public String describeWorkItemsForOpenTasks() {
		TaskService service = getTaskService();
		List<TaskSummary> tasks = service.getTasksAssignedAsPotentialOwner("abaxter", "en-UK");
		
		StringBuilder builder = new StringBuilder();
		for(TaskSummary taskSummary : tasks) {
			Task task = service.getTaskById(taskSummary.getId());
			TaskData data = task.getTaskData();
			
			WorkItem workItem = getWorkItemService().getWorkItem(data.getWorkItemId());
			
			builder.append(ReflectionToStringBuilder.toString(workItem));
			builder.append("\n");
		}
		
		return builder.toString();
	}
	

	@WebMethod
	public Long testLoanProcess() throws Exception {
		final String variableKey = "loanOrder";
		
		Map<String, Object> processVariables = new HashMap<String, Object>();
		LoanOrder order = new LoanOrder();
		order.setFirstName("Adam");
		order.setLastName("Baxter");
		order.setLoanAmount(500000L);
		processVariables.put(variableKey, order);
		
		ProcessService processService = getProcessService();
		
		ProcessInstance processInstance = processService.startProcess(loanTestReleaseId, loanTestProcessId, processVariables);
		return processInstance.getId();
	}
	

	
	@WebMethod
	public Long startProcessNullValues() {
		ProcessService processService =  getProcessService();
		
		Map<String, Object> processVariables = null;
		ProcessInstance processInstance = processService.startProcess(taskTestReleaseId, taskTestProcessId, processVariables);
		LOG.info("Process Instance: "+processInstance.getId());
		
		return processInstance.getId();
	}
	
	@WebMethod
	public String getProcessVariables(long processInstanceId) {
		ProcessService processService =  getProcessService();
		Map<String, Object> variables = processService.getProcessInstanceVariables(processInstanceId);
		
		StringBuilder builder = new StringBuilder();
		builder.append("Total variables: "+variables.size()+"\n");
		for(String key : variables.keySet()) {
			builder.append("Variable: "+key+" :: "+ReflectionToStringBuilder.toString(variables.get(key))+"\n");
		}
		return builder.toString();
	}
	
	@WebMethod
	public String getProcessVariable(long processInstanceId, String key) {
		ProcessService processService =  getProcessService();
		Object variable = processService.getProcessInstanceVariable(processInstanceId, key);
		
		StringBuilder builder = new StringBuilder();
		builder.append("Variable: "+key+" :: "+ReflectionToStringBuilder.toString(variable)+"\n");
		return builder.toString();
	}
	
	
	@WebMethod
	public String setProcessVariable(long processInstanceId, String key, String firstName) {
		ProcessService processService =  getProcessService();
		Object variable = processService.getProcessInstanceVariable(processInstanceId, key);
		
		if(variable != null && variable instanceof LoanOrder) {
			LoanOrder order = (LoanOrder)variable;
			order.setFirstName(firstName);
			processService.setProcessInstanceVariable(processInstanceId, key, order);
		}
		
		return getProcessVariable(processInstanceId, key);
	}
	
	
	
}
