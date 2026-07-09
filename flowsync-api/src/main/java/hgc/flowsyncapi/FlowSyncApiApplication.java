package hgc.flowsyncapi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("hgc.flowsyncapi.mapper")
public class FlowSyncApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlowSyncApiApplication.class, args);
    }
}
