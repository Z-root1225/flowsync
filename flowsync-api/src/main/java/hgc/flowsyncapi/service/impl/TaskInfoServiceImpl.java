package hgc.flowsyncapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hgc.flowsyncapi.entity.TaskInfo;
import hgc.flowsyncapi.mapper.TaskInfoMapper;
import hgc.flowsyncapi.service.TaskInfoService;
import org.springframework.stereotype.Service;
import hgc.flowsyncapi.service.ProjectInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import java.io.Serializable;
import java.util.List;

@Service
public class TaskInfoServiceImpl extends ServiceImpl<TaskInfoMapper, TaskInfo> implements TaskInfoService {

    @Autowired
    @Lazy
    private ProjectInfoService projectInfoService;

    @Override
    public List<TaskInfo> getTasks(Long projectId) {
        LambdaQueryWrapper<TaskInfo> queryWrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            queryWrapper.eq(TaskInfo::getProjectId, projectId);
        }
        queryWrapper.orderByDesc(TaskInfo::getId); // 按 id 倒序展示
        return this.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(TaskInfo entity) {
        boolean success = super.save(entity);
        if (success && entity.getProjectId() != null) {
            projectInfoService.updateProjectStatus(entity.getProjectId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(TaskInfo entity) {
        boolean success = super.updateById(entity);
        if (success && entity.getProjectId() != null) {
            projectInfoService.updateProjectStatus(entity.getProjectId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdate(TaskInfo entity) {
        boolean success = super.saveOrUpdate(entity);
        if (success && entity.getProjectId() != null) {
            projectInfoService.updateProjectStatus(entity.getProjectId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Serializable id) {
        TaskInfo task = this.getById(id);
        boolean success = super.removeById(id);
        if (success && task != null && task.getProjectId() != null) {
            projectInfoService.updateProjectStatus(task.getProjectId());
        }
        return success;
    }
}
