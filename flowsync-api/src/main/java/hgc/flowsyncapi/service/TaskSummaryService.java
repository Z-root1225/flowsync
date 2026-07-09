package hgc.flowsyncapi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hgc.flowsyncapi.entity.TaskSummary;
import java.util.List;

public interface TaskSummaryService extends IService<TaskSummary> {
    List<TaskSummary> getSummaries();
}
