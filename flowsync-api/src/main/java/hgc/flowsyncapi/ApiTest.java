package hgc.flowsyncapi;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.*;

public class ApiTest {
    public static void main(String[] args) {
        // 使用系统环境变量 DASHSCOPE_API_KEY，避免将真实密钥硬编码并误上传到 Git
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            // 如果环境变量没配，你也可以在这临时填入进行测试，但切记测试完后不要提交此文件
            apiKey = "YOUR_DASHSCOPE_API_KEY_HERE";
        }
        String apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
        
        System.out.println("=== 开始独立测试通义千问 API ===");
        System.out.println("测试地址: " + apiUrl);
        System.out.println("使用的 API Key: " + apiKey.substring(0, 15) + "...");
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen-plus");

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", "你好，请回答两个字：成功。");
            messages.add(userMsg);
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            System.out.println("正在向阿里云发送请求...");
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            
            System.out.println("\n=== 测试成功！ ===");
            System.out.println("接口返回状态码: " + response.getStatusCode());
            System.out.println("返回内容: " + response.getBody());
        } catch (Exception e) {
            System.err.println("\n=== 测试失败！ ===");
            System.err.println("错误类型: " + e.getClass().getName());
            System.err.println("错误原因: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
