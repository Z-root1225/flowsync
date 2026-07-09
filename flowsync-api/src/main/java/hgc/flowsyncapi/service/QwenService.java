package hgc.flowsyncapi.service;

import hgc.flowsyncapi.dto.AiTaskPlanResponse;
import hgc.flowsyncapi.entity.User;
import java.util.List;

public interface QwenService {
    String getTaskSuggestion(String projectName, String title, String description);
    
    AiTaskPlanResponse getTaskPlan(Long projectId, String projectName, String goal, String description, List<User> members);
}
