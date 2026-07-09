package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.service.TaskInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskInfoService taskInfoService;

    // 获取任务列表 (可按 projectId 筛选，默认按 ID 倒序)
    @GetMapping
    public ApiResponse<List<TaskInfo>> getTasks(
            @RequestParam(value = "projectId", required = false) Long projectId) {
        List<TaskInfo> list = taskInfoService.getTasks(projectId);
        return ApiResponse.success(list);
    }

    // 创建/编辑任务
    @PostMapping
    public ApiResponse<TaskInfo> saveTask(
            @RequestParam("currentUserId") Long currentUserId,
            @RequestBody TaskInfo task) {
        
        if (task.getId() == null) {
            // 新建任务
            task.setCreateTime(LocalDateTime.now());
            task.setCreatorId(currentUserId); // 自动记录创建人
            if (task.getStatus() == null) {
                task.setStatus("未开始");
            }
        }
        
        boolean success = taskInfoService.saveOrUpdate(task);
        if (success) {
            return ApiResponse.success("任务保存成功", task);
        } else {
            return ApiResponse.error("保存任务失败");
        }
    }

    // 删除任务
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteTask(@PathVariable("id") Long id) {
        boolean success = taskInfoService.removeById(id);
        if (success) {
            return ApiResponse.success("任务删除成功", "Task deleted successfully");
        } else {
            return ApiResponse.error("删除任务失败");
        }
    }
}
