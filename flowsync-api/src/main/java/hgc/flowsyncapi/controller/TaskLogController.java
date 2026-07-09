package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.entity.TaskLog;
import hgc.flowsyncapi.service.TaskLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/task-logs")
public class TaskLogController {

    @Autowired
    private TaskLogService taskLogService;

    // 获取进度记录列表 (可按 taskId 筛选)
    @GetMapping
    public ApiResponse<List<TaskLog>> getTaskLogs(
            @RequestParam(value = "taskId", required = false) Long taskId) {
        List<TaskLog> list = taskLogService.getTaskLogs(taskId);
        return ApiResponse.success(list);
    }

    // 新增进度记录
    @PostMapping
    public ApiResponse<TaskLog> addTaskLog(
            @RequestParam("currentUserId") Long currentUserId,
            @RequestBody TaskLog log) {
        
        log.setCreateTime(LocalDateTime.now());
        log.setOperatorId(currentUserId); // 自动记录操作人为当前用户
        
        if (log.getProgressPercent() == null) {
            log.setProgressPercent(0);
        }
        
        boolean success = taskLogService.save(log);
        if (success) {
            return ApiResponse.success("进度记录已提交", log);
        } else {
            return ApiResponse.error("保存进度记录失败");
        }
    }
}
