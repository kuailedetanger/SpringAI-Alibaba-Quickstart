# 阿里云百炼 API 实战教程

## 🎯 课程目标

通过本课程学习，你将能够：
- 理解什么是 AI 大模型 API
- 学会调用阿里云百炼平台的文本生成接口
- 掌握流式输出和多模态（文本+音频）的调用方式
- 了解如何在实际项目中集成 AI 能力

---

## 🏗️ 项目结构概览

D:\IDEA_WorkingSpace2\SpringAI-Alibaba-Quickstart\Tool-Calling\doc\Tool-Calling教学指南.md
# Spring AI Tool-Calling 工具调用教学指南

## 一、什么是 Tool-Calling（工具调用）

### 1.1 概念通俗解释

想象一下，你是一个超级智能的机器人，但你有一个小缺点：**你不擅长查天气、查时间、翻译外语**这些具体的事情。

怎么办呢？你可以找一些"小帮手"来帮你做这些事：
- 天气小帮手：专门查天气
- 时间小帮手：专门查不同城市的时间
- 翻译小帮手：专门做语言翻译

当用户问你"北京明天天气怎么样？"，你知道自己不会查天气，就会：
1. 识别出需要查天气
2. 把任务交给天气小帮手
3. 小帮手查完告诉你结果
4. 你再用友好的语言告诉用户

这个过程就是 **Tool-Calling（工具调用）**！

### 1.2 在 AI 对话中的作用

在传统的 AI 聊天中：
- AI 只能"凭记忆"回答问题
- 如果问实时信息（如天气），AI 可能答不上来或者给过时的答案

有了 Tool-Calling 之后：
- AI 可以调用外部工具获取实时数据
- 回答更加准确、更有价值

## 二、项目结构概览


## 三、核心组件讲解

### 3.1 工具类（Tools）

工具类是实现具体功能的核心，每个工具类都有一个或多个方法，使用 `@Tool` 注解标记。

#### 示例：天气工具类

```java
public class WeatherTools {

    @Tool(description = "Use api.weather to get weather information.")
    public Response getWeatherServiceMethod(
        @ToolParam(description = "City name") String city,
        @ToolParam(description = "Number of days of weather forecast") int days) {
        // 调用天气 API 获取数据
        return response;
    }
}
```

**关键点解析：**

| 注解 | 作用 | 通俗解释 |
|------|------|----------|
| `@Tool` | 标记这是一个工具方法 | 告诉 AI："这是一个可以调用的工具" |
| `@ToolParam` | 描述方法参数 | 告诉 AI："这个参数是什么意思" |
| `description` | 工具/参数的描述 | AI 需要靠这个理解什么时候调用工具 |

### 3.2 配置类（AutoConfiguration）

配置类负责创建工具实例并交给 Spring 管理。

```java
@Configuration
public class WeatherAutoConfiguration {

    @Bean
    public WeatherTools weatherTools(WeatherProperties properties) {
        return new WeatherTools(properties);
    }
}
```

**通俗解释：**
- Spring 启动时会自动运行这个配置类
- 创建 `WeatherTools` 对象并"注册"到系统中
- 其他地方需要使用时，Spring 会自动提供

### 3.3 属性类（Properties）

属性类用于管理配置信息（如 API Key）。

```java
@ConfigurationProperties(prefix = "spring.ai.toolcalling.weather")
public class WeatherProperties {
    private String apiKey;
    // getter/setter
}
```

**配置文件 application.yml：**

```yaml
spring:
  ai:
    toolcalling:
      weather:
        enabled: true
        api-key: ${WEATHER_API_KEY}
```

**通俗解释：**
- 敏感信息（如 API Key）不写死在代码里
- 通过环境变量 `${WEATHER_API_KEY}` 传入
- 更加安全、灵活

### 3.4 控制器（Controller）

控制器提供对外的 HTTP 接口，让用户可以通过浏览器或 Postman 调用。

```java
@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final ChatClient dashScopeChatClient;

    @GetMapping("/chat-tool-method")
    public String chatTranslateMethod(
        @RequestParam(defaultValue = "请告诉我北京1天以后的天气") String query) {
        return dashScopeChatClient
            .prompt(query)
            .tools(new WeatherTools(weatherProperties))
            .call()
            .content();
    }
}
```

**调用流程：**
1. 用户访问 `/weather/chat-tool-method?query=北京明天天气`
2. 控制器收到请求
3. 调用 ChatClient 并传入天气工具
4. AI 自动判断是否需要调用工具
5. 返回最终结果

## 四、三种调用方式对比

### 4.1 无工具版（simpleChat）

```java
@GetMapping("/chat")
public String simpleChat(String query) {
    return dashScopeChatClient.prompt(query).call().content();
}
```

