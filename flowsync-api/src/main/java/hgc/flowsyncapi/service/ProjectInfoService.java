package hgc.flowsyncapi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import hgc.flowsyncapi.entity.ProjectInfo;

public interface ProjectInfoService extends IService<ProjectInfo> {
    /**
     * 根据项目下属任务的状态，自动更新项目的整体状态
     * @param projectId 项目 ID
     */
    void updateProjectStatus(Long projectId);
}
