package hgc.flowsyncapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task_info")
public class TaskInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @JsonProperty("project_id")
    private Long projectId;
    
    @JsonProperty("parent_id")
    private Long parentId;
    
    private String title;
    private String description;
    
    @JsonProperty("assignee_id")
    private Long assigneeId;
    
    @JsonProperty("creator_id")
    private Long creatorId;
    
    private String status;
    private String priority;
    
    @JsonProperty("due_date")
    private LocalDate dueDate;
    
    @JsonProperty("ai_suggestion")
    private String aiSuggestion;
    
    @JsonProperty("create_time")
    private LocalDateTime createTime;
}