**特点：**
- 直接让 AI 回答
- 无法获取实时数据
- 如果问天气，AI 可能胡说八道

### 4.2 工具方法版（chat-tool-method）

```java
@GetMapping("/chat-tool-method")
public String chatTranslateMethod(String query) {
    return dashScopeChatClient
        .prompt(query)
        .tools(new WeatherTools(weatherProperties))  // 传入工具实例
        .call()
        .content();
}
```

**特点：**
- 直接传入工具对象
- 简单直接
- 适合快速测试

### 4.3 工具名称版（chat-tool-function）

```java
@GetMapping("/chat-tool-function")
public String chatTranslateFunction(String query) {
    return dashScopeChatClient
        .prompt(query)
        .tools("getWeatherFunction")  // 传入工具名称
        .call()
        .content();
}
```

**特点：**
- 通过名称引用工具
- 需要提前注册工具
- 适合复杂场景

```
┌─────────────────────────────────────────────────────────────────┐
│                    启动时：注册工具                              │
├─────────────────────────────────────────────────────────────────┤
│ WeatherAutoConfiguration                                       │
│   @Bean(name = "getWeatherFunction")                           │
│       ↓                                                        │
│   创建 WeatherService 对象                                      │
│       ↓                                                        │
│   注册到 Spring 容器（名字：getWeatherFunction）                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    请求时：使用工具                              │
├─────────────────────────────────────────────────────────────────┤
│ WeatherController                                              │
│   .tools("getWeatherFunction")                                 │
│       ↓                                                        │
│   Spring AI 在容器中查找名字为 "getWeatherFunction" 的 Bean      │
│       ↓                                                        │
│   找到 WeatherService 对象                                     │
│       ↓                                                        │
│   AI 调用 WeatherService.apply(Request)                        │
│       ↓                                                        │
│   返回天气数据                                                  │
└─────────────────────────────────────────────────────────────────┘
```

| 注解                        | 作用                                       |
| --------------------------- | ------------------------------------------ |
| `@Bean(name = "xxx")`       | 给 Bean 起一个名字，方便查找               |
| `@ConditionalOnMissingBean` | 如果已经有同名 Bean 就不创建了（避免重复） |
| `@Description`              | 描述这个 Bean 的用途                       |

## 五、三大工具详解

### 5.1 天气工具（WeatherTools）

**功能：** 查询指定城市的天气预报

**使用方式：**

**核心代码：**
```java
@Tool(description = "Use api.weather to get weather information.")
public Response getWeatherServiceMethod(
    @ToolParam(description = "City name") String city,
    @ToolParam(description = "Number of days of weather forecast. Value ranges from 1 to 14") int days) {
    // 调用天气 API
    String url = "https://api.weatherapi.com/v1/forecast.json?q=" + city + "&days=" + days;
    // 返回结果
    return response;
}
```

### 5.2 时间工具（TimeTools）

**功能：** 获取指定时区的当前时间

**使用方式：**

**核心代码：**
```java
@Tool(description = "Get the time of a specified city.")
public String getCityTimeMethod(
    @ToolParam(description = "Time zone id, such as Asia/Shanghai") String timeZoneId) {
    return ZoneUtils.getTimeByZoneId(timeZoneId);
}
```

**常见时区 ID：**
- 北京：`Asia/Shanghai`
- 纽约：`America/New_York`
- 伦敦：`Europe/London`
- 东京：`Asia/Tokyo`

### 5.3 翻译工具（BaidutranslateTools）

**功能：** 使用百度翻译 API 进行文本翻译

**使用方式：**

**核心代码：**
```java
@Tool(description = "Baidu translation function for general text translation")
public Map<String, String> baiduTranslateMethod(
    @ToolParam(description = "Content that needs to be translated") String q,
    @ToolParam(description = "Source language") String from,
    @ToolParam(description = "Target language") String to) {
    // 调用百度翻译 API
    return translations;
}
```

**语言代码：**
- 中文：`zh`
- 英文：`en`
- 日文：`jp`
- 韩文：`kor`

## 六、动手实践

### 6.1 环境准备

1. **JDK 17**：确保安装了 Java 17
2. **Maven**：用于管理依赖
3. **API Key**：
   - 阿里云 DashScope API Key（用于 AI 对话）
   - 天气 API Key（用于查询天气）
   - 百度翻译 API Key（用于翻译）

### 6.2 设置环境变量

在系统中设置以下环境变量：

**Windows：**
```cmd
set DASHSCOPE_API_KEY=你的阿里云API密钥
set WEATHER_API_KEY=你的天气API密钥
set BAIDU_TRANSLATE_APP_ID=你的百度翻译APP_ID
set BAIDU_TRANSLATE_SECRET_KEY=你的百度翻译密钥
```

