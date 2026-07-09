package hgc.flowsyncapi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hgc.flowsyncapi.entity.ProjectInfo;
import hgc.flowsyncapi.mapper.ProjectInfoMapper;
import hgc.flowsyncapi.service.ProjectInfoService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.mapper.TaskInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProjectInfoServiceImpl extends ServiceImpl<ProjectInfoMapper, ProjectInfo> implements ProjectInfoService {

    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProjectStatus(Long projectId) {
        if (projectId == null) {
            return;
        }

        ProjectInfo project = this.getById(projectId);
        if (project == null) {
            return;
        }

        // 查询该项目下所有的任务
        LambdaQueryWrapper<TaskInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskInfo::getProjectId, projectId);
        List<TaskInfo> tasks = taskInfoMapper.selectList(queryWrapper);

        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        boolean hasInProgress = false;
        boolean hasNotStarted = false;
        boolean hasCompleted = false;

        for (TaskInfo task : tasks) {
            String status = task.getStatus();
            if ("进行中".equals(status)) {
                hasInProgress = true;
            } else if ("已完成".equals(status)) {
                hasCompleted = true;
            } else {
                hasNotStarted = true;
            }
        }

        String newStatus;
        if (hasCompleted && !hasInProgress && !hasNotStarted) {
            // 所有任务都已完成 -> 项目已完成
            newStatus = "已完成";
        } else if (hasInProgress || hasCompleted) {
            // 只要有任何任务在进行中，或者有部分任务已完成 -> 项目进行中
            newStatus = "进行中";
        } else {
            // 所有任务都未开始 -> 项目未开始
            newStatus = "未开始";
        }

        if (!newStatus.equals(project.getStatus())) {
            project.setStatus(newStatus);
            this.updateById(project);
        }
    }
}
