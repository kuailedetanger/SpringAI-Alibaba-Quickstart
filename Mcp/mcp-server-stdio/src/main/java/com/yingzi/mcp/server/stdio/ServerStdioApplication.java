package com.yingzi.mcp.server.stdio;

import com.yingzi.mcp.server.stdio.config.ToolConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 *
 *  工作原理图解
 *
 * ┌────────────────────────────────────────────────────────────┐
 * │                ServerStdioApplication                      │
 * │                    (启动类)                                │
 * │                      │                                    │
 * │                      ▼                                    │
 * │           @SpringBootApplication                          │
 * │                      │                                    │
 * │                      ▼                                    │
 * │           ┌─────────────────┐                             │
 * │           │ 自动配置 Spring │                             │
 * │           └────────┬────────┘                             │
 * │                    │                                      │
 * │                    ▼                                      │
 * │           @Import(ToolConfiguration.class)                │
 * │                    │                                      │
 * │                    ▼                                      │
 * │           ┌─────────────────┐                             │
 * │           │ 注册工具服务    │  ← 时间、天气、翻译工具      │
 * │           └────────┬────────┘                             │
 * │                    │                                      │
 * │                    ▼                                      │
 * │           ┌─────────────────┐                             │
 * │           │ MCP 服务端启动  │  ← 等待客户端连接调用        │
 * │           └─────────────────┘                             │
 * └────────────────────────────────────────────────────────────┘
 *
 * @author yingzi
 * @date 2025/3/27:13:25
 */
@SpringBootApplication
@Import(ToolConfiguration.class)
public class ServerStdioApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerStdioApplication.class, args);
    }

}