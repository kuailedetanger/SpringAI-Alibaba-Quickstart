package com.yingzi.chatModel.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author yingzi
 * @date 2025/3/21:16:22
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ChatClient chatClient;
    private final Environment environment;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    public ChatController(ChatClient.Builder builder, Environment environment) {
        this.chatClient = builder.build();
        this.environment = environment;
    }

    /**
     * qwq模型目前只支持 Stream 调用
     * ChatClient 流式调用
     */

    @GetMapping("/stream")
    public Flux<String> streamChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？")String query, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        
        long startTime = System.currentTimeMillis();
        log.info("========================================");
        log.info("请求开始 - Query: {}", query);
//        log.info("DASHSCOPE_API_KEY 配置值: {}", apiKey);
//        log.info("从 Environment 获取: {}", environment.getProperty("spring.ai.dashscope.api-key"));
        log.info("API Key 是否为空: {}", apiKey == null || apiKey.isEmpty());
        log.info("========================================");
        
        StringBuilder fullResponse = new StringBuilder();
        
        return chatClient.prompt(query)
                .stream()
                .content()
                .doOnNext(chunk -> {
                    fullResponse.append(chunk);
                    log.debug("收到响应片段: {}", chunk);
                })
                .doOnComplete(() -> {
                    long endTime = System.currentTimeMillis();
                    log.info("========================================");
                    log.info("请求完成 - 总耗时: {}ms", endTime - startTime);
                    log.info("完整响应内容: {}", fullResponse.toString());
                    log.info("响应长度: {} 字符", fullResponse.length());
                    log.info("========================================");
                })
                .doOnError(error -> {
                    long endTime = System.currentTimeMillis();
                    log.error("请求失败 - 耗时: {}ms, 错误: {}", endTime - startTime, error.getMessage(), error);
                });
    }
}