**Linux/Mac：**
```bash
export DASHSCOPE_API_KEY=你的阿里云API密钥
export WEATHER_API_KEY=你的天气API密钥
export BAIDU_TRANSLATE_APP_ID=你的百度翻译APP_ID
export BAIDU_TRANSLATE_SECRET_KEY=你的百度翻译密钥
```

### 6.3 运行项目

```bash
cd Tool-Calling
mvn spring-boot:run
```

### 6.4 测试接口

打开浏览器或 Postman，访问以下地址：

1. **测试天气查询**

2. **测试时间查询**

3. **测试翻译功能**

## 七、工具调用的工作原理

### 7.1 完整流程图


### 7.2 AI 如何决定调用哪个工具

AI 会做以下思考：

1. **理解用户问题**："北京明天天气怎么样？"
2. **分析需求**：需要查询实时天气数据
3. **查找可用工具**：发现有 WeatherTools 工具
4. **匹配工具描述**：工具描述是"获取天气信息"，正好匹配需求
5. **提取参数**：从问题中提取出"北京"（城市）和"明天"（1天）
6. **调用工具**：执行 getWeatherServiceMethod("北京", 1)
7. **整理结果**：将工具返回的数据用自然语言描述给用户

### 7.3 @Tool 注解的重要性

`@Tool` 注解的 `description` 属性非常关键：

```java
@Tool(description = "Use api.weather to get weather information.")
```

**这个描述就像是工具的"说明书"，AI 靠它来理解：**
- 这个工具是干什么的？
- 什么时候应该用这个工具？

如果描述写得不好，AI 可能：
- 找不到正确的工具
- 用错工具
- 干脆不调用工具

## 八、扩展知识

### 8.1 自定义工具

如果你想添加自己的工具，可以按照以下步骤：

1. **创建工具类**：
```java
public class MyTools {

    @Tool(description = "计算两个数的和")
    public int add(
        @ToolParam(description = "第一个数") int a,
        @ToolParam(description = "第二个数") int b) {
        return a + b;
    }
}
```

2. **创建配置类**：
```java
@Configuration
public class MyAutoConfiguration {

    @Bean
    public MyTools myTools() {
        return new MyTools();
    }
}
```

3. **创建控制器**：
```java
@RestController
@RequestMapping("/my")
public class MyController {

    @GetMapping("/chat")
    public String chat(String query) {
        return dashScopeChatClient
            .prompt(query)
            .tools(new MyTools())
            .call()
            .content();
    }
}
```

### 8.2 工具调用的应用场景

| 场景 | 工具类型 | 示例 |
|------|----------|------|
| 实时数据 | 天气、股票、新闻 | "今天的上证指数是多少？" |
| 计算 | 计算器、单位换算 | "100美元等于多少人民币？" |
| 查询 | 数据库查询、百科 | "李白的出生地在哪里？" |
| 操作 | 文件操作、邮件发送 | "帮我发一封邮件给张三" |

### 8.3 注意事项

1. **API Key 安全**：不要把 API Key 硬编码在代码中
2. **错误处理**：工具调用可能失败，需要处理异常
3. **参数校验**：对用户输入的参数进行验证
4. **性能考虑**：工具调用可能耗时，考虑异步处理

## 九、常见问题解答

### Q1：为什么 AI 不调用工具？

**可能原因：**
- `@Tool` 的 `description` 描述不够清晰
- 问题描述不够明确
- 工具没有正确注册

**解决方法：**
- 检查 `@Tool` 注解的描述是否准确
- 尝试更明确的提问方式
- 检查配置类是否正确

### Q2：如何获取 API Key？

- **阿里云 DashScope**：登录阿里云控制台申请
- **天气 API**：注册 weatherapi.com 获取免费额度
- **百度翻译**：注册百度翻译开放平台

### Q3：工具调用失败怎么办？

检查以下几点：
1. API Key 是否正确
2. 网络是否畅通
3. 参数是否正确
4. API 调用次数是否超限

### Q4：可以同时调用多个工具吗？

可以！AI 可以根据需要调用多个工具，例如：
这会先调用天气工具，再调用翻译工具。

## 十、总结

### 核心要点

1. **Tool-Calling** 让 AI 可以调用外部工具获取实时数据
2. **@Tool 注解** 是标记工具方法的关键
3. **description** 属性帮助 AI 理解工具用途
4. **配置文件** 管理敏感信息

### 学习收获

通过本项目，你学会了：
- Spring AI 的工具调用机制
- 如何创建自定义工具
- AI 与外部系统的集成方式
- 实际项目的配置和部署

### 下一步

尝试扩展这个项目：
1. 添加新的工具（如股票查询、新闻查询）
2. 实现更复杂的多工具调用场景
3. 添加用户界面，打造一个智能助手应用

---

**作者**：yingzi
**版本**：1.0
**日期**：2025年3月
