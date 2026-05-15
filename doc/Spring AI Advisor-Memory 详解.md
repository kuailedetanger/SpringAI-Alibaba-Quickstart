# Spring AI Advisor-Memory 项目详解 - 课堂教学版

## 一、项目概述

### 1.1 什么是"聊天记忆"？

想象一下，你和朋友聊天时：
- 你说："昨天我们聊的那个电影..."
- 朋友能听懂你在说什么，因为他记得你们之前的对话

**AI聊天机器人也需要这种"记忆力"！**

如果没有记忆，每次对话都是独立的，就像这样：

用户：我想买一部手机 AI：好的，请问您有什么预算？ 用户：它支持5G吗？ AI：抱歉，您问的是什么产品？


有了记忆之后：

### 1.2 三种记忆存储方式

| 存储方式 | 特点 | 适用场景 |
|---------|------|---------|
| **In-Memory** | 存在内存中，重启就消失 | 测试、临时会话 |
| **MySQL** | 存在数据库，永久保存 | 需要持久化的生产环境 |
| **Redis** | 存在缓存，读写快 | 高并发、性能要求高 |

---

## 二、项目结构


---

## 三、核心代码讲解

### 3.1 启动类 - 项目的"入口大门"

```java
@SpringBootApplication
public class AdvisorMemoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdvisorMemoryApplication.class, args);
    }
}
```

**简单理解：** 这就是项目的启动按钮，运行这个类，整个Spring Boot项目就启动了。

### 3.2 控制器 - 处理用户请求

`AdvisorController` 是对外的API接口，提供了三个接口：

| 接口地址 | 功能 |
|---------|------|
| `/advisor/in-memory` | 使用内存存储聊天记录 |
| `/advisor/mysql` | 使用MySQL存储聊天记录 |
| `/advisor/redis` | 使用Redis存储聊天记录 |

**关键代码解析：**

```java
@RestController
@RequestMapping("/advisor")
public class AdvisorController {
    
    private final ChatClient chatClient;  // AI聊天客户端
    private final InMemoryChatMemory chatMemory = new InMemoryChatMemory();
    
    // 构造函数：创建带有记忆功能的聊天客户端
    public AdvisorController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultAdvisors(
                    new MessageChatMemoryAdvisor(chatMemory)  // 配置记忆顾问
                )
                .build();
    }
}
```

**像这样理解：**
- `ChatClient` 就像一个"会说话的机器人"
- `ChatMemory` 是机器人的"记忆大脑"
- `MessageChatMemoryAdvisor` 是"记忆管家"，负责管理记忆

### 3.3 MySQL记忆组件

`MysqlChatMemory` 负责将聊天记录存储到MySQL数据库中。

**核心功能：**

```java
// 添加消息到记忆
public void add(String conversationId, List<Message> messages) {
    // 1. 先读取已有的消息
    List<Message> all = this.selectMessageById(conversationId);
    // 2. 添加新消息
    all.addAll(messages);
    // 3. 保存到数据库
    this.updateMessageById(conversationId, this.objectMapper.writeValueAsString(all));
}

// 获取消息
public List<Message> get(String conversationId, int lastN) {
    List all = this.selectMessageById(conversationId);
    // 返回最后N条消息
    return all != null ? all.stream().skip(
        Math.max(0, all.size() - lastN)
    ).toList() : List.of();
}
```

**工作流程：**


### 3.4 Redis记忆组件

`RedisChatMemory` 使用Redis的列表数据结构存储聊天记录。

**核心代码：**

```java
public void add(String conversationId, List<Message> messages) {
    String key = "spring_ai_alibaba_chat_memory:" + conversationId;
    for (Message message : messages) {
        String messageJson = this.objectMapper.writeValueAsString(message);
        this.jedis.rpush(key, messageJson);  // 像排队一样加到列表末尾
    }
}

public List<Message> get(String conversationId, int lastN) {
    String key = "spring_ai_alibaba_chat_memory:" + conversationId;
    // 获取列表最后N个元素
    List<String> messageStrings = this.jedis.lrange(key, -lastN, -1);
    // 转换为Message对象
    ...
}
```

