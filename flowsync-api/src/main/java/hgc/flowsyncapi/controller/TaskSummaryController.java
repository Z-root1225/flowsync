package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.entity.TaskSummary;
import hgc.flowsyncapi.service.TaskSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/summaries")
public class TaskSummaryController {

    @Autowired
    private TaskSummaryService taskSummaryService;

    // 获取总结列表 (按 ID 倒序)
    @GetMapping
    public ApiResponse<List<TaskSummary>> getSummaries() {
        List<TaskSummary> list = taskSummaryService.getSummaries();
        return ApiResponse.success(list);
    }

    // 新增总结
    @PostMapping
    public ApiResponse<TaskSummary> addSummary(
            @RequestParam("currentUserId") Long currentUserId,
            @RequestBody TaskSummary summary) {
        
        summary.setCreateTime(LocalDateTime.now());
        summary.setCreatedBy(currentUserId); // 自动记录总结撰写人为当前用户
        
        boolean success = taskSummaryService.save(summary);
        if (success) {
            return ApiResponse.success("总结报告提交成功", summary);
        } else {
            return ApiResponse.error("提交总结失败");
        }
    }

    // 修改总结
    @PutMapping
    public ApiResponse<String> updateSummary(@RequestBody TaskSummary summary) {
        boolean success = taskSummaryService.updateById(summary);
        if (success) {
            return ApiResponse.success("总结报告更新成功", "Summary updated successfully");
        } else {
            return ApiResponse.error("更新总结失败");
        }
    }

    // 删除总结
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteSummary(@PathVariable("id") Long id) {
        boolean success = taskSummaryService.removeById(id);
        if (success) {
            return ApiResponse.success("总结报告已删除", "Summary deleted successfully");
        } else {
            return ApiResponse.error("删除总结失败");
        }
    }
}
