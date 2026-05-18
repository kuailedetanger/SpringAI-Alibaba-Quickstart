# MCP (Model Context Protocol) 教学指南

---

## 一、什么是 MCP？

**MCP** 全称是 **Model Context Protocol**，中文叫做"模型上下文协议"。

### 简单理解

想象一下：
- **AI 模型**就像一个聪明的大脑，但它自己不会看天气、不会翻译、不会查资料
- **MCP** 就像是给这个大脑接上了各种"手脚"，让它能够调用外部工具来完成任务

**一句话总结**：MCP 让 AI 模型拥有了调用外部工具的能力！

---

## 二、项目结构解析

我们的 Mcp 目录包含四个模块：


### 2.1 角色分工

| 角色 | 功能 | 比喻 |
|------|------|------|
| **服务端 (Server)** | 提供工具能力（时间、天气、翻译） | 工具仓库 |
| **客户端 (Client)** | 调用 AI 模型，让模型决定是否使用工具 | 用户 + AI 大脑 |

### 2.2 通信方式对比

| 方式 | 英文含义 | 特点 | 适用场景 |
|------|------|----------|----------|
| **Stdio** | 标准输入输出（standard input/output) | 通过命令行输入输出通信 | 本地测试、简单场景 |
| **Webflux |                                      | 通过网络 API 通信 | 生产环境、远程调用 |

---

## 三、核心工具组件

本项目提供了三个实用工具：

### 3.1 时间服务 (TimeService)

**功能**：获取世界任意城市的当前时间

```java
@Tool(description = "Get the time of a specified city.")
public String getCityTimeMethod(
    @ToolParam(description = "Time zone id, such as Asia/Shanghai") String timeZoneId)
```

**常用时区**：
- 北京：`Asia/Shanghai`
- 东京：`Asia/Tokyo`
- 纽约：`America/New_York`
- 伦敦：`Europe/London`

### 3.2 天气服务 (OpenMeteoService)

**功能**：获取指定经纬度的天气预报

```java
@Tool(description = "获取指定经纬度的天气预报")
public String getWeatherForecastByLocation(double latitude, double longitude)
```

**常用城市坐标**：
- 北京：纬度 `39.9042`，经度 `116.4074`
- 上海：纬度 `31.2304`，经度 `121.4737`
- 广州：纬度 `23.1291`，经度 `113.2644`

### 3.3 翻译服务 (BaidutranslateService)

**功能**：使用百度翻译 API 进行文本翻译

```java
@Tool(description = "Baidu translation function")
public Map<String, String> baiduTranslateMethod(
    String q,    // 要翻译的内容
    String from, // 源语言
    String to)   // 目标语言
```

**语言代码**：
- 中文：`zh`
- 英文：`en`
- 日文：`jp`
- 韩文：`kor`

---

## 四、工作流程详解

### 4.1 完整调用流程


### 4.2 具体步骤说明（以"北京现在几点了？"为例）

1. **用户提问**："北京现在几点了？"

2. **客户端接收**：把问题传给 AI 模型

3. **AI 分析**：
   - 理解问题需要获取时间信息
   - 发现有 `TimeService` 工具可用
   - 决定调用 `getCityTimeMethod("Asia/Shanghai")`

4. **服务端执行**：
   - 收到工具调用请求
   - 使用 Java 的 `ZonedDateTime` 获取上海时区时间
   - 返回结果

5. **AI 总结**：把工具返回的结果用自然语言整理成回答

6. **返回给用户**："北京现在是下午2点30分。"

---

## 五、代码核心解析

### 5.1 工具注册机制

在 `ToolConfiguration.java` 中，我们把所有工具注册到系统中：

```java
@Configuration
public class ToolConfiguration {
    
    @Bean
    public ToolCallbackProvider weatherTools(
        OpenMeteoService openMeteoService,
        TimeService timeService,
        BaidutranslateService baidutranslateService) {
        
        // 把三个工具对象注册到工具提供者中
        return MethodToolCallbackProvider.builder()
            .toolObjects(openMeteoService, timeService, baidutranslateService)
            .build();
    }
}
```

### 5.2 工具标记注解

使用 `@Tool` 注解标记方法为可调用的工具：

```java
@Service
public class TimeService {
    
    // @Tool 注解告诉系统这是一个可以被AI调用的工具
    @Tool(description = "Get the time of a specified city.")
    public String getCityTimeMethod(
        // @ToolParam 描述参数的含义，帮助AI理解如何调用
        @ToolParam(description = "Time zone id, such as Asia/Shanghai") String timeZoneId) {
        
        ZoneId zid = ZoneId.of(timeZoneId);
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zid);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        return zonedDateTime.format(formatter);
    }
}
```

### 5.3 客户端交互逻辑

```java
public class ClientStdioApplication {
    
    @Bean
    public CommandLineRunner predefinedQuestions(ChatClient.Builder chatClientBuilder, 
                                                ToolCallbackProvider tools) {
        return args -> {
            // 构建带有工具能力的聊天客户端
            var chatClient = chatClientBuilder
                .defaultTools(tools)  // 加载所有注册的工具
                .build();
            
            // 创建命令行交互
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\n>>> QUESTION: ");
                String userInput = scanner.nextLine();
                
                if (userInput.equalsIgnoreCase("exit")) {
                    break;  // 输入 exit 退出
                }
                
                // 调用AI并获取回答
                String answer = chatClient.prompt(userInput).call().content();
                System.out.println("\n>>> ASSISTANT: " + answer);
            }
        };
    }
}
```

---

