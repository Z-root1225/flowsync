package hgc.flowsyncapi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_log")
public class TaskLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @JsonProperty("task_id")
    private Long taskId;
    
    @JsonProperty("progress_percent")
    private Integer progressPercent;
    
    private String content;
    
    @JsonProperty("operator_id")
    private Long operatorId;
    
    @JsonProperty("create_time")
    private LocalDateTime createTime;
}