**Redis的优势：**
- 读写速度极快（内存操作）
- 适合高并发场景
- 支持过期策略

### 3.5 消息反序列化器

`MessageDeserializer` 负责将JSON格式的消息转换为Java对象。

```java
public Message deserialize(JsonParser p, DeserializationContext ctxt) {
    // 解析JSON
    JsonNode node = mapper.readTree(p);
    String messageType = node.get("messageType").asText();
    
    // 根据类型创建不同的消息对象
    switch (messageType) {
        case "USER" -> message = new UserMessage(...);
        case "ASSISTANT" -> message = new AssistantMessage(...);
        default -> throw new IllegalArgumentException("Unknown message type");
    }
    return message;
}
```

---

## 四、配置说明

### 4.1 application.yml

```yaml
server:
  port: 8082                    # 服务端口

spring:
  application:
    name: Advisor-Memory        # 应用名称
  
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}  # 阿里云API密钥（环境变量）
      chat:
        options:
          model: qwq-plus       # 使用的AI模型

  datasource:
    url: 127.0.0.1:3306        # MySQL地址
    username: root              # MySQL用户名
    password: root              # MySQL密码
  
  data:
    redis:
      host: 127.0.0.1           # Redis地址
      port: 6379                # Redis端口
```

### 4.2 环境变量设置

运行前需要设置环境变量：

```bash
# Windows系统
set DASHSCOPE_API_KEY=你的阿里云API密钥

# Linux/Mac系统
export DASHSCOPE_API_KEY=你的阿里云API密钥
```

---

## 五、使用方法

### 5.1 启动服务

```bash
cd Advisor-Memory
mvn spring-boot:run
```

### 5.2 调用API示例

使用浏览器或curl访问：

**1. 内存存储方式**
```bash
curl "http://localhost:8082/advisor/in-memory?prompt=你好&chatId=user001"
```

**2. MySQL存储方式**
```bash
curl "http://localhost:8082/advisor/mysql?prompt=推荐一部电影&chatId=user001"
```

**3. Redis存储方式**
```bash
curl "http://localhost:8082/advisor/redis?prompt=它讲的是什么故事&chatId=user001"
```

---

## 六、三种存储方式对比

| 特性 | In-Memory | MySQL | Redis |
|-----|-----------|-------|-------|
| **数据持久化** | ❌ 重启丢失 | ✅ 永久保存 | ⚠️ 可配置 |
| **读写速度** | 最快 | 较慢 | 很快 |
| **数据容量** | 受内存限制 | 几乎无限 | 受内存限制 |
| **适用场景** | 测试、临时 | 生产持久化 | 高并发缓存 |
| **部署复杂度** | 简单 | 需要部署数据库 | 需要部署Redis |

---

## 七、课堂练习建议

### 练习1：理解记忆机制

1. 启动服务，调用 `/advisor/in-memory` 接口
2. 连续发送多条消息，观察AI是否记得上下文
3. 重启服务，再次发送消息，观察记忆是否丢失

### 练习2：对比三种存储方式

1. 分别调用三个接口，记录响应时间
2. 查看MySQL数据库中的 `chat_memory` 表
3. 使用 `redis-cli` 查看Redis中的数据

### 练习3：扩展功能

尝试添加以下功能：
- 清空指定会话的记忆
- 查询会话的消息数量
- 设置消息过期时间

---

## 八、总结

这个项目展示了Spring AI如何实现聊天记忆功能：

1. **核心思想**：将对话历史存储起来，发送请求时带上历史记录
2. **三种实现**：内存、MySQL、Redis各有适用场景
3. **关键组件**：`ChatMemory`接口定义了记忆的基本操作
4. **Advisor模式**：通过`MessageChatMemoryAdvisor`将记忆能力注入到ChatClient

**记住一句话：** AI的"记忆力"本质上就是把对话历史存起来，下次聊天时一起发给AI模型！

---

**参考资料：**
- Spring AI官方文档：https://docs.spring.io/spring-ai/reference/
- 阿里云DashScope：https://dashscope.aliyun.com/