## 六、配置说明

### 6.1 服务端配置 (application.yml)

```yaml
spring:
  application:
    name: mcp-server-stdio
  
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}  # 阿里云通义千问 API Key
    
    mcp:
      server:
        name: weather-time-translate
        version: 0.0.1
    
    toolcalling:
      baidutranslate:
        app-id: ${BAIDU_TRANSLATE_APP_ID}
        secret-key: ${BAIDU_TRANSLATE_SECRET_KEY}
```

### 6.2 需要设置的环境变量

| 变量名 | 说明 | 获取方式 |
|--------|------|----------|
| `DASHSCOPE_API_KEY` | 阿里云通义千问 API Key | 阿里云控制台申请 |
| `BAIDU_TRANSLATE_APP_ID` | 百度翻译 App ID | 百度翻译开放平台申请 |
| `BAIDU_TRANSLATE_SECRET_KEY` | 百度翻译密钥 | 百度翻译开放平台申请 |

---

## 七、运行项目

### 7.1 运行 Stdio 版本

**第一步：运行服务端**

```bash
# 进入服务端目录
cd mcp-server-stdio

# 设置环境变量（Windows）
set DASHSCOPE_API_KEY=你的API密钥
set BAIDU_TRANSLATE_APP_ID=你的AppID
set BAIDU_TRANSLATE_SECRET_KEY=你的密钥

# 运行服务
mvn spring-boot:run
```

**第二步：运行客户端**

```bash
# 进入客户端目录
cd mcp-client-stdio

# 设置环境变量
set DASHSCOPE_API_KEY=你的API密钥

# 运行客户端
mvn spring-boot:run

# 然后就可以输入问题了
>>> QUESTION: 北京现在天气怎么样？
>>> ASSISTANT: 北京当前温度...
```

### 7.2 运行 Webflux 版本

Webflux 版本通过 HTTP 网络通信，适合远程调用：

```bash
# 运行服务端
cd mcp-server-webflux
mvn spring-boot:run

# 运行客户端
cd mcp-client-webflux
mvn spring-boot:run
```

---

## 八、实践练习

### 练习 1：获取时间

**提问**："东京现在几点了？"

**预期结果**：AI 调用 `TimeService.getCityTimeMethod("Asia/Tokyo")` 返回东京时间

### 练习 2：获取天气

**提问**："上海今天天气怎么样？"

**预期结果**：AI 调用 `OpenMeteoService.getWeatherForecastByLocation(31.2304, 121.4737)`

### 练习 3：翻译功能

**提问**："把 'Hello World' 翻译成中文"

**预期结果**：AI 调用 `BaidutranslateService.baiduTranslateMethod("Hello World", "en", "zh")`

### 练习 4：组合使用

**提问**："纽约现在几点？天气怎么样？"

**预期结果**：AI 会依次调用时间工具和天气工具，然后综合回答

---

## 九、扩展开发：添加新工具

### 步骤 1：创建新工具类

```java
@Service
public class CalculatorService {
    
    @Tool(description = "简单计算器，支持加减乘除")
    public String calculate(
        @ToolParam(description = "第一个数字") double num1,
        @ToolParam(description = "运算符：+ - * /") String operator,
        @ToolParam(description = "第二个数字") double num2) {
        
        double result = switch (operator) {
            case "+" -> num1 + num2;
            case "-" -> num1 - num2;
            case "*" -> num1 * num2;
            case "/" -> num1 / num2;
            default -> throw new IllegalArgumentException("未知运算符");
        };
        
        return String.format("%.2f %s %.2f = %.2f", num1, operator, num2, result);
    }
}
```

### 步骤 2：注册新工具

在 `ToolConfiguration.java` 中添加：

```java
@Bean
public ToolCallbackProvider weatherTools(
    OpenMeteoService openMeteoService,
    TimeService timeService,
    BaidutranslateService baidutranslateService,
    CalculatorService calculatorService) {  // 添加新工具
    
    return MethodToolCallbackProvider.builder()
        .toolObjects(openMeteoService, timeService, baidutranslateService, calculatorService)
        .build();
}
```

### 步骤 3：测试新工具

运行项目后提问："计算 100 乘以 5 等于多少？"

---

## 十、常见问题

### Q1：为什么需要 MCP？

**A**：AI 模型的知识是固定的（训练截止日期之前的），MCP 让 AI 能够调用外部工具，获取实时数据（如天气）、执行计算、访问最新信息等。

### Q2：Stdio 和 Webflux 有什么区别？

**A**：
- **Stdio**：通过命令行通信，适合本地测试
- **Webflux**：通过 HTTP 网络通信，适合生产环境

### Q3：为什么我的工具没有被调用？

**A**：可能原因：
1. 工具没有使用 `@Tool` 注解标记
2. 工具没有在 `ToolConfiguration` 中注册
3. AI 模型认为不需要调用工具
4. 参数描述不够清晰

### Q4：需要付费吗？

**A**：
- OpenMeteo 天气 API：免费
- 百度翻译 API：有免费额度
- 阿里云通义千问：需要付费

---

## 十一、总结

通过学习这个项目，你可以：

1. ✅ 理解 MCP 的核心概念
2. ✅ 学会创建和注册工具
3. ✅ 掌握客户端和服务端的配置
4. ✅ 能够扩展添加新的工具功能

这是 AI 应用开发中非常重要的一项技能，掌握它可以让你的 AI 应用更加强大！

---

**文档版本**：v1.0  
**适用场景**：课堂教学、初学者入门  
**最后更新**：2026年5月
