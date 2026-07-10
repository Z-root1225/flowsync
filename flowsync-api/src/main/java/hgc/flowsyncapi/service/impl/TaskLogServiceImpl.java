package hgc.flowsyncapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.entity.TaskLog;
import hgc.flowsyncapi.mapper.TaskLogMapper;
import hgc.flowsyncapi.mapper.TaskInfoMapper;
import hgc.flowsyncapi.service.TaskLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import hgc.flowsyncapi.service.ProjectInfoService;
import org.springframework.context.annotation.Lazy;

@Service
@SuppressWarnings("all")
public class TaskLogServiceImpl extends ServiceImpl<TaskLogMapper, TaskLog> implements TaskLogService {

    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Autowired
    @Lazy
    private ProjectInfoService projectInfoService;

    @Override
    public List<TaskLog> getTaskLogs(Long taskId) {
        LambdaQueryWrapper<TaskLog> queryWrapper = new LambdaQueryWrapper<>();
        if (taskId != null) {
            queryWrapper.eq(TaskLog::getTaskId, taskId);
        }
        queryWrapper.orderByDesc(TaskLog::getId); // 按 id 倒序展示
        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(TaskLog entity) {
        // 保存进度日志
        boolean success = super.save(entity);
        if (success && entity.getTaskId() != null) {
            // 同步更新关联任务的状态
            TaskInfo task = taskInfoMapper.selectById(entity.getTaskId());
            if (task != null) {
                // 如果进度达到 100%，自动设置任务状态为“已完成”
                if (entity.getProgressPercent() >= 100) {
                    task.setStatus("已完成");
                } else if (entity.getProgressPercent() > 0) {
                    task.setStatus("进行中");
                }
                taskInfoMapper.updateById(task);

                // 同步更新关联项目的状态
                if (task.getProjectId() != null) {
                    projectInfoService.updateProjectStatus(task.getProjectId());
                }
            }
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(TaskLog entity) {
        boolean success = super.updateById(entity);
        if (success && entity.getTaskId() != null) {
            recalculateTaskStatus(entity.getTaskId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(java.io.Serializable id) {
        TaskLog log = this.getById(id);
        boolean success = super.removeById(id);
        if (success && log != null && log.getTaskId() != null) {
            recalculateTaskStatus(log.getTaskId());
        }
        return success;
    }

    private void recalculateTaskStatus(Long taskId) {
        if (taskId == null) return;
        TaskInfo task = taskInfoMapper.selectById(taskId);
        if (task == null) return;

        // 查找该任务最新的一条进度反馈记录
        LambdaQueryWrapper<TaskLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskLog::getTaskId, taskId).orderByDesc(TaskLog::getId).last("limit 1");
        TaskLog latestLog = this.getOne(queryWrapper);

        if (latestLog != null) {
            if (latestLog.getProgressPercent() >= 100) {
                task.setStatus("已完成");
            } else if (latestLog.getProgressPercent() > 0) {
                task.setStatus("进行中");
            } else {
                task.setStatus("未开始");
            }
        } else {
            // 如果进度记录全部删除了，将任务状态重设为“未开始”
            task.setStatus("未开始");
        }
        taskInfoMapper.updateById(task);

        // 同步更新父项目状态
        if (task.getProjectId() != null) {
            projectInfoService.updateProjectStatus(task.getProjectId());
        }
    }
}

