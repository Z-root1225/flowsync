package hgc.flowsyncapi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hgc.flowsyncapi.common.ApiResponse;
import hgc.flowsyncapi.entity.ProjectInfo;
import hgc.flowsyncapi.service.ProjectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectInfoService projectInfoService;

    // 获取项目列表 (按 ID 倒序)
    @GetMapping
    public ApiResponse<List<ProjectInfo>> getProjects() {
        LambdaQueryWrapper<ProjectInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ProjectInfo::getId);
        List<ProjectInfo> list = projectInfoService.list(queryWrapper);
        return ApiResponse.success(list);
    }

    // 新建/编辑项目
    @PostMapping
    public ApiResponse<ProjectInfo> saveProject(
            @RequestParam("currentUserId") Long currentUserId,
            @RequestBody ProjectInfo project) {
        
        if (project.getId() == null) {
            // 新建项目
            project.setCreateTime(LocalDateTime.now());
            if (project.getOwnerId() == null) {
                // 系统自动将负责人设为当前操作用户
                project.setOwnerId(currentUserId);
            }
        }
        
        boolean success = projectInfoService.saveOrUpdate(project);
        if (success) {
            return ApiResponse.success("项目保存成功", project);
        } else {
            return ApiResponse.error("保存项目失败");
        }
    }

    // 删除项目
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteProject(@PathVariable("id") Long id) {
        boolean success = projectInfoService.removeById(id);
        if (success) {
            return ApiResponse.success("项目已删除", "Project deleted successfully");
        } else {
            return ApiResponse.error("删除项目失败");
        }
    }
}
