package hgc.flowsyncapi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hgc.flowsyncapi.entity.TaskSummary;
import hgc.flowsyncapi.mapper.TaskSummaryMapper;
import hgc.flowsyncapi.service.TaskSummaryService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TaskSummaryServiceImpl extends ServiceImpl<TaskSummaryMapper, TaskSummary> implements TaskSummaryService {

    @Override
    public List<TaskSummary> getSummaries() {
        LambdaQueryWrapper<TaskSummary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(TaskSummary::getId); // 按 id 倒序展示全部总结
        return this.list(queryWrapper);
    }
}
