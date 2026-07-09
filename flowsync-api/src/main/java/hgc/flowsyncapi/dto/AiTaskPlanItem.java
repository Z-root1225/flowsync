package hgc.flowsyncapi.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AiTaskPlanItem {
    private String title;
    private String description;
    private String priority;
    
    @JsonProperty("suggested_days")
    @JsonAlias({"suggestedDays", "suggested_days"})
    private Integer suggestedDays;
    
    @JsonProperty("assignee_id")
    @JsonAlias({"assigneeId", "assignee_id"})
    private Long assigneeId;
}
