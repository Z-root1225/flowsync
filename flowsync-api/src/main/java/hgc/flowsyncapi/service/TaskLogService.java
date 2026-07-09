package hgc.flowsyncapi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hgc.flowsyncapi.entity.TaskLog;
import java.util.List;

public interface TaskLogService extends IService<TaskLog> {
    List<TaskLog> getTaskLogs(Long taskId);
}
