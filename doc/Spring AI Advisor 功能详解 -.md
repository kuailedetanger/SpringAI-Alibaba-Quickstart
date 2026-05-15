# Spring AI Advisor 功能详解 - 通俗易懂版

## 📚 项目概述

这个项目演示了 Spring AI 的 **Advisor（顾问）** 功能，就像给 AI 模型配备了一个"智能助手"，在 AI 回答问题前后进行一些处理。

### 🎯 核心功能
- **聊天记忆**：让 AI 记住之前的对话内容，实现连续对话
- **自定义 Advisor**：自定义 AI 回答的处理逻辑
- **流式响应**：实时获取 AI 的回答

---

## 🔧 技术栈

- **Spring Boot 3.x**：Web 框架
- **Spring AI Alibaba**：阿里云 AI 集成
- **QWQ-Plus 模型**：阿里云通义千问推理模型
- **Java 17**：编程语言

---

## 📖 核心概念讲解

### 1. 什么是 Advisor？

**通俗解释**：Advisor 就像是 AI 的"私人秘书"或"助手"，在 AI 处理用户请求的过程中，可以在不同阶段进行干预和处理。

**工作流程**：
# Spring AI Advisor 功能详解 - 通俗易懂版

## 📚 项目概述

这个项目演示了 Spring AI 的 **Advisor（顾问）** 功能，就像给 AI 模型配备了一个"智能助手"，在 AI 回答问题前后进行一些处理。

### 🎯 核心功能
- **聊天记忆**：让 AI 记住之前的对话内容，实现连续对话
- **自定义 Advisor**：自定义 AI 回答的处理逻辑
- **流式响应**：实时获取 AI 的回答

---

## 🔧 技术栈

- **Spring Boot 3.x**：Web 框架
- **Spring AI Alibaba**：阿里云 AI 集成
- **QWQ-Plus 模型**：阿里云通义千问推理模型
- **Java 17**：编程语言

---

## 📖 核心概念讲解

### 1. 什么是 Advisor？

**通俗解释**：Advisor 就像是 AI 的"私人秘书"或"助手"，在 AI 处理用户请求的过程中，可以在不同阶段进行干预和处理。

**工作流程**：

用户提问 → Advisor 处理（before） → AI 思考 → Advisor 处理（after） → 返回答案



```
**生活中的比喻**：
- 就像你去餐厅吃饭，服务员（Advisor）会先记录你的需求（before），厨师（AI）做菜，服务员再检查菜品质量后端给你（after）

### 2. ChatMemory（聊天记忆）

**通俗解释**：让 AI 能够"记住"之前的对话内容，就像和朋友聊天一样，能够基于上下文进行交流。

**生活中的比喻**：
- 普通聊天：每次都像第一次见面，需要重新介绍自己
- 有记忆的聊天：像老朋友聊天，记得之前说过的话

### 3. ChatClient（聊天客户端）

**通俗解释**：这是与 AI 模型交互的主要工具，就像打电话的"电话机"，通过它与 AI 进行沟通。

---
```

---

## 💻 核心代码详解

### 1. 启动类（AdvisorApplication.java）

```java
@SpringBootApplication
public class AdvisorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdvisorApplication.class, args);
    }
}
```

**简单说明**：这是 Spring Boot 的标准启动类，就像程序的"入口大门"。

---

### 2. 控制器（AdvisorsController.java）

#### 2.1 核心组件初始化

```java
private final ChatClient chatClient;
private final InMemoryChatMemory chatMemory = new InMemoryChatMemory();
private final int CHAT_MEMORY_RETRIEVE_SIZE = 100;
```

**通俗解释**：
- `chatClient`：与 AI 对话的工具
- `chatMemory`：AI 的"大脑记忆"，存储在内存中
- `CHAT_MEMORY_RETRIEVE_SIZE`：记住最近 100 条对话

#### 2.2 配置 Advisor

```java
this.chatClient = builder
    .defaultAdvisors(
        new MessageChatMemoryAdvisor(chatMemory)
        // 整合 QWQ 的思考过程到输出中
        // ,new ReasoningContentAdvisor(0)
    )
    .build();
```

**通俗解释**：
- 给 AI 配备了一个"记忆助手"
- 注释掉的 `ReasoningContentAdvisor` 是自定义的思考过程处理器

#### 2.3 聊天接口

```java
@GetMapping("/chatWithChatMemory")
public Flux<String> chatWithChatMemory(String chatId, String prompt) {
    return chatClient.prompt()
        .user(prompt)
        .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE))
        .stream().content();
}
```

**通俗解释**：
- `chatId`：对话的唯一标识，就像"聊天室号"
- `prompt`：用户的问题
- `Flux<String>`：流式响应，实时显示 AI 的回答
- 通过 `advisors` 传入对话 ID 和记忆大小

#### 2.4 获取历史消息

```java
@GetMapping("messages")
public List<Message> getMessages(
    @RequestParam(value = "chatId") String chatId, 
    @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
) {
    if (Objects.isNull(chatId)) {
        throw new RuntimeException("chatId is null");
    }
    return chatMemory.get(chatId, size);
}
```

**通俗解释**：
- 查看某个对话的历史记录
- `chatId`：对话 ID
- `size`：获取多少条历史消息（默认 10 条）

---

### 3. 自定义 Advisor（ReasoningContentAdvisor.java）

#### 3.1 核心接口实现

