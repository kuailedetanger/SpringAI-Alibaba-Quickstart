package com.yingzi.structedOutput.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * JSON控制器 - 演示阿里云模型内置的JSON输出格式
 * 核心功能：使用模型内置的JSON模式，让AI直接返回JSON格式
 * 
 * 【小白理解】：就像告诉AI"请用中文回答"一样，这里是告诉AI"请用JSON格式回答"
 * 
 * @author yingzi
 * @date 2025/4/2:17:46
 */
@RestController
@RequestMapping("/json")
public class JsonController {

    // ========== 核心组件 ==========
    // 1. ChatClient：高级聊天客户端
    private final ChatClient chatClient;
    
    // 2. responseFormat：【关键】阿里云模型的内置JSON格式配置
    //    这个配置告诉AI："请直接返回JSON对象"
    private final DashScopeResponseFormat responseFormat;

    /**
     * 构造方法 - 初始化JSON格式配置
     */
    public JsonController(ChatClient.Builder builder) {
        // ========== 关键步骤：创建JSON格式配置 ==========
        // 1. 创建响应格式对象
        DashScopeResponseFormat responseFormat = new DashScopeResponseFormat();
        
        // 2. 设置格式类型为JSON_OBJECT
        //    可选类型：
        //    - JSON_OBJECT：返回JSON对象 {"key": "value"}
        //    - TEXT：返回普通文本（默认）
        responseFormat.setType(DashScopeResponseFormat.Type.JSON_OBJECT);

        // 保存配置供后续使用
        this.responseFormat = responseFormat;
        
        // 创建ChatClient实例
        this.chatClient = builder.build();
    }

    /**
     * 方式一：普通对话（不带内置格式）
     * 演示：只在提问中要求JSON，但不强制
     * 
     * 【特点】：
     * - 只在query中说"请以JSON格式介绍你自己"
     * - AI可能听话返回JSON，也可能不返回
     * - 完全依赖AI理解自然语言指令
     * 
     * @param query 用户提问
     * @return AI返回的内容（可能是JSON，也可能不是）
     */
    @GetMapping("/chat")
    public String simpleChat(
            @RequestParam(value = "query", 
            defaultValue = "请以JSON格式介绍你自己") String query) {
        
        // 直接调用，不设置任何格式选项
        return chatClient.prompt(query).call().content();
    }

    /**
     * 方式二：使用内置JSON格式（推荐）
     * 演示：强制要求AI返回JSON格式
     * 
     * 【特点】：
     * - 通过API参数强制要求JSON格式
     * - AI一定会返回有效的JSON（如果生成失败会报错）
     * - 比自然语言指令更可靠
     * 
     * @param query 用户提问
     * @return AI返回的标准JSON字符串
     */
    @GetMapping("/chat-format")
    public String simpleChatFormat(
            @RequestParam(value = "query", 
            defaultValue = "请以JSON格式介绍你自己") String query) {
        
        return chatClient.prompt(query)
                // ========== 关键：设置请求选项 ==========
                .options(
                        DashScopeChatOptions.builder()
                                // withTopP：控制输出多样性，0.7表示中等多样性
                                .withTopP(0.7)
                                // withResponseFormat：【核心】强制设置JSON格式
                                .withResponseFormat(responseFormat)
                                .build()
                )
                .call().content();
    }
}