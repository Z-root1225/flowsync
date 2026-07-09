package hgc.flowsyncapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_summary")
public class TaskSummary {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @JsonProperty("project_id")
    private Long projectId;
    
    @JsonProperty("task_id")
    private Long taskId;
    
    @JsonProperty("summary_type")
    private String summaryType;
    
    private String content;
    
    @JsonProperty("created_by")
    private Long createdBy;
    
    @JsonProperty("create_time")
    private LocalDateTime createTime;
}
