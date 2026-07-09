package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.dto.*;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.service.QwenService;
import hgc.flowsyncapi.service.TaskInfoService;
import hgc.flowsyncapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private QwenService qwenService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskInfoService taskInfoService;

    // 单任务 AI 执行建议
    @PostMapping("/task-suggestion")
    public ApiResponse<Map<String, String>> getTaskSuggestion(@RequestBody AiTaskSuggestionRequest request) {
        String suggestion = qwenService.getTaskSuggestion(
                request.getProjectName(),
                request.getTitle(),
                request.getDescription()
        );
        Map<String, String> result = new HashMap<>();
        result.put("suggestion", suggestion);
        return ApiResponse.success(result);
    }

    // AI 任务规划拆解
    @PostMapping("/task-plan")
    public ApiResponse<AiTaskPlanResponse> getTaskPlan(@RequestBody AiTaskPlanRequest request) {
        // 加载全部成员名单用于大模型负责人推荐
        List<User> members = userService.list();
        AiTaskPlanResponse planResponse = qwenService.getTaskPlan(
                request.getProjectId(),
                request.getProjectName(),
                request.getGoal(),
                request.getDescription(),
                members
        );
        return ApiResponse.success(planResponse);
    }

    // 批量导入大模型拆解出的子任务
    @PostMapping("/task-plan/import")
    public ApiResponse<String> importTaskPlan(@RequestBody AiTaskPlanImportRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ApiResponse.error("未检测到需要导入的任务列表项");
        }

        for (AiTaskPlanItem item : request.getItems()) {
            TaskInfo task = new TaskInfo();
            task.setProjectId(request.getProjectId());
            task.setCreatorId(request.getCreatorId());
            task.setAssigneeId(item.getAssigneeId());
            task.setTitle(item.getTitle());
            task.setDescription(item.getDescription());
            task.setPriority(item.getPriority() != null ? item.getPriority() : "中");
            task.setStatus("未开始");
            task.setCreateTime(LocalDateTime.now());
            
            // 根据 AI 建议的天数动态计算截止日期 (默认为 3 天)
            int days = item.getSuggestedDays() != null ? item.getSuggestedDays() : 3;
            task.setDueDate(LocalDate.now().plusDays(days));
            
            taskInfoService.save(task);
        }

        return ApiResponse.success(String.format("已成功导入 %d 项任务至该项目中", request.getItems().size()));
    }
}