```java
public class ReasoningContentAdvisor implements BaseAdvisor {
    
    private final int order;
    
    public ReasoningContentAdvisor(Integer order) {
        this.order = order != null ? order : 0;
    }
    
    @NotNull
    @Override
    public AdvisedRequest before(@NotNull AdvisedRequest request) {
        return request;
    }
    
    @NotNull
    @Override
    public AdvisedResponse after(AdvisedResponse advisedResponse) {
        // 处理 AI 的回答
        ChatResponse resp = advisedResponse.response();
        if (Objects.isNull(resp)) {
            return advisedResponse;
        }
        
        // 获取 AI 的思考过程
        String reasoningContent = String.valueOf(
            resp.getResults().get(0).getOutput().getMetadata().get("reasoningContent")
        );
        
        // 如果有思考过程，将其整合到回答中
        if (StringUtils.hasText(reasoningContent)) {
            List<Generation> thinkGenerations = resp.getResults().stream()
                .map(generation -> {
                    AssistantMessage output = generation.getOutput();
                    AssistantMessage thinkAssistantMessage = new AssistantMessage(
                        String.format("思考过程：\n%s\n\n回答：\n%s", reasoningContent, output.getText()),
                        output.getMetadata(),
                        output.getToolCalls(),
                        output.getMedia()
                    );
                    return new Generation(thinkAssistantMessage, generation.getMetadata());
                }).toList();
            
            ChatResponse thinkChatResp = ChatResponse.builder()
                .from(resp).generations(thinkGenerations).build();
            return AdvisedResponse.from(advisedResponse).response(thinkChatResp).build();
        }
        
        return advisedResponse;
    }
    
    @Override
    public int getOrder() {
        return this.order;
    }
}
```

**通俗解释**：
- `before()` 方法：在 AI 回答前处理（当前版本未使用）
- `after()` 方法：在 AI 回答后处理，提取思考过程并整合到回答中
- `getOrder()` 方法：定义 Advisor 的执行顺序

**功能说明**：

- QWQ 模型会在元数据中存储思考过程
- 这个 Advisor 将思考过程提取出来，添加到最终回答中
- 让用户可以看到 AI 的"思考过程"

---

### 4. 配置文件（application.yml）

```yaml
server:
  port: 8081

spring:
  application:
    name: Advisor

  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        options:
          model: qwq-plus
```

**通俗解释**：
- `server.port`：服务运行在 8081 端口
- `spring.ai.dashscope.api-key`：阿里云 API 密钥（从环境变量读取）
- `model: qwq-plus`：使用 QWQ-Plus 推理模型

---

## 🚀 使用方法

### 1. 环境准备

**设置 API 密钥**：
```bash
# Windows
set DASHSCOPE_API_KEY=你的API密钥

# Linux/Mac
export DASHSCOPE_API_KEY=你的API密钥
```

### 2. 启动项目

```bash
mvn spring-boot:run
```

### 3. 测试接口

#### 3.1 发送消息（带记忆）

```bash
curl "http://localhost:8081/advisors/chatWithChatMemory?chatId=user123&prompt=你好"
```

**参数说明**：
- `chatId`：对话唯一标识（同一个 ID 会共享记忆）
- `prompt`：用户的问题

#### 3.2 继续对话（测试记忆功能）

```bash
curl "http://localhost:8081/advisors/chatWithChatMemory?chatId=user123&prompt=我刚才说了什么？"
```

**预期结果**：AI 应该能记住你之前说了"你好"

#### 3.3 查看历史消息

```bash
curl "http://localhost:8081/advisors/messages?chatId=user123&size=10"
```

**返回示例**：
```json
[
  {
    "content": "你好",
    "messageType": "USER"
  },
  {
    "content": "你好！有什么我可以帮助你的吗？",
    "messageType": "ASSISTANT"
  }
]
```

---

## 🎓 学习要点

### 1. Advisor 的作用

- **请求前处理**：修改用户输入、添加系统提示等
- **请求后处理**：修改 AI 回答、提取元数据、格式化输出等
- **链式调用**：可以配置多个 Advisor，按顺序执行

### 2. ChatMemory 的实现

- **InMemoryChatMemory**：内存存储（重启后丢失）
- **可扩展性**：可以替换为数据库存储
- **会话隔离**：通过 `chatId` 区分不同对话

### 3. 流式响应的优势

- **实时反馈**：不需要等待完整回答
- **更好的用户体验**：像打字一样逐字显示
- **节省内存**：不需要存储完整响应

---

## 🔍 进阶思考

### 1. 如何持久化聊天记忆？

**思路**：
- 使用 Redis 或数据库存储聊天记录
- 实现 `ChatMemory` 接口，自定义存储逻辑

### 2. 如何实现多个用户隔离？

**思路**：
- 使用 `userId + chatId` 作为唯一标识
- 在 Advisor 中添加权限控制

### 3. 如何优化性能？

**思路**：
- 限制历史消息数量
- 使用向量检索相关历史消息
- 实现消息摘要功能

---

## 📝 总结

这个项目展示了 Spring AI 的核心功能：

1. **Advisor 模式**：灵活的请求处理机制
2. **聊天记忆**：实现连续对话
3. **流式响应**：实时获取 AI 回答
4. **自定义扩展**：根据需求定制功能

**学习建议**：
- 先理解基本概念
- 运行项目体验功能
- 阅读源码理解实现
- 尝试自定义扩展

---

## 🎯 实践练习

1. **基础练习**：
   - 运行项目，测试聊天功能
   - 尝试不同的 `chatId`，观察记忆隔离效果

2. **进阶练习**：
   - 启用 `ReasoningContentAdvisor`，观察思考过程
   - 修改 `CHAT_MEMORY_RETRIEVE_SIZE`，测试记忆容量

3. **挑战练习**：
   - 实现自定义 Advisor，添加日志记录
   - 将聊天记录保存到文件

---

**文档版本**：1.0  
**适用人群**：Java 初学者、Spring AI 学习者  
**更新日期**：2025-05-15