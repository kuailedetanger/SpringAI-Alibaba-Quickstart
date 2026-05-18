package com.yingzi.structedOutput.controller;

import com.yingzi.structedOutput.entity.BeanEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Bean控制器 - 演示如何将AI响应转换为Java对象
 * 核心功能：把AI返回的文本转换成我们定义的BeanEntity对象
 * @author yingzi
 * @date 2025/4/2:18:01
 */
@RestController
@RequestMapping("/bean")
public class BeanController {

    // 日志工具，用于打印调试信息
    private static final Logger log = LoggerFactory.getLogger(BeanController.class);

    // ========== 核心组件 ==========
    // 1. ChatClient：高级聊天客户端，简化调用AI模型
    private final ChatClient chatClient;
    // 2. ChatModel：底层AI模型接口，更灵活的调用方式
    private final ChatModel chatModel;
    // 3. BeanOutputConverter：【关键】转换器，把JSON文本转成Java对象
    private final BeanOutputConverter<BeanEntity> converter;
    // 4. format：告诉AI需要返回的JSON格式模板
    private final String format;

    /**
     * 构造方法 - 依赖注入初始化
     * Spring会自动注入ChatClient.Builder和ChatModel
     */
    public BeanController(ChatClient.Builder builder, ChatModel chatModel) {
        this.chatModel = chatModel;

        // ========== 关键步骤1：创建转换器 ==========
        // 创建一个专门把JSON转成BeanEntity对象的转换器
        // ParameterizedTypeReference用于告诉转换器要转成什么类型
        this.converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<BeanEntity>() {
                }
        );
        
        // ========== 关键步骤2：获取格式模板 ==========
        // 从转换器获取JSON格式要求字符串
        // 这个字符串会告诉AI："请按这个格式返回结果"
        // 示例格式：{"title":"...","author":"...","date":"...","content":"..."}
        this.format = converter.getFormat();
        log.info("【格式模板】AI需要返回的JSON格式: {}", format);
        
        // 创建ChatClient实例
        this.chatClient = builder.build();
    }

    /**
     * 方式一：普通对话（不带格式要求）
     * 演示：直接调用AI，不指定格式，AI返回普通文本
     * 
     * @param query 用户提问，默认值是写一首诗
     * @return AI返回的原始文本
     */
    @GetMapping("/chat")
    public String simpleChat(
            @RequestParam(value = "query", 
            defaultValue = "以影子为作者，写一篇200字左右的有关人工智能诗篇") String query) {
        
        // 直接调用AI，不设置格式要求
        String result = chatClient.prompt(query)
                .call()      // 发送请求
                .content();  // 获取返回内容

        log.info("【AI原始响应】: {}", result);
        
        // 尝试把结果转成Bean对象（通常会失败，因为没有指定格式）
        assert result != null;
        try {
            BeanEntity convert = converter.convert(result);
            log.info("✅ 反序列化成功！转换后的对象: {}", convert);
        } catch (Exception e) {
            log.error("❌ 反序列化失败（正常现象，因为没有指定JSON格式）");
        }
        
        return result;
    }

    /**
     * 方式二：带格式要求的对话（推荐）
     * 演示：告诉AI必须返回JSON格式，然后成功转换成Java对象
     * 
     * @param query 用户提问
     * @return AI返回的JSON格式文本
     */
    @GetMapping("/chat-format")
    public String simpleChatFormat(
            @RequestParam(value = "query", 
            defaultValue = "以影子为作者，写一篇200字左右的有关人工智能诗篇") String query) {
        
        // ========== 关键：格式提示词模板 ==========
        // 告诉AI：
        // 1. format: 必须以纯JSON格式输出，不要markdown
        // 2. outputExample: 给出具体的格式示例（使用{format}占位符）
        String promptUserSpec = """
                format: 以纯文本输出 json，请不要包含任何多余的文字——包括 markdown 格式;
                outputExample: {format};
                """;

        // 调用AI并设置格式要求
        String result = chatClient.prompt(query)
                .user(u -> u.text(promptUserSpec)  // 添加格式提示
                        .param("format", format)) // 把格式模板填充进去
                .call().content();

        log.info("【AI格式化响应】: {}", result);
        
        // ========== 关键：转换为Java对象 ==========
        assert result != null;
        try {
            // 使用转换器把JSON文本转成BeanEntity对象
            BeanEntity convert = converter.convert(result);
            log.info("✅ 反序列化成功！\n  ├─标题: {}\n  ├─作者: {}\n  ├─日期: {}\n  └─内容: {}", 
                    convert.getTitle(), convert.getAuthor(), 
                    convert.getDate(), convert.getContent().substring(0, Math.min(30, convert.getContent().length())) + "...");
        } catch (Exception e) {
            log.error("❌ 反序列化失败: {}", e.getMessage());
        }
        
        return result;
    }

    /**
     * 方式三：使用底层ChatModel（高级用法）
     * 演示：直接使用ChatModel接口，更灵活地构建Prompt
     * 
     * @param query 用户提问
     * @return AI返回的JSON格式文本
     */
    @GetMapping("/chat-model-format")
    public String chatModel(
            @RequestParam(value = "query", 
            defaultValue = "以影子为作者，写一篇200字左右的有关人工智能诗篇") String query) {
        
        // ========== 使用PromptTemplate构建提示词 ==========
        // 把用户问题和格式要求拼接成完整的提示词
        String template = query + "\n{format}";
        Prompt prompt = new PromptTemplate(template, Map.of("format", format)).create();

        // 直接调用底层ChatModel（比ChatClient更灵活）
        String result = chatModel.call(prompt)      // 发送Prompt
                                .getResult()       // 获取结果
                                .getOutput()       // 获取输出
                                .getText();        // 获取文本内容

        log.info("【ChatModel响应】: {}", result);
        
        // 尝试转换为Bean对象
        assert result != null;
        try {
            BeanEntity convert = converter.convert(result);
            log.info("✅ 反序列化成功！转换后的对象: {}", convert);
        } catch (Exception e) {
            log.error("❌ 反序列化失败: {}", e.getMessage());
        }
        
        return result;
    }
}