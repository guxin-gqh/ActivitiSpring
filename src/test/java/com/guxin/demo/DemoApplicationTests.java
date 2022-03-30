package com.guxin.demo;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

@SpringBootTest
class DemoApplicationTests {
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    TaskService taskService;
    @Autowired
    HistoryService historyService;
    @Test
    void contextLoads() {
    }
    @Test
    public void testStartProcess(){
        ProcessInstance mydemo = runtimeService.startProcessInstanceByKey("guxin","guxin");
        System.out.println(mydemo.getActivityId());
        System.out.println(mydemo.getProcessDefinitionId());
        System.out.println(mydemo.getId());
        System.out.println(mydemo.getName());
    }
    /**
     * 查询当前个人待执行的任务*/
    @Test
    public void testFindPersonTaskList(){
        String assignee="manager";
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey("mydemo")
                .taskAssignee(assignee)
                .list();
        for (Task task : list) {
            System.out.println(task.getProcessInstanceId());
            System.out.println(task.getId());
            System.out.println(task.getAssignee());
            System.out.println(task.getName());
        }
    }
    /**
     * 完成任务*/
    @Test
    public void completeTask(){
        String assignee="manager";
//        根据流程的key和任务的负责人查询任务
//        返回一个任务对象
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("mydemo")
//               .taskAssignee(assignee)
                .processInstanceId("60a25e6e-aff3-11ec-98eb-68f728c08373")
                .singleResult();
//        通过任务id去完成任务
        taskService.complete(task.getId());
    }
    /**
     * 查询流程信息*/
    @Test
    public void queryProcessInstance(){
        String processDefinitionKey="mydemo";
        List<ProcessInstance> list = runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .list();
        for (ProcessInstance processInstance : list) {
            System.out.println("==================");
            System.out.println(processInstance.getProcessDefinitionId());
            System.out.println(processInstance.isSuspended());
            System.out.println(processInstance.isEnded());
            System.out.println(processInstance.getActivityId());
            System.out.println(processInstance.getBusinessKey());
            System.out.println(processInstance.getProcessDefinitionName());
        }
    }
    /**
     * 查询出所有的流程*/
    @Test
    public void  queryProcessDefinition(){
        String processDefinitionKey="mydemo";
        List<ProcessDefinition> list = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .orderByProcessDefinitionVersion()
                .desc()
                .list();
        for (ProcessDefinition processDefinition : list) {
            System.out.println(processDefinition.getId());
            System.out.println(processDefinition.getName());
            System.out.println(processDefinition.getVersion());
            System.out.println(processDefinition.getDeploymentId());
        }
    }

    /**
     * 删除流程*/
    @Test
    public  void deleteDeployment(){
        String deploymentId="d5d8aeca-afed-11ec-a2e9-68f728c08373";
        repositoryService.deleteDeployment(deploymentId);
 //       repositoryService.deleteDeployment(deploymentId,true);后面添加一个true表示级联删除 即使流程有启动的实例也可以删除
    }

    /**
     * 获取bpmn的文件*/
    @Test
    public void queryBpmnFile() throws Exception{
        String processDefinitionKey="mydemo";
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .singleResult();
        String deploymentId = processDefinition.getDeploymentId();
        InputStream bpmnInput = repositoryService.getResourceAsStream(deploymentId, processDefinition.getResourceName());
        File file_bpmn = new File("d:/mydemo.bpmn");
        FileOutputStream bpmnOutput = new FileOutputStream(file_bpmn);
        IOUtils.copy(bpmnInput,bpmnOutput);
        bpmnOutput.close();
    }

    /**
     * 查看历史信息*/
    @Test
    public void findHistoryInfo(){
        HistoricActivityInstanceQuery instanceQuery = historyService.createHistoricActivityInstanceQuery();
        instanceQuery.processDefinitionId("mydemo:2:0e7892aa-afef-11ec-b019-68f728c08373");
        List<HistoricActivityInstance> list = instanceQuery.orderByHistoricActivityInstanceStartTime().asc().list();
        for (HistoricActivityInstance history : list) {
            System.out.println(history.getActivityId());
            System.out.println(history.getActivityName());
            System.out.println(history.getProcessDefinitionId());
            System.out.println(history.getProcessInstanceId());
            System.out.println("========");
        }
    }
    /**
     * 添加业务到key，到Activiti的表
     */
    @Test
    public  void addBusinessKey(){
        String processDefinitionKey="mydemo";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, "1001");
        System.out.println(processInstance.getBusinessKey());
    }

    /**
     * 获取流程实例的挂起和激活*/
    @Test
    public  void suspendAllProcessInstance(){
        String processDefinitionKey="mydemo";
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .singleResult();
        boolean suspended = processDefinition.isSuspended();
        String id = processDefinition.getId();
        if (suspended){
            repositoryService.activateProcessDefinitionById(id,true,null);
            System.out.println(id+"already to active");
        }else {
            repositoryService.suspendProcessDefinitionById(id,true,null);
            System.out.println(id+"already to suspend");
        }
    }
    /**
     挂起激活单个实例
    */
    @Test
    public void suspendSingleProcessInstance(){
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId("").singleResult();
        boolean suspended = processInstance.isSuspended();
        String id = processInstance.getId();
        if (suspended){
            repositoryService.activateProcessDefinitionById(id,true,null);
            System.out.println(id+"already to active");
        }else {
            repositoryService.suspendProcessDefinitionById(id,true,null);
            System.out.println(id+"already to suspend");
        }
    }

}
