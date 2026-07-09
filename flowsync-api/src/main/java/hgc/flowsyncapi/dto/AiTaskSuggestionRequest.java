package hgc.flowsyncapi.dto;

import lombok.Data;

@Data
public class AiTaskSuggestionRequest {
    private String projectName;
    private String title;
    private String description;
}
