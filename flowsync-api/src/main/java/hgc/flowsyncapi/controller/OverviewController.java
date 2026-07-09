package hgc.flowsyncapi.controller;

import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.service.OverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/overview")
public class OverviewController {

    @Autowired
    private OverviewService overviewService;

    // 获取工作台统计信息 (成员数、项目数、任务数、总结数)
    @GetMapping
    public ApiResponse<Map<String, Object>> getOverview() {
        Map<String, Object> stats = overviewService.getOverviewStats();
        return ApiResponse.success(stats);
    }
}
