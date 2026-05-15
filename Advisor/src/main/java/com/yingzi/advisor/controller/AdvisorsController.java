package com.yingzi.advisor.controller;

import com.yingzi.advisor.component.ReasoningContentAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @author yingzi
 * @date 2025/3/21:17:31
 */
@RestController
@RequestMapping("/advisors")
public class AdvisorsController {


    /**
     * `chatClient`：与 AI 对话的工具
     */
    private final ChatClient chatClient;
    /**
     * `chatClientWithLoggingAdvisor`：带有日志监控功能的客户端
     */
    private final ChatClient chatClientWithLoggingAdvisor;
    /**
     * `chatMemory`：AI 的"大脑记忆"，存储在内存中
     */
    private final InMemoryChatMemory chatMemory = new InMemoryChatMemory();

    /**
     * `CHAT_MEMORY_RETRIEVE_SIZE`：记住最近 100 条对话
     */
    private final int CHAT_MEMORY_RETRIEVE_SIZE = 100;


    /**
     * - 给 AI 配备了一个"记忆助手"
     * - 注释掉的 `ReasoningContentAdvisor` 是自定义的思考过程处理器
     * @param builder
     */
    public AdvisorsController(ChatClient.Builder builder) {

        this.chatClient = builder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory)
                        // 整合 QWQ 的思考过程到输出中
//                        ,new ReasoningContentAdvisor(0)
                )
                .build();
                
        // 创建带有自定义Advisor的客户端（演示日志监控功能）
        this.chatClientWithLoggingAdvisor = builder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new ReasoningContentAdvisor(0) // 使用ReasoningContentAdvisor展示Advisor功能
                )
                .build();
    }


    /**
     * @param `chatId`：对话的唯一标识，就像"聊天室号"
     * @param `prompt`：用户的问题
     * @param `Flux<String>`：流式响应，实时显示 AI 的回答
     * - 通过 `advisors` 传入对话 ID 和记忆大小
     *
     * -----------扩展知识----------------
     * 关于 chatId 的关键解答：
     * 1. 会话机制：
     *
     * 只要给定一个 chatId，就会创建一个新的会话或加入已存在的会话
     * 就像你在豆包、ChatGPT 等平台新建一个对话窗口
     * 在同一个 chatId 下的所有对话都会被保存和关联
     * AI 可以看到该会话中的所有历史消息，并基于上下文进行回复
     * 2. 记忆大小限制：
     *
     * 通过 CHAT_MEMORY_RETRIEVE_SIZE 参数控制（当前设为 100）
     * 这意味着每次请求时，最多只加载该会话的最近 100 条消息作为上下文
     * 不是无限制地保存整个对话历史
     * 这是为了防止上下文过长导致性能问题或超出模型的最大上下文长度
     * 3. 是否可以一直问下去：
     *
     * 理论上可以，但受限于内存和模型上下文长度
     * 实际应用中会有大小限制，以保证性能和成本控制
     *
     *
     * -----------扩展知识----------------
     * @return
     */
    @GetMapping("/chatWithChatMemory")
    public Flux<String> chatWithChatMemory(String chatId, String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE))
                .stream().content();
    }

    /**
     * - 查看某个对话的历史记录
     * @param `chatId`：对话 ID
     * @param `size`：获取多少条历史消息（默认 10 条）
     * @return
     */
    @GetMapping("/chatWithLoggingAdvisor")
    public Flux<String> chatWithLoggingAdvisor(String chatId, String prompt) {
        return chatClientWithLoggingAdvisor.prompt()
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE))
                .stream().content();
    }
    
    /**
     * 演示不使用任何Advisor的纯AI响应
     * 用于对比Advisor的作用
     */
    @GetMapping("/chatWithoutAdvisors")
    public Flux<String> chatWithoutAdvisors(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
    
    /**
     * 演示动态添加Advisor
     * 展示Advisor的灵活性
     */
    @GetMapping("/chatWithDynamicAdvisor")
    public Flux<String> chatWithDynamicAdvisor(String chatId, String prompt, boolean enableMemory) {
        var clientBuilder = chatClient.prompt()
                .user(prompt);
        
        if (enableMemory) {
            clientBuilder = clientBuilder.advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                    .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE));
        } else {
            clientBuilder = clientBuilder.advisors(java.util.Collections.emptyList()); // 不添加任何Advisor参数，实现无记忆对话
        }
        
        return clientBuilder.stream().content();
    }

    @GetMapping("messages")
    public List<Message> getMessages(@RequestParam(value = "chatId") String chatId, @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        if (Objects.isNull(chatId)) {
            throw new RuntimeException("chatId is null");
        }
        return chatMemory.get(chatId,size);

    }

    /**
     * 演示Advisor的实际效果对比
     * 通过不同的endpoint可以直观感受Advisor的价值
     */
    @GetMapping("/demo-advisor-benefits")
    public String demoAdvisorBenefits() {
        return "Advisor的好处:\n" +
               "1. 聊天记忆 - AI能记住对话历史 (/chatWithChatMemory)\n" +
               "2. 内容过滤 - 可以过滤敏感内容\n" +
               "3. 结构化输出 - 可以标准化AI输出格式\n" +
               "4. 个性化 - 可以为不同用户提供定制化服务\n" +
               "5. 上下文增强 - 可以添加额外的上下文信息\n" +
               "6. 安全控制 - 可以实施安全策略\n" +
               "7. 日志监控 - 可以记录所有交互 (/chatWithLoggingAdvisor)\n" +
               "8. 性能监控 - 可以追踪响应时间和性能\n" +
               "9. 推理可见 - 可以展示AI的思考过程 (/chatWithCustomAdvisor)\n" +
               "\n尝试这些端点来体验不同效果:\n" +
               "- /advisors/chatWithChatMemory - 带记忆的对话\n" +
               "- /advisors/chatWithoutAdvisors - 无Advisor的纯AI响应\n" +
               "- /advisors/chatWithCustomAdvisor - 带自定义Advisor的对话\n" +
               "- /advisors/chatWithLoggingAdvisor - 带日志监控的对话\n" +
               "- /advisors/demo-advisor-benefits - 查看Advisor功能说明";
    }
    
    /**
     * 展示Advisor的链式处理能力
     * 演示多个Advisor可以协同工作的场景
     */
    @GetMapping("/multi-advisor-demo")
    public String multiAdvisorDemo() {
        return "Advisor的链式处理:\n" +
               "1. Request进入\n" +
               "2. -> MemoryAdvisor: 添加历史对话上下文\n" +
               "3. -> LoggingAdvisor: 记录请求日志\n" +
               "4. -> SecurityAdvisor: 检查安全内容\n" +
               "5. -> AI模型: 生成响应\n" +
               "6. -> ReasoningAdvisor: 增强响应内容\n" +
               "7. -> LoggingAdvisor: 记录响应日志\n" +
               "8. -> Response返回\n\n" +
               "每个Advisor专注于单一职责，可以灵活组合使用";
    }
}