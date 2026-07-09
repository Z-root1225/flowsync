package hgc.flowsyncapi.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hgc.flowsyncapi.dto.AiTaskPlanItem;
import hgc.flowsyncapi.dto.AiTaskPlanResponse;
import hgc.flowsyncapi.entity.User;
import hgc.flowsyncapi.service.QwenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class QwenServiceImpl implements QwenService {

    @Value("${dashscope.api-key:your-api-key-here}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    @Override
    public String getTaskSuggestion(String projectName, String title, String description) {
        if (apiKey == null || !apiKey.trim().startsWith("sk-")) {
            return getFallbackSuggestion(title);
        }

        try {
            String systemPrompt = "你是一个简单直接的项目任务助手。请用最容易理解的中文输出，给出：1. 建议拆分的子任务；2. 执行顺序；3. 风险提醒。控制在300字以内。";
            String userPrompt = String.format("项目名称：%s\n任务标题：%s\n任务说明：%s", projectName, title, description);

            String responseBody = callQwen(systemPrompt, userPrompt, false);
            return parseContentFromResponse(responseBody);
        } catch (Exception e) {
            System.err.println("调用千问获取建议出错: " + e.getMessage());
            return getFallbackSuggestion(title);
        }
    }

    @Override
    public AiTaskPlanResponse getTaskPlan(Long projectId, String projectName, String goal, String description, List<User> members) {
        if (apiKey == null || !apiKey.trim().startsWith("sk-") || members == null || members.isEmpty()) {
            return buildFallbackPlan(projectName, goal, members);
        }

        try {
            String systemPrompt = "你是一个项目任务拆解助手。请把大任务拆成可以直接执行的小任务。我会给你可选的成员名单，请为每个任务推荐一个最合适的负责人，在 assignee_id 字段填写该成员的 id（必须是名单中已有的数字 id）。" +
                    "只返回严格 JSON 格式，不要解释，不要 markdown 标记。你必须返回如下 JSON 对象结构，不能返回数组包围：\n" +
                    "{\n" +
                    "  \"summary\": \"本次任务拆解的概述文案，100字以内\",\n" +
                    "  \"items\": [\n" +
                    "    {\n" +
                    "      \"title\": \"任务名称\",\n" +
                    "      \"description\": \"任务详细描述\",\n" +
                    "      \"priority\": \"优先级(低/中/高)\",\n" +
                    "      \"suggested_days\": 预计花费天数(整数，如 3),\n" +
                    "      \"assignee_id\": 负责人ID(必须是所给成员中的数字ID)\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            
            StringBuilder memberListStr = new StringBuilder();
            for (User u : members) {
                memberListStr.append(u.getId()).append(" - ").append(u.getRealName()).append("\n");
            }

            String userPrompt = String.format(
                    "项目名称：%s\n任务目标：%s\n补充说明：%s\n可选成员名单（id - 姓名）：\n%s",
                    projectName, goal, description != null ? description : "无", memberListStr.toString()
            );

            String responseBody = callQwen(systemPrompt, userPrompt, true);
            String jsonContent = parseContentFromResponse(responseBody);
            
            // 去除 markdown 标记（大模型有时仍会包含 ```json ... ``` 标记）
            if (jsonContent.contains("```")) {
                jsonContent = jsonContent.replaceAll("```json|```", "").trim();
            }

            AiTaskPlanResponse planResponse = objectMapper.readValue(jsonContent, AiTaskPlanResponse.class);
            
            // 负责人 ID 有效性校验与兜底（第一人）
            if (planResponse.getItems() != null && !planResponse.getItems().isEmpty()) {
                Long defaultAssigneeId = members.get(0).getId();
                Set<Long> validUserIds = new HashSet<>();
                for (User u : members) {
                    validUserIds.add(u.getId());
                }

                for (AiTaskPlanItem item : planResponse.getItems()) {
                    if (item.getAssigneeId() == null || !validUserIds.contains(item.getAssigneeId())) {
                        item.setAssigneeId(defaultAssigneeId); // 兜底替换为名单第一个人
                    }
                    if (item.getPriority() == null || item.getPriority().trim().isEmpty()) {
                        item.setPriority("中");
                    }
                    if (item.getSuggestedDays() == null) {
                        item.setSuggestedDays(3);
                    }
                }
            }
            return planResponse;

        } catch (Exception e) {
            System.err.println("调用千问拆解任务出错: " + e.getMessage() + "。将采用 fallback 方案");
            return buildFallbackPlan(projectName, goal, members);
        }
    }

    // 调用千问 API
    private String callQwen(String systemPrompt, String userPrompt, boolean requireJson) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "qwen-plus");

        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);

        requestBody.put("messages", messages);

        if (requireJson) {
            Map<String, String> responseFormat = new HashMap<>();
            responseFormat.put("type", "json_object");
            requestBody.put("response_format", responseFormat);
        }

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);
        
        return response.getBody();
    }

    // 从 Chat Completion 结果中提取文本内容
    private String parseContentFromResponse(String responseJson) throws Exception {
        JsonNode rootNode = objectMapper.readTree(responseJson);
        JsonNode choicesNode = rootNode.path("choices");
        if (choicesNode.isArray() && choicesNode.size() > 0) {
            JsonNode messageNode = choicesNode.get(0).path("message");
            return messageNode.path("content").asText();
        }
        throw new RuntimeException("未能从大模型返回中解析出 choices 数据: " + responseJson);
    }

    // 建议兜底方案
    private String getFallbackSuggestion(String title) {
        return String.format(
                "💡针对任务「%s」，系统给出的执行建议（Mock）：\n" +
                "1. 建议拆分：\n" +
                "   - 准备与环境部署：搭建底层脚手架，设计核心基础类。\n" +
                "   - 功能编码与对接：编写 Service 层逻辑与 API 控制器，打通数据库交互通路。\n" +
                "   - 交付物输出：测试接口可靠性，撰写项目进度总结。\n" +
                "2. 执行顺序：环境搭建 -> 功能编写 -> 联调交付。\n" +
                "3. 风险提醒：请关注前后端请求参数的拼写，确保 API 返回格式与前端拦截器保持兼容。",
                title
        );
    }

    // 任务规划兜底方案
    private AiTaskPlanResponse buildFallbackPlan(String projectName, String goal, List<User> members) {
        AiTaskPlanResponse response = new AiTaskPlanResponse();
        response.setSummary(String.format("后端 API 未配置有效的百炼 API Key 或连接超时。系统已自动为项目「%s」的目标「%s」生成备选任务草稿（请在页面手动选择负责人）。", projectName, goal));
        
        List<AiTaskPlanItem> items = new ArrayList<>();
        
        AiTaskPlanItem item1 = new AiTaskPlanItem();
        item1.setTitle("准备项目环境及基础资料");
        item1.setDescription("熟悉小组成员的架构分工，搭建项目骨架并初始化建表数据。");
        item1.setPriority("中");
        item1.setSuggestedDays(1);
        item1.setAssigneeId(members != null && !members.isEmpty() ? members.get(0).getId() : null);
        items.add(item1);

        AiTaskPlanItem item2 = new AiTaskPlanItem();
        item2.setTitle("主体功能模块编码开发");
        item2.setDescription("进行核心业务数据结构的编码，对外开放控制层 REST 接口。");
        item2.setPriority("高");
        item2.setSuggestedDays(4);
        item2.setAssigneeId(members != null && !members.isEmpty() ? members.get(0).getId() : null);
        items.add(item2);

        AiTaskPlanItem item3 = new AiTaskPlanItem();
        item3.setTitle("功能验证调试与工作总结");
        item3.setDescription("前后端跨域代理调试，测试各模块完整性，撰写总结归档文档。");
        item3.setPriority("低");
        item3.setSuggestedDays(1);
        item3.setAssigneeId(members != null && !members.isEmpty() ? members.get(0).getId() : null);
        items.add(item3);

        response.setItems(items);
        return response;
    }
}
