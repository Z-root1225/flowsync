package hgc.flowsyncapi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hgc.flowsyncapi.entity.TaskInfo;
import java.util.List;

public interface TaskInfoService extends IService<TaskInfo> {
    List<TaskInfo> getTasks(Long projectId);
}